package io.mikael.poc

data class ColorRow(val id: Int, val rgb: String, val match: Boolean,
                    val delta1976: Int = 0, val delta1994: Int = 0, val delta2000: Int = 0)

data class RGB(val r: Int, val g: Int, val b: Int) {
    private val binSize = 1.0

    constructor(rgb: Int) : this((rgb and 0xFF0000) shr 16, (rgb and 0x00FF00) shr 8, rgb and 0xFF)

    override fun toString() = String.format("#%02x%02x%02x", r, g, b)

    fun toLab() = LAB.fromRGB(r, g, b, binSize)
}

