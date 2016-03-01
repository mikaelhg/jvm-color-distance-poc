/*
Copyright (c) 2011, Stanford University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* The name Stanford University may not be used to endorse or promote products
  derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL STANFORD UNIVERSITY BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.mikael.poc

/**
 * From https://github.com/StanfordHCI/c3
 */
class LAB {

    var L: Double = 0.toDouble()
    var a: Double = 0.toDouble()
    var b: Double = 0.toDouble()
    var c = -1
    var w: DoubleArray? = null
    var s = -1.0

    constructor(L: Double, a: Double, b: Double) {
        this.L = L
        this.a = a
        this.b = b
    }

    private constructor(L: Double, a: Double, b: Double, c: Int) {
        this.L = L
        this.a = a
        this.b = b
        this.c = c
    }

    override fun toString() = "${L.toInt()},${a.toInt()},${b.toInt()}"

    override fun equals(other: Any?): Boolean {
        if (other is LAB) {
            return L == other.L && a == other.a && b == other.b
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        val x = L.toInt()
        val y = (a + 110).toInt()
        val z = (b + 110).toInt()
        return x shl 16 or (y shl 8) or z
    }

    fun copy(): LAB {
        val x = LAB(L, a, b, c)
        if (w != null) x.w = w!!.clone()
        return x
    }

    fun distance(y: LAB): Double {
        val dL = L - y.L
        val da = a - y.a
        val db = b - y.b
        return Math.sqrt(dL * dL + da * da + db * db)
    }

    fun rgb(): Int {
        // first, map CIE L*a*b* to CIE XYZ
        var y = (L + 16) / 116
        var x = y + a / 500
        var z = y - b / 200

        // D65 standard referent
        val X = 0.950470
        val Y = 1.0
        val Z = 1.088830

        x = X * if (x > 0.206893034) x * x * x else (x - 4.0 / 29) / 7.787037
        y = Y * if (y > 0.206893034) y * y * y else (y - 4.0 / 29) / 7.787037
        z = Z * if (z > 0.206893034) z * z * z else (z - 4.0 / 29) / 7.787037

        // second, map CIE XYZ to sRGB
        var r = 3.2404542 * x - 1.5371385 * y - 0.4985314 * z
        var g = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z
        var b = 0.0556434 * x - 0.2040259 * y + 1.0572252 * z
        r = if (r <= 0.00304) 12.92 * r else 1.055 * Math.pow(r, 1 / 2.4) - 0.055
        g = if (g <= 0.00304) 12.92 * g else 1.055 * Math.pow(g, 1 / 2.4) - 0.055
        b = if (b <= 0.00304) 12.92 * b else 1.055 * Math.pow(b, 1 / 2.4) - 0.055

        // third, get sRGB values
        var ir = Math.round(255 * r).toInt()
        ir = Math.max(0, Math.min(ir, 255))
        var ig = Math.round(255 * g).toInt()
        ig = Math.max(0, Math.min(ig, 255))
        var ib = Math.round(255 * b).toInt()
        ib = Math.max(0, Math.min(ib, 255))

        return 0xFF0000 and (ir shl 16) or (0x00FF00 and (ig shl 8)) or (0xFF and ib)
    }

    fun hex(): String {
        val rgb = this.rgb()
        val r = 0xFF and (rgb shr 16)
        val g = 0xFF and (rgb shr 8)
        val b = 0xFF and rgb
        var sr = Integer.toHexString(r)
        var sg = Integer.toHexString(g)
        var sb = Integer.toHexString(b)
        if (sr.length < 2) sr = "0" + sr
        if (sg.length < 2) sg = "0" + sg
        if (sb.length < 2) sb = "0" + sb
        return "#" + sr + sg + sb
    }

    companion object {

        /**
         * Maps an RGB triple to binned LAB space (D65).
         * Binning is done by *flooring* LAB values.
         */
        fun fromRGB(ri: Int, gi: Int, bi: Int, binSize: Double): LAB {
            // first, normalize RGB values
            var r = ri / 255.0
            var g = gi / 255.0
            var b = bi / 255.0

            // D65 standard referent
            val X = 0.950470
            val Y = 1.0
            val Z = 1.088830

            // second, map sRGB to CIE XYZ
            r = if (r <= 0.04045) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
            g = if (g <= 0.04045) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
            b = if (b <= 0.04045) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)
            var x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / X
            var y = (0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / Y
            var z = (0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / Z

            // third, map CIE XYZ to CIE L*a*b* and return
            x = if (x > 0.008856) Math.pow(x, 1.0 / 3) else 7.787037 * x + 4.0 / 29
            y = if (y > 0.008856) Math.pow(y, 1.0 / 3) else 7.787037 * y + 4.0 / 29
            z = if (z > 0.008856) Math.pow(z, 1.0 / 3) else 7.787037 * z + 4.0 / 29

            var L = 116 * y - 16
            var A = 500 * (x - y)
            var B = 200 * (y - z)

            if (binSize > 0) {
                L = binSize * Math.floor(L / binSize)
                A = binSize * Math.floor(A / binSize)
                B = binSize * Math.floor(B / binSize)
            }
            return LAB(L, A, B)
        }

        /**
         * Maps an RGB triple to binned LAB space (D65).
         * Binning is done by *rounding* LAB values.
         */
        fun fromRGBr(ri: Int, gi: Int, bi: Int, binSize: Double): LAB {
            // first, normalize RGB values
            var r = ri / 255.0
            var g = gi / 255.0
            var b = bi / 255.0

            // D65 standard referent
            val X = 0.950470
            val Y = 1.0
            val Z = 1.088830

            // second, map sRGB to CIE XYZ
            r = if (r <= 0.04045) r / 12.92 else Math.pow((r + 0.055) / 1.055, 2.4)
            g = if (g <= 0.04045) g / 12.92 else Math.pow((g + 0.055) / 1.055, 2.4)
            b = if (b <= 0.04045) b / 12.92 else Math.pow((b + 0.055) / 1.055, 2.4)

            var x = (0.4124564 * r + 0.3575761 * g + 0.1804375 * b) / X
            var y = (0.2126729 * r + 0.7151522 * g + 0.0721750 * b) / Y
            var z = (0.0193339 * r + 0.1191920 * g + 0.9503041 * b) / Z

            // third, map CIE XYZ to CIE L*a*b* and return
            x = if (x > 0.008856) Math.pow(x, 1.0 / 3) else 7.787037 * x + 4.0 / 29
            y = if (y > 0.008856) Math.pow(y, 1.0 / 3) else 7.787037 * y + 4.0 / 29
            z = if (z > 0.008856) Math.pow(z, 1.0 / 3) else 7.787037 * z + 4.0 / 29

            var L = 116 * y - 16
            var A = 500 * (x - y)
            var B = 200 * (y - z)

            if (binSize > 0) {
                L = binSize * Math.round(L / binSize)
                A = binSize * Math.round(A / binSize)
                B = binSize * Math.round(B / binSize)
            }

            return LAB(L, A, B)
        }

        fun isInRGBGamut(L: Double, A: Double, B: Double): Boolean {
            // first, map CIE L*a*b* to CIE XYZ
            var y = (L + 16) / 116
            var x = y + A / 500
            var z = y - B / 200

            // D65 standard referent
            val X = 0.950470
            val Y = 1.0
            val Z = 1.088830

            x = X * if (x > 0.206893034) x * x * x else (x - 4.0 / 29) / 7.787037
            y = Y * if (y > 0.206893034) y * y * y else (y - 4.0 / 29) / 7.787037
            z = Z * if (z > 0.206893034) z * z * z else (z - 4.0 / 29) / 7.787037

            // second, map CIE XYZ to sRGB
            var r = 3.2404542 * x - 1.5371385 * y - 0.4985314 * z
            var g = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z
            var b = 0.0556434 * x - 0.2040259 * y + 1.0572252 * z
            r = if (r <= 0.00304) 12.92 * r else 1.055 * Math.pow(r, 1 / 2.4) - 0.055
            g = if (g <= 0.00304) 12.92 * g else 1.055 * Math.pow(g, 1 / 2.4) - 0.055
            b = if (b <= 0.00304) 12.92 * b else 1.055 * Math.pow(b, 1 / 2.4) - 0.055

            // third, check sRGB values
            return !(r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1)
        }

        fun ciede2000(x: LAB, y: LAB): Double {
            // adapted from Sharma et al's MATLAB implementation at
            //  http://www.ece.rochester.edu/~gsharma/ciede2000/

            // parametric factors, use defaults
            val kl = 1.0
            val kc = 1.0
            val kh = 1.0

            // compute terms
            val pi = Math.PI
            val L1 = x.L
            val a1 = x.a
            val b1 = x.b
            val Cab1 = Math.sqrt(a1 * a1 + b1 * b1)
            val L2 = y.L
            val a2 = y.a
            val b2 = y.b
            val Cab2 = Math.sqrt(a2 * a2 + b2 * b2)
            val Cab = 0.5 * (Cab1 + Cab2)
            val G = 0.5 * (1 - Math.sqrt(Math.pow(Cab, 7.0) / (Math.pow(Cab, 7.0) + Math.pow(25.0, 7.0))))
            val ap1 = (1 + G) * a1
            val ap2 = (1 + G) * a2
            val Cp1 = Math.sqrt(ap1 * ap1 + b1 * b1)
            val Cp2 = Math.sqrt(ap2 * ap2 + b2 * b2)
            val Cpp = Cp1 * Cp2

            // ensure hue is between 0 and 2pi
            var hp1 = Math.atan2(b1, ap1)
            if (hp1 < 0) hp1 += 2 * pi
            var hp2 = Math.atan2(b2, ap2)
            if (hp2 < 0) hp2 += 2 * pi

            var dL = L2 - L1
            var dC = Cp2 - Cp1
            var dhp = hp2 - hp1

            if (dhp > +pi) dhp -= 2 * pi
            if (dhp < -pi) dhp += 2 * pi
            if (Cpp == 0.0) dhp = 0.0

            // Note that the defining equations actually need
            // signed Hue and chroma differences which is different
            // from prior color difference formulae
            var dH = 2.0 * Math.sqrt(Cpp) * Math.sin(dhp / 2)

            // Weighting functions
            val Lp = 0.5 * (L1 + L2)
            val Cp = 0.5 * (Cp1 + Cp2)

            // Average Hue Computation
            // This is equivalent to that in the paper but simpler programmatically.
            // Average hue is computed in radians and converted to degrees where needed
            var hp = 0.5 * (hp1 + hp2)
            // Identify positions for which abs hue diff exceeds 180 degrees
            if (Math.abs(hp1 - hp2) > pi) hp -= pi
            if (hp < 0) hp += 2 * pi

            // Check if one of the chroma values is zero, in which case set
            // mean hue to the sum which is equivalent to other value
            if (Cpp == 0.0) hp = hp1 + hp2

            val Lpm502 = (Lp - 50) * (Lp - 50)
            val Sl = 1 + 0.015 * Lpm502 / Math.sqrt(20 + Lpm502)
            val Sc = 1 + 0.045 * Cp
            val T = 1 - 0.17 * Math.cos(hp - pi / 6)
                + 0.24 * Math.cos(2 * hp)
                + 0.32 * Math.cos(3 * hp + pi / 30)
                - 0.20 * Math.cos(4 * hp - 63 * pi / 180)
            val Sh = 1 + 0.015 * Cp * T
            val ex = (180 / pi * hp - 275) / 25
            val delthetarad = 30 * pi / 180 * Math.exp(-1 * (ex * ex))
            val Rc = 2 * Math.sqrt(Math.pow(Cp, 7.0) / (Math.pow(Cp, 7.0) + Math.pow(25.0, 7.0)))
            val RT = -1.0 * Math.sin(2 * delthetarad) * Rc

            dL /= (kl * Sl)
            dC /= (kc * Sc)
            dH /= (kh * Sh)

            // The CIE 00 color difference
            return Math.sqrt(dL * dL + dC * dC + dH * dH + RT * dC * dH)
        }
    }
}
