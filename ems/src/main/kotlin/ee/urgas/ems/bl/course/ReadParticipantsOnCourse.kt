package ee.urgas.ems.bl.course

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import ee.urgas.ems.bl.access.assertTeacherOrAdminHasAccessToCourse
import ee.urgas.ems.bl.idToLongOrInvalidReq
import ee.urgas.ems.conf.security.EasyUser
import ee.urgas.ems.db.*
import ee.urgas.ems.exception.InvalidRequestException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/v2")
class ReadParticipantsOnCourseController {

    data class StudentsOnCourseResponse(@JsonProperty("id") val id: String,
                                        @JsonProperty("email") val email: String,
                                        @JsonProperty("given_name") val givenName: String,
                                        @JsonProperty("family_name") val familyName: String)

    data class TeachersOnCourseResponse(@JsonProperty("id") val id: String,
                                        @JsonProperty("email") val email: String,
                                        @JsonProperty("given_name") val givenName: String,
                                        @JsonProperty("family_name") val familyName: String)

    data class ParticipantOnCourseResponse(@JsonProperty("students")
                                           @JsonInclude(Include.NON_NULL) val students: List<StudentsOnCourseResponse>?,
                                           @JsonProperty("teachers")
                                           @JsonInclude(Include.NON_NULL) val teachers: List<TeachersOnCourseResponse>?)


    enum class Role(val paramValue: String) {
        TEACHER("teacher"),
        STUDENT("student"),
        ALL("all")
    }

    @Secured("ROLE_TEACHER", "ROLE_ADMIN")
    @GetMapping("/courses/{courseId}/participants")
    fun readStudentsOnCourse(@PathVariable("courseId") courseIdStr: String,
                             @RequestParam("role", required = false) roleReq: String?,
                             caller: EasyUser): ParticipantOnCourseResponse {

        log.debug { "Getting participants on course $courseIdStr for ${caller.id} with role $roleReq" }

        val courseId = courseIdStr.idToLongOrInvalidReq()

        assertTeacherOrAdminHasAccessToCourse(caller, courseId)

        when (roleReq) {
            Role.TEACHER.paramValue -> {
                val teachers = selectTeachersOnCourse(courseId)
                log.debug { "Teachers on course $courseId: $teachers" }
                return ParticipantOnCourseResponse(null, selectTeachersOnCourse(courseId))

            }

            Role.STUDENT.paramValue -> {
                val students = selectStudentsOnCourse(courseId)
                log.debug { "Students on course $courseId: $students" }
                return ParticipantOnCourseResponse(selectStudentsOnCourse(courseId), null)
            }

            Role.ALL.paramValue, null -> {
                val students = selectStudentsOnCourse(courseId)
                val teachers = selectTeachersOnCourse(courseId)

                log.debug { "Students on course $courseId: $students" }
                log.debug { "Teachers on course $courseId: $teachers" }

                return ParticipantOnCourseResponse(selectStudentsOnCourse(courseId), selectTeachersOnCourse(courseId))
            }

            else -> throw InvalidRequestException("Invalid parameter $roleReq")
        }
    }
}


private fun selectStudentsOnCourse(courseId: Long): List<ReadParticipantsOnCourseController.StudentsOnCourseResponse> {
    return transaction {
        (Student innerJoin StudentCourseAccess innerJoin Course)
                .slice(Student.id, Student.email, Student.givenName, Student.familyName)
                .select { Course.id eq courseId }
                .withDistinct()
                .map {
                    ReadParticipantsOnCourseController.StudentsOnCourseResponse(
                            it[Student.id].value,
                            it[Student.email],
                            it[Student.givenName],
                            it[Student.familyName]
                    )
                }
    }
}

private fun selectTeachersOnCourse(courseId: Long): List<ReadParticipantsOnCourseController.TeachersOnCourseResponse> {
    return transaction {
        (Teacher innerJoin TeacherCourseAccess innerJoin Course)
                .slice(Teacher.id, Teacher.email, Teacher.givenName, Teacher.familyName)
                .select { Course.id eq courseId }
                .withDistinct()
                .map {
                    ReadParticipantsOnCourseController.TeachersOnCourseResponse(
                            it[Teacher.id].value,
                            it[Teacher.email],
                            it[Teacher.givenName],
                            it[Teacher.familyName]
                    )
                }
    }
}