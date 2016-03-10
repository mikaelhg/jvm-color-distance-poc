package io.mikael.poc

data class ColorRow(val id: Int, val rgb: String, val match: Boolean,
                    val delta1976: Int = 0, val delta1994: Int = 0, val delta2000: Int = 0)

