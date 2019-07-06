package ee.urgas.ems.bl.exercise

import com.fasterxml.jackson.annotation.JsonProperty
import ee.urgas.ems.conf.security.EasyUser
import ee.urgas.ems.db.Exercise
import ee.urgas.ems.db.ExerciseVer
import ee.urgas.ems.db.GraderType
import ee.urgas.ems.db.Teacher
import mu.KotlinLogging
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/v2")
class TeacherCreateExerciseController {

    data class NewExerciseBody(@JsonProperty("title", required = true) val title: String,
                               @JsonProperty("text_html", required = true) val textHtml: String,
                               @JsonProperty("public", required = true) val public: Boolean,
                               @JsonProperty("grader_type", required = true) val graderType: GraderType,
                               @JsonProperty("aas_id", required = false) val aasId: String?)

    data class CreatedExerciseResponse(@JsonProperty("id") val id: String)

    @Secured("ROLE_TEACHER", "ROLE_ADMIN")
    @PostMapping("/teacher/exercises")
    fun createExercise(@RequestBody dto: NewExerciseBody, caller: EasyUser): CreatedExerciseResponse {

        log.debug { "Create exercise '${dto.title}' by ${caller.id}" }
        return CreatedExerciseResponse(insertExercise(caller.id, dto).toString())
    }
}


private fun insertExercise(ownerId: String, body: TeacherCreateExerciseController.NewExerciseBody): Long {
    val teacherId = EntityID(ownerId, Teacher)

    return transaction {
        val exerciseId =
                Exercise.insertAndGetId {
                    it[owner] = teacherId
                    it[public] = body.public
                    it[createdAt] = DateTime.now()
                }

        ExerciseVer.insert {
            it[exercise] = exerciseId
            it[author] = teacherId
            it[validFrom] = DateTime.now()
            it[graderType] = body.graderType
            it[aasId] = body.aasId
            it[title] = body.title
            it[textHtml] = body.textHtml
        }
        exerciseId.value
    }
}
