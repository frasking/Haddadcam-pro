package com.example.network

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import com.example.model.CameraProtocol
import com.example.model.DiscoveredDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections
import java.util.UUID
import java.util.regex.Pattern

object LanScanner {

    // Common ports scanned in surveillance & network apps like TinyCam
    private val PROBE_PORTS = listOf(
        554 to CameraProtocol.RTSP,          // RTSP standard camera streaming
        8000 to CameraProtocol.ONVIF,        // Hikvision / ONVIF media
        8899 to CameraProtocol.ONVIF,        // ONVIF alternative
        37777 to CameraProtocol.WIRED_DVR,   // Dahua / DVR DVR
        80 to CameraProtocol.HTTP_MJPEG,     // Router Web Admin & Web cameras
        8080 to CameraProtocol.HTTP_MJPEG,   // Alternative HTTP / IP Webcam
        443 to CameraProtocol.HTTP_MJPEG,    // HTTPS Router / Secure Cameras
        8443 to CameraProtocol.HTTP_MJPEG,   // Secondary HTTPS
        53 to CameraProtocol.HTTP_MJPEG,     // DNS (standard on almost all WiFi routers)
        22 to CameraProtocol.HTTP_MJPEG      // SSH (common on routers / NAS / IP cameras)
    )

    /**
     * Detects the real IPv4 address and subnet of the current active network interface.
     * Returns Pair(localIp, subnetPrefix), e.g. ("192.168.1.55", "192.168.1")
     */
    fun getLocalSubnetInfo(): Pair<String, String>? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress ?: continue
                        if (!host.startsWith("127.")) {
                            val lastDot = host.lastIndexOf('.')
                            if (lastDot > 0) {
                                val subnet = host.substring(0, lastDot)
                                return Pair(host, subnet)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * Extracts the gateway / router IP from WifiManager DhcpInfo if available.
     */
    fun getWifiGatewayIp(context: Context?): String? {
        try {
            if (context != null) {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                val dhcp: DhcpInfo? = wifi?.dhcpInfo
                if (dhcp != null && dhcp.gateway != 0) {
                    val g = dhcp.gateway
                    return String.format("%d.%d.%d.%d", g and 0xff, g shr 8 and 0xff, g shr 16 and 0xff, g shr 24 and 0xff)
                }
            }
        } catch (e: Exception) {
        }
        return null
    }

    /**
     * Performs a comprehensive, real network discovery scan (similar to TinyCam Monitor Pro):
     * 1. UPnP SSDP Discovery (UDP 1900) - discovers WiFi routers and smart devices.
     * 2. ONVIF WS-Discovery (UDP 3702) - discovers Profile S CCTV cameras.
     * 3. Priority Gateway / Router probe - checks .1 and default gateway explicitly.
     * 4. Multi-threaded TCP port & reachability scan across all subnet host IPs (1..254).
     * 5. Resolves MAC vendors and device types.
     */
    suspend fun scanLocalSubnetReal(
        context: Context?,
        subnetPrefix: String,
        onProgress: (Int, String) -> Unit
    ): List<DiscoveredDevice> = withContext(Dispatchers.IO) {
        val discoveredMap = mutableMapOf<String, DiscoveredDevice>()

        val gatewayIp = getWifiGatewayIp(context) ?: "$subnetPrefix.1"

        // Phase 1: UPnP SSDP Discovery (UDP 1900) - standard router & media device discovery
        onProgress(3, "فحص UPnP SSDP لاكتشاف الراوتر وأجهزة الشبكة...")
        val ssdpDevices = discoverSsdpMulticastReal(context, subnetPrefix, gatewayIp)
        ssdpDevices.forEach { dev ->
            discoveredMap[dev.ip] = dev
        }

        // Phase 2: ONVIF WS-Discovery (UDP 3702) - IP Camera standard probe
        onProgress(8, "إرسال حزم كشف ONVIF القياسية (WS-Discovery)...")
        val onvifDevices = discoverOnvifMulticastReal(context, subnetPrefix)
        onvifDevices.forEach { dev ->
            // ONVIF camera has higher fidelity than generic SSDP
            discoveredMap[dev.ip] = dev
        }

        // Phase 3: Explicitly probe Gateway / Router (e.g. 192.168.1.1 or detected gateway)
        onProgress(12, "فحص الراوتر والبوابة الرئيسية ($gatewayIp)...")
        if (!discoveredMap.containsKey(gatewayIp)) {
            val gatewayDevice = probeSpecificHostReal(gatewayIp, isGateway = true)
            if (gatewayDevice != null) {
                discoveredMap[gatewayIp] = gatewayDevice
            }
        }

        // Phase 4: Read system ARP cache
        val arpMap = readSystemArpTable()

        // Phase 5: Multi-threaded TCP sweep on subnet IPs (1..254)
        val concurrencyLimit = Semaphore(35)
        val totalIps = 254
        var scannedCount = 0

        coroutineScope {
            val scanJobs = (1..totalIps).map { hostNum ->
                val ip = "$subnetPrefix.$hostNum"
                async(Dispatchers.IO) {
                    concurrencyLimit.withPermit {
                        if (!discoveredMap.containsKey(ip)) {
                            val isRouterHost = (ip == gatewayIp || hostNum == 1 || hostNum == 254)
                            val foundDev = probeSpecificHostReal(ip, isGateway = isRouterHost)
                            if (foundDev != null) {
                                val mac = arpMap[ip] ?: foundDev.mac
                                val finalBrand = if (isRouterHost && !foundDev.brand.contains("راوتر")) {
                                    "راوتر الشبكة / البوابة ($ip)"
                                } else {
                                    resolveDeviceBrand(mac, foundDev.brand)
                                }
                                val updatedDev = foundDev.copy(brand = finalBrand, mac = mac)
                                synchronized(discoveredMap) {
                                    discoveredMap[ip] = updatedDev
                                }
                            }
                        }

                        synchronized(this@LanScanner) {
                            scannedCount++
                            val percent = 15 + (scannedCount * 83 / totalIps)
                            val foundCount = discoveredMap.size
                            onProgress(
                                percent.coerceIn(15, 98),
                                "فحص $ip (تم العثور على $foundCount أجهزة)..."
                            )
                        }
                    }
                }
            }
            scanJobs.awaitAll()
        }

        onProgress(100, "اكتمل الفحص: تم العثور على ${discoveredMap.size} أجهزة نشطة (راوتر وكاميرات).")

        // Return devices sorted: Router / Gateway first, then cameras, then other IPs
        discoveredMap.values.sortedWith(
            compareBy<DiscoveredDevice> { !it.brand.contains("راوتر") }
                .thenBy { it.protocol != CameraProtocol.RTSP && it.protocol != CameraProtocol.ONVIF }
                .thenBy { it.ip }
        )
    }

    /**
     * Probes an IP by attempting fast TCP connects on common camera & router ports.
     */
    private fun probeSpecificHostReal(ip: String, isGateway: Boolean): DiscoveredDevice? {
        // Test ports in priority order
        for ((port, protocol) in PROBE_PORTS) {
            var socket: Socket? = null
            try {
                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 220)
                // Connected successfully!
                val brand = when {
                    isGateway -> "راوتر الشبكة / البوابة الرئيسية ($ip)"
                    port == 554 -> "كاميرا بث RTSP ($ip)"
                    port == 8000 -> "كاميرا Hikvision / ONVIF ($ip)"
                    port == 37777 -> "جهاز تسجيل Dahua DVR / NVR ($ip)"
                    port == 8899 -> "كاميرا ONVIF سلكية/لاسلكية ($ip)"
                    port == 80 -> "كاميرا IP / واجهة ويب ($ip)"
                    port == 8080 -> "بث كاميرا IP MJPEG ($ip)"
                    port == 443 || port == 8443 -> "كاميرا آمنة / جهاز ويب ($ip)"
                    else -> "جهاز شبكة نشط ($ip)"
                }
                return DiscoveredDevice(
                    ip = ip,
                    port = port,
                    protocol = protocol,
                    brand = brand,
                    mac = "نشط على المنفذ $port",
                    onvifServiceUrl = if (protocol == CameraProtocol.ONVIF) "http://$ip:$port/onvif/device_service" else ""
                )
            } catch (e: Exception) {
            } finally {
                try { socket?.close() } catch (e: Exception) {}
            }
        }

        // Quick fallback: ICMP ping / reachability
        try {
            val addr = InetAddress.getByName(ip)
            if (addr.isReachable(250)) {
                val label = if (isGateway) "راوتر الشبكة / البوابة ($ip)" else "جهاز شبكة متصل بالراوتر ($ip)"
                return DiscoveredDevice(
                    ip = ip,
                    port = 80,
                    protocol = CameraProtocol.HTTP_MJPEG,
                    brand = label,
                    mac = "متصل",
                    onvifServiceUrl = ""
                )
            }
        } catch (e: Exception) {
        }

        return null
    }

    /**
     * Real UPnP SSDP Discovery (UDP 1900) to find routers (TP-Link, Huawei, Asus, etc.) and smart devices.
     */
    private fun discoverSsdpMulticastReal(
        context: Context?,
        subnetPrefix: String,
        gatewayIp: String
    ): List<DiscoveredDevice> {
        val results = mutableListOf<DiscoveredDevice>()
        var wifiLock: WifiManager.MulticastLock? = null
        var socket: DatagramSocket? = null

        try {
            if (context != null) {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiLock = wifi?.createMulticastLock("ssdp_discovery")
                wifiLock?.setReferenceCounted(true)
                wifiLock?.acquire()
            }

            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 1800

            val ssdpMsg = "M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 2\r\n" +
                    "ST: ssdp:all\r\n\r\n"

            val bytes = ssdpMsg.toByteArray(Charsets.UTF_8)

            // Multicast
            try {
                val mPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName("239.255.255.250"), 1900)
                socket.send(mPacket)
            } catch (e: Exception) {}

            // Broadcast
            try {
                val bPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName("$subnetPrefix.255"), 1900)
                socket.send(bPacket)
            } catch (e: Exception) {}

            val recvBuffer = ByteArray(4096)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 1800) {
                try {
                    val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                    socket.receive(recvPacket)
                    val responseStr = String(recvPacket.data, 0, recvPacket.length, Charsets.UTF_8)
                    val senderIp = recvPacket.address.hostAddress ?: continue

                    var brand = if (senderIp == gatewayIp || senderIp.endsWith(".1")) {
                        "راوتر الشبكة / البوابة ($senderIp)"
                    } else {
                        "جهاز شبكة IP ($senderIp)"
                    }

                    // Extract SERVER header if present
                    val serverMatch = Pattern.compile("(?i)SERVER:\\s*([^\r\n]+)").matcher(responseStr)
                    if (serverMatch.find()) {
                        val serverVal = serverMatch.group(1)?.trim() ?: ""
                        if (serverVal.isNotEmpty()) {
                            brand = when {
                                senderIp == gatewayIp || senderIp.endsWith(".1") -> "راوتر الشبكة ($serverVal)"
                                serverVal.contains("cam", ignoreCase = true) || serverVal.contains("dvr", ignoreCase = true) -> "كاميرا IP ($serverVal)"
                                else -> "جهاز شبكة ($serverVal)"
                            }
                        }
                    }

                    results.add(
                        DiscoveredDevice(
                            ip = senderIp,
                            port = 80,
                            protocol = CameraProtocol.HTTP_MJPEG,
                            brand = brand,
                            mac = "UPnP / SSDP",
                            onvifServiceUrl = ""
                        )
                    )
                } catch (e: java.net.SocketTimeoutException) {
                    break
                } catch (e: Exception) {
                    break
                }
            }
        } catch (e: Exception) {
        } finally {
            try { socket?.close() } catch (e: Exception) {}
            try { if (wifiLock?.isHeld == true) wifiLock.release() } catch (e: Exception) {}
        }

        return results
    }

    /**
     * Real UDP Probe to find ONVIF Profile S IP cameras (WS-Discovery on 3702).
     */
    private fun discoverOnvifMulticastReal(context: Context?, subnetPrefix: String): List<DiscoveredDevice> {
        val results = mutableListOf<DiscoveredDevice>()
        var wifiLock: WifiManager.MulticastLock? = null
        var socket: DatagramSocket? = null

        try {
            if (context != null) {
                val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiLock = wifi?.createMulticastLock("onvif_discovery")
                wifiLock?.setReferenceCounted(true)
                wifiLock?.acquire()
            }

            socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = 2000

            val uuid = UUID.randomUUID().toString()
            val probeXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <Envelope xmlns="http://www.w3.org/2003/05/soap-envelope" xmlns:dn="http://www.onvif.org/ver10/network/wsdl">
                  <Header>
                    <wsa:MessageID xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">uuid:$uuid</wsa:MessageID>
                    <wsa:To xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>
                    <wsa:Action xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>
                  </Header>
                  <Body>
                    <dn:Probe>
                      <dn:Types>dn:NetworkVideoTransmitter</dn:Types>
                    </dn:Probe>
                  </Body>
                </Envelope>
            """.trimIndent()

            val bytes = probeXml.toByteArray(Charsets.UTF_8)

            // Multicast to 239.255.255.250:3702
            try {
                val multicastPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName("239.255.255.250"), 3702)
                socket.send(multicastPacket)
            } catch (e: Exception) {}

            // Subnet broadcast
            try {
                val broadcastPacket = DatagramPacket(bytes, bytes.size, InetAddress.getByName("$subnetPrefix.255"), 3702)
                socket.send(broadcastPacket)
            } catch (e: Exception) {}

            val recvBuffer = ByteArray(8192)
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 2000) {
                try {
                    val recvPacket = DatagramPacket(recvBuffer, recvBuffer.size)
                    socket.receive(recvPacket)
                    val responseStr = String(recvPacket.data, 0, recvPacket.length, Charsets.UTF_8)
                    val senderIp = recvPacket.address.hostAddress ?: continue

                    val xAddrsMatch = Pattern.compile("<(?:[^:]+:)?XAddrs>([^<]+)</(?:[^:]+:)?XAddrs>").matcher(responseStr)
                    val serviceUrl = if (xAddrsMatch.find()) {
                        xAddrsMatch.group(1)?.split(" ")?.firstOrNull() ?: "http://$senderIp:80/onvif/device_service"
                    } else {
                        "http://$senderIp:80/onvif/device_service"
                    }

                    val port = try {
                        val uri = java.net.URI(serviceUrl)
                        if (uri.port > 0) uri.port else 80
                    } catch (e: Exception) {
                        80
                    }

                    results.add(
                        DiscoveredDevice(
                            ip = senderIp,
                            port = port,
                            protocol = CameraProtocol.ONVIF,
                            brand = "كاميرا ONVIF Profile S ($senderIp)",
                            mac = "استجابة بث حقيقي",
                            onvifServiceUrl = serviceUrl
                        )
                    )
                } catch (e: java.net.SocketTimeoutException) {
                    break
                } catch (e: Exception) {
                    break
                }
            }
        } catch (e: Exception) {
        } finally {
            try { socket?.close() } catch (e: Exception) {}
            try { if (wifiLock?.isHeld == true) wifiLock.release() } catch (e: Exception) {}
        }

        return results
    }

    /**
     * Reads /proc/net/arp to resolve device MAC address from hardware ARP cache.
     */
    private fun readSystemArpTable(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val tokens = line?.trim()?.split("\\s+".toRegex()) ?: continue
                    if (tokens.size >= 4 && !tokens[0].equals("IP", ignoreCase = true)) {
                        val ip = tokens[0]
                        val mac = tokens[3]
                        if (mac != "00:00:00:00:00:00") {
                            map[ip] = mac.uppercase()
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
        return map
    }

    private fun resolveDeviceBrand(mac: String, defaultLabel: String): String {
        val cleanMac = mac.replace(":", "").uppercase()
        return when {
            cleanMac.startsWith("4419B6") || cleanMac.startsWith("B4A382") || cleanMac.startsWith("C056E3") ->
                "Hikvision IP Camera"
            cleanMac.startsWith("3CEF8C") || cleanMac.startsWith("E0508B") || cleanMac.startsWith("9002A9") ->
                "Dahua Technology Cam"
            cleanMac.startsWith("00408C") || cleanMac.startsWith("ACCC8E") ->
                "Axis Network Camera"
            cleanMac.startsWith("98254A") || cleanMac.startsWith("D84732") || cleanMac.startsWith("54AF97") ->
                "TP-Link Tapo / Router"
            cleanMac.startsWith("0080F0") || cleanMac.startsWith("001217") ->
                "Panasonic Security Camera"
            cleanMac.startsWith("B827EB") || cleanMac.startsWith("DCA632") ->
                "Raspberry Pi IP Webcam"
            else -> defaultLabel
        }
    }
}
