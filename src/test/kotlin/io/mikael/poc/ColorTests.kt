package io.mikael.poc

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

class ColorTests {

    @Test
    fun testLab() {

        LAB.fromRGB(1, 2, 3, 255.toDouble())

    }

}
