package pages.exercise

import components.code_editor.CodeEditorComp
import onSingleClickWithDisabled
import org.w3c.dom.HTMLButtonElement
import rip.kspar.ezspa.Component
import rip.kspar.ezspa.doInPromise
import rip.kspar.ezspa.getElemByIdAs
import tmRender
import kotlin.js.Promise


class AutoAssessmentTabComp(
    private val exercise: ExerciseDTO,
    private val onSaveUpdatedExercise: suspend (exercise: ExerciseDTO) -> Unit,
    parent: Component?
) : Component(parent) {

    companion object {
        const val GRADING_SCRIPT_FILENAME = "evaluate.sh"
    }

    private lateinit var editor: CodeEditorComp

    override val children: List<Component>
        get() = listOf(editor)

    override fun create(): Promise<*> = doInPromise {
        val gradingScript = CodeEditorComp.File(
            GRADING_SCRIPT_FILENAME, exercise.grading_script, "shell", CodeEditorComp.Edit.TOGGLED
        )
        val assets = exercise.assets.orEmpty().sortedBy { it.file_name }.map {
            CodeEditorComp.File(it.file_name, it.file_content, "python", CodeEditorComp.Edit.TOGGLED)
        }
        editor = CodeEditorComp(
            listOf(gradingScript) + assets,
            fileCreator = CodeEditorComp.CreateFile("python", CodeEditorComp.Edit.TOGGLED), parent = this
        )
    }

    override fun render(): String = tmRender(
        "t-c-exercise-tab-aa",
        "aaLabel" to "Automaatkontroll",
        "editorDstId" to editor.dstId,
        "doUpdateLabel" to "Salvesta"
    )

    override fun postRender() {
        getElemByIdAs<HTMLButtonElement>("update-submit-aa").onSingleClickWithDisabled("Salvestan...") {
            val newScripts = editor.getAllFiles().map { AssetDTO(it.name, it.content.orEmpty()) }
            val newGradingScript = newScripts.single { it.file_name == GRADING_SCRIPT_FILENAME }
            exercise.grading_script = newGradingScript.file_content
            exercise.assets = newScripts - newGradingScript
            onSaveUpdatedExercise(exercise)
        }
    }

    fun getEditorActiveTabId() = editor.getActiveTabFilename()
    fun setEditorActiveTabId(editorTabId: String) = editor.setActiveTabByFilename(editorTabId)
}
