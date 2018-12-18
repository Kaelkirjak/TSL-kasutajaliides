package ee.urgas.ems

import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.sql.DataSource


@SpringBootApplication
@EnableScheduling
class EmsApplication {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }
}

@Component
class InitComponent(val dataSource: DataSource) {
    @PostConstruct
    fun init() {
        Database.connect(dataSource)
    }
}


fun main(args: Array<String>) {
    runApplication<EmsApplication>(*args)
}
