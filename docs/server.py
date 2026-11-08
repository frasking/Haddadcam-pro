import http.server
import socketserver
import os

base_dir = os.path.dirname(os.path.abspath(__file__))
os.chdir(base_dir)
class CustomHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache, no-store, must-revalidate')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        super().end_headers()

CustomHandler.extensions_map['.apk'] = 'application/vnd.android.package-archive'

socketserver.ThreadingTCPServer.allow_reuse_address = True
with socketserver.ThreadingTCPServer(('0.0.0.0', 3000), CustomHandler) as httpd:
    print("Serving on 3000")
    httpd.serve_forever()
