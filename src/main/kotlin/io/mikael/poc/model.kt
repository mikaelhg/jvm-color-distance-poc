package io.mikael.poc

data class ColorRow(val id: Int, val rgb: String, val match: Boolean,
                    val delta1976: Int = 0, val delta1994: Int = 0, val delta2000: Int = 0)

data class RGB(val r: Int, val g: Int, val b: Int, private val binSize : Double = 1.0) {

    constructor(rgb: Int) : this((rgb and 0xFF0000) shr 16, (rgb and 0x00FF00) shr 8, rgb and 0xFF)

    override fun toString() = String.format("#%02x%02x%02x", r, g, b)

    fun toInt() : Int = 0xFF0000 and (r shl 16) or (0x00FF00 and (g shl 8)) or (0xFF and b)

    fun toXyz() : XYZ {
        // first, normalize RGB values
        var r2 = r / 255.0
        var g2 = g / 255.0
        var b2 = b / 255.0

        // D65 standard referent
        val X = 0.950470
        val Y = 1.0
        val Z = 1.088830

        // second, map sRGB to CIE XYZ
        r2 = if (r2 <= 0.04045) r2 / 12.92 else Math.pow((r2 + 0.055) / 1.055, 2.4)
        g2 = if (g2 <= 0.04045) g2 / 12.92 else Math.pow((g2 + 0.055) / 1.055, 2.4)
        b2 = if (b2 <= 0.04045) b2 / 12.92 else Math.pow((b2 + 0.055) / 1.055, 2.4)

        var x = (0.4124564 * r2 + 0.3575761 * g2 + 0.1804375 * b2) / X
        var y = (0.2126729 * r2 + 0.7151522 * g2 + 0.0721750 * b2) / Y
        var z = (0.0193339 * r2 + 0.1191920 * g2 + 0.9503041 * b2) / Z

        // third, map CIE XYZ to CIE L*a*b* and return
        x = if (x > 0.008856) Math.pow(x, 1.0 / 3) else 7.787037 * x + 4.0 / 29
        y = if (y > 0.008856) Math.pow(y, 1.0 / 3) else 7.787037 * y + 4.0 / 29
        z = if (z > 0.008856) Math.pow(z, 1.0 / 3) else 7.787037 * z + 4.0 / 29

        return XYZ(x, y, z, binSize)
    }

}

data class XYZ(val x: Double, val y: Double, val z: Double, private val binSize : Double = 1.0) {

    fun toLab() : LAB = toLabGeneral(Math::floor)

    fun toLabRounding() : LAB = toLabGeneral { Math.round(it).toDouble() }

    fun toLabGeneral(method: (Double) -> Double) : LAB {
        var L = 116 * y - 16
        var A = 500 * (x - y)
        var B = 200 * (y - z)

        if (binSize > 0) {
            L = binSize * method(L / binSize)
            A = binSize * method(A / binSize)
            B = binSize * method(B / binSize)
        }

        return LAB(L, A, B)
    }

}
