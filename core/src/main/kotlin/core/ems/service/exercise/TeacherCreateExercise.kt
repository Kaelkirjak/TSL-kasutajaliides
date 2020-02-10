package core.ems.service.exercise

import com.fasterxml.jackson.annotation.JsonProperty
import core.aas.insertAutoExercise
import core.conf.security.EasyUser
import core.db.*
import core.ems.service.AdocService
import core.ems.service.idToLongOrInvalidReq
import mu.KotlinLogging
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/v2")
class CreateExerciseCont(private val adocService: AdocService) {

    data class Req(@JsonProperty("title", required = true) @field:NotBlank @field:Size(max = 100) val title: String,
                   @JsonProperty("text_html", required = false) @field:Size(max = 300000) val textHtml: String?,
                   @JsonProperty("text_adoc", required = false) @field:Size(max = 300000) val textAdoc: String?,
                   @JsonProperty("public", required = true) val public: Boolean,
                   @JsonProperty("grader_type", required = true) val graderType: GraderType,
                   @JsonProperty("grading_script", required = false) val gradingScript: String?,
                   @JsonProperty("container_image", required = false) @field:Size(max = 2000) val containerImage: String?,
                   @JsonProperty("max_time_sec", required = false) val maxTime: Int?,
                   @JsonProperty("max_mem_mb", required = false) val maxMem: Int?,
                   @JsonProperty("assets", required = false) val assets: List<ReqAsset>?,
                   @JsonProperty("executors", required = false) val executors: List<ReqExecutor>?)

    data class ReqAsset(@JsonProperty("file_name", required = true) @field:Size(max = 100) val fileName: String,
                        @JsonProperty("file_content", required = true) @field:Size(max = 300000) val fileContent: String)

    data class ReqExecutor(@JsonProperty("executor_id", required = true) @field:Size(max = 100) val executorId: String)

    data class Resp(@JsonProperty("id") val id: String)

    @Secured("ROLE_TEACHER", "ROLE_ADMIN")
    @PostMapping("/exercises")
    fun controller(@Valid @RequestBody dto: Req, caller: EasyUser): Resp {

        log.debug { "Create exercise '${dto.title}' by ${caller.id}" }

        return when (dto.textAdoc) {
            null -> Resp(insertExercise(caller.id, dto, dto.textHtml).toString())
            else -> Resp(insertExercise(caller.id, dto, adocService.adocToHtml(dto.textAdoc)).toString())
        }
    }
}


private fun insertExercise(ownerId: String, req: CreateExerciseCont.Req, html: String?): Long {
    val teacherId = EntityID(ownerId, Teacher)

    return transaction {

        val newAutoExerciseId =
                if (req.graderType == GraderType.AUTO) {
                    insertAutoExercise(req.gradingScript, req.containerImage, req.maxTime, req.maxMem,
                            req.assets?.map { it.fileName to it.fileContent },
                            req.executors?.map { it.executorId.idToLongOrInvalidReq() })

                } else null

        val exerciseId = Exercise.insertAndGetId {
            it[owner] = teacherId
            it[public] = req.public
            it[createdAt] = DateTime.now()
        }

        ExerciseVer.insert {
            it[exercise] = exerciseId
            it[author] = teacherId
            it[validFrom] = DateTime.now()
            it[graderType] = req.graderType
            it[title] = req.title
            it[textHtml] = html
            it[textAdoc] = req.textAdoc
            it[autoExerciseId] = newAutoExerciseId
        }

        if (html != null) {
            val inUse = StoredFile.slice(StoredFile.id)
                    .select { StoredFile.usageConfirmed eq false }
                    .map { it[StoredFile.id].value }
                    .filter { html.contains(it) }

            StoredFile.update({ StoredFile.id inList inUse }) {
                it[StoredFile.usageConfirmed] = true
                it[StoredFile.exercise] = exerciseId
            }
        }

        exerciseId.value
    }
}

