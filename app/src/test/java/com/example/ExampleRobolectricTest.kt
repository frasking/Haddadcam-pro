package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun appNameResourceKeyTest() {
    val expectedName = "كام حداد برو"
    assertEquals("كام حداد برو", expectedName)
  }
}
