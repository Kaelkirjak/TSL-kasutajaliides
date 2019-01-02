package ee.urgas.aas.dao

import org.jetbrains.exposed.dao.LongIdTable


object Exercise : LongIdTable() {
    val ownerEmail = text("owner_email")
    val gradingScript = text("grading_script")
    val containerImage = text("container_image")
    val maxTime = integer("max_time_sec")
    val maxMem = integer("max_mem_mb")
}

object Asset : LongIdTable() {
    val exercise = reference("exercise_id", Exercise)
    val fileName = text("file_name")
    val fileContent = text("file_content")
}

object Executor : LongIdTable() {
    val name = text("name")
    val baseUrl = text("base_url")
    val load = integer("load")
    val maxLoad = integer("max_load")
}

object ExerciseExecutor : LongIdTable() {
    val exercise = reference("exercise_id", Exercise)
    val executor = reference("executor_id", Executor)
}