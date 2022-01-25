package pages.participants

import Icons
import components.EzCollComp
import components.modal.ConfirmationTextModalComp
import debug
import kotlinx.coroutines.await
import plainDstStr
import rip.kspar.ezspa.Component
import rip.kspar.ezspa.doInPromise
import rip.kspar.ezspa.sleep

// buildList is experimental
@ExperimentalStdlibApi
class ParticipantsStudentsListComp(
    private val students: List<ParticipantsRootComp.Student>,
    private val studentsPending: List<ParticipantsRootComp.PendingStudent>,
    private val studentsMoodlePending: List<ParticipantsRootComp.PendingMoodleStudent>,
    private val isEditable: Boolean,
    parent: Component?
) : Component(parent) {

    data class StudentProps(
        val firstName: String?, val lastName: String?,
        val email: String, val username: String?, val utUsername: String?,
        val isActive: Boolean, val groups: List<String>
    )

    private lateinit var studentsColl: EzCollComp<StudentProps>

    private lateinit var removeFromCourseModal: ConfirmationTextModalComp

    override val children: List<Component>
        get() = listOf(studentsColl, removeFromCourseModal)

    override fun create() = doInPromise {

        val activeStudentProps = students.map {
            StudentProps(
                it.given_name,
                it.family_name,
                it.email,
                it.id,
                it.moodle_username,
                true,
                it.groups.map { it.name })
        }
        val pendingStudentProps = studentsPending.map {
            StudentProps(null, null, it.email, null, null, false, it.groups.map { it.name })
        }
        val moodlePendingStudentProps = studentsMoodlePending.map {
            StudentProps(null, null, it.email, null, it.moodle_username, false, it.groups.map { it.name })
        }

        val studentProps = activeStudentProps + pendingStudentProps + moodlePendingStudentProps

        val allGroups: List<String> = studentProps.flatMap { it.groups }.distinct().sorted()
        val hasGroups = allGroups.isNotEmpty()

        val items = studentProps.map { p ->
            EzCollComp.Item(
                p,
                EzCollComp.ItemTypeIcon(if (p.isActive) Icons.user else Icons.pending),
                if (p.isActive) "${p.firstName} ${p.lastName}" else "(Kutse ootel)",
                if (p.isActive) EzCollComp.TitleStatus.NORMAL else EzCollComp.TitleStatus.INACTIVE,
                topAttr = if (hasGroups) EzCollComp.ListAttr(
                    "Rühmad",
                    p.groups.map { EzCollComp.ListAttrItem(it) }.toMutableList(),
                    Icons.groups,
                    onClick = if (isEditable) ::changeGroups else null
                ) else null,
                bottomAttrs = buildList<EzCollComp.Attr<StudentProps>> {
                    add(EzCollComp.SimpleAttr("Email", p.email, Icons.email))
                    p.username?.let { add(EzCollComp.SimpleAttr("Kasutajanimi", p.username, Icons.user)) }
                    p.utUsername?.let { add(EzCollComp.SimpleAttr("UT kasutajanimi", p.utUsername, Icons.utUser)) }
                },
                isSelectable = isEditable,
                actions = if (isEditable) listOf(
                    EzCollComp.Action(Icons.groups, "Rühmad...", ::changeGroups),
                    EzCollComp.Action(Icons.removeParticipant, "Eemalda kursuselt", ::removeFromCourse),
                ) else emptyList(),
            )
        }

        studentsColl = EzCollComp(
            items,
            EzCollComp.Strings("õpilane", "õpilast"),
            if (isEditable) listOf(
                EzCollComp.MassAction(Icons.removeParticipant, "Eemalda kursuselt", ::removeFromCourse)
            ) else emptyList(),
            buildList {
                add(
                    EzCollComp.FilterGroup<StudentProps>(
                        "Staatus", listOf(
                            EzCollComp.Filter("Aktiivne", { it.props.isActive }),
                            EzCollComp.Filter("Ootel", { !it.props.isActive }),
                        )
                    )
                )
                if (hasGroups)
                    add(
                        EzCollComp.FilterGroup<StudentProps>(
                            "Rühm",
                            allGroups.map { g ->
                                EzCollComp.Filter(g, { it.props.groups.contains(g) })
                            }
                        )
                    )
            },
            buildList {
                if (hasGroups)
                    add(
                        EzCollComp.Sorter("Rühma ja nime järgi",
                            compareBy<EzCollComp.Item<StudentProps>> { it.props.groups.getOrNull(0) }
                                .thenBy { it.props.groups.getOrNull(1) }
                                .thenBy { it.props.groups.getOrNull(2) }
                                .thenBy { it.props.groups.getOrNull(3) }
                                .thenBy { it.props.groups.getOrNull(4) }
                                .thenBy { it.props.lastName?.lowercase() ?: it.props.email.lowercase() }
                                .thenBy { it.props.firstName?.lowercase() })
                    )
                add(
                    EzCollComp.Sorter("Nime järgi",
                        compareBy<EzCollComp.Item<StudentProps>> {
                            it.props.lastName?.lowercase() ?: it.props.email.lowercase()
                        }
                            .thenBy { it.props.firstName?.lowercase() }
                    )
                )
            },
            parent = this
        )

        removeFromCourseModal = ConfirmationTextModalComp(
            null, "Eemalda", "Tühista", "Eemaldan...",
            parent = this
        )
    }


    override fun render() = plainDstStr(studentsColl.dstId, removeFromCourseModal.dstId)

    override fun postRender() {
        super.postRender()
    }

    override fun renderLoading() = "Laen õpilasi..."

    override fun postChildrenBuilt() {
    }

    private var groupIdx = 1
    private suspend fun changeGroups(item: EzCollComp.Item<StudentProps>): EzCollComp.Result {
        // TODO: if hasGroups changed i.e. first student is added to a group then should createAndBuild this

        return if (item.topAttr != null && item.topAttr is EzCollComp.ListAttr<*, *>) {
            val groupAttr = item.topAttr.unsafeCast<EzCollComp.ListAttr<StudentProps, String>>()
            groupAttr.items.add(EzCollComp.ListAttrItem("Rühm ${groupIdx++}"))
            EzCollComp.ResultModified(listOf(item))

        } else {
            val groupAttr = EzCollComp.ListAttr(
                "Rühmad",
                mutableListOf(EzCollComp.ListAttrItem("Rühm ${groupIdx++}")),
                Icons.groups,
                onClick = if (isEditable) ::changeGroups else null
            )
            EzCollComp.ResultModified(listOf(item.copy(topAttr = groupAttr)))
        }
    }

    private suspend fun removeFromCourse(item: EzCollComp.Item<StudentProps>): EzCollComp.Result =
        removeFromCourse(listOf(item))

    private suspend fun removeFromCourse(items: List<EzCollComp.Item<StudentProps>>): EzCollComp.Result {
        debug { "Removing ${items.map { it.title }}" }

        removeFromCourseModal.setText("Eemalda ${items.size} õpilast?")
        removeFromCourseModal.setPrimaryAction {
            sleep(2000).await()
            true
        }

        debug { "opening modal" }
        val removeConfirmed = removeFromCourseModal.openWithClosePromise().await()
        debug { "modal await complete" }
        debug { "ret = $removeConfirmed" }

        return if (removeConfirmed)
            EzCollComp.ResultModified<StudentProps>(emptyList())
        else
            EzCollComp.ResultUnmodified
    }
}

