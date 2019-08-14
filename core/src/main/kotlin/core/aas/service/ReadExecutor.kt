package core.aas.service

import com.fasterxml.jackson.annotation.JsonProperty
import core.conf.security.EasyUser
import core.db.Executor
import mu.KotlinLogging
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}


@RestController
@RequestMapping("/v2")
class ReadExecutorCont {

    data class Resp(
            @JsonProperty("id") val id: String,
            @JsonProperty("name") val name: String,
            @JsonProperty("base_url") val baseUrl: String,
            @JsonProperty("load") val load: Int,
            @JsonProperty("max_load") val maxLoad: Int)

    @Secured("ROLE_TEACHER", "ROLE_ADMIN")
    @GetMapping("/executors")
    fun controller(caller: EasyUser): List<Resp> {
        log.debug { "Getting executors for ${caller.id}" }
        return selectAllExecutors()
    }
}


private fun selectAllExecutors(): List<ReadExecutorCont.Resp> {
    return transaction {
        Executor.selectAll().sortedBy { Executor.id }
                .map {
                    ReadExecutorCont.Resp(
                            it[Executor.id].value.toString(),
                            it[Executor.name],
                            it[Executor.baseUrl],
                            it[Executor.load],
                            it[Executor.maxLoad]
                    )
                }
    }
}