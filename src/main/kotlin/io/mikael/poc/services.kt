package io.mikael.poc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.util.*

@Service
open class ColorRepository
    @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    private val ORANGE = RGB(255, 165, 0).toXyz().toLab()

    private val query = """
        SELECT id, colorlist
          FROM visitors
         WHERE colorlist IS NOT NULL
           AND LENGTH(colorlist) > 0
    """

    fun getRows(): List<ColorRow> {
        val ret = ArrayList<ColorRow>()
        jdbcTemplate.query(query) { rs: ResultSet, rowNum: Int ->
            val id = rs.getInt("id")
            rs.getString("colorlist").split(",")
                    .filter { it.length > 0 }
                    .map { RGB(it.toInt()) }
                    .forEach {
                        val delta = LAB.ciede2000(ORANGE, it.toXyz().toLab())
                        ret.add(ColorRow(id, it.toString(), closeEnough(delta), delta2000 = delta.toInt()))
                    }
        }
        return ret
    }

    private fun closeEnough(delta: Double) = delta <= 20.0

}
