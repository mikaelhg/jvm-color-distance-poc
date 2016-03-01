package io.mikael.poc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import javax.sql.DataSource

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

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
