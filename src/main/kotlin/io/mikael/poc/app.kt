package io.mikael.poc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

data class ColorRow(val id: Int, val rgb: String, val match: Boolean, val delta1976: Int, val delta1994: Int, val delta2000: Int)

data class RGB(val r: Int, val g: Int, val b: Int) {
    private val binSize = 1.0

    constructor(rgb: Int) : this((rgb and 0xFF0000) shr 16, (rgb and 0x00FF00) shr 8, rgb and 0xFF)

    override fun toString() = String.format("#%02x%02x%02x", r, g, b)

    fun toLab() = LAB.fromRGB(r, g, b, binSize)
}

@SpringBootApplication
open class Application {

    @Bean
    open fun jdbcTemplate(dataSource: DataSource) = JdbcTemplate(dataSource)

}

@Controller
open class ColorController
    @Autowired constructor(val colorRepository: ColorRepository) {

    @RequestMapping("/")
    fun index() = ModelAndView("index", mapOf("rows" to colorRepository.getRows()))

}

@Service
open class ColorRepository
    @Autowired constructor(val jdbcTemplate: JdbcTemplate) {

    private val binSize = 1.0

    private val ORANGE = RGB(255, 165, 0).toLab()

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
                    .map(Integer::parseInt)
                    .map { RGB(it) }
                    .forEach {
                        val delta = LAB.ciede2000(ORANGE, it.toLab())
                        ret.add(ColorRow(id, it.toString(), delta <= 20.0, delta.toInt(), 0, 0))
                    }
        }
        return ret
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
