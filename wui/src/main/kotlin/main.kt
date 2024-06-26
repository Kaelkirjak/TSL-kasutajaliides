import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.dom.clear
import libheaders.CodeMirror
import libheaders.ContainerQueryPolyfill
import pages.ExerciseSummaryPage
import pages.Navbar
import pages.OldParticipantsPage
import pages.course_exercises.CourseExercisesPage
import pages.courses.CoursesPage
import pages.exercise.ExercisePage
import pages.exercise_library.ExerciseLibraryPage
import pages.grade_table.GradeTablePage
import pages.participants.ParticipantsPage
import pages.sidenav.Sidenav
import queries.*
import rip.kspar.ezspa.*


private val PAGES = listOf(
    CoursesPage, CourseExercisesPage, ExerciseSummaryPage, GradeTablePage,
    OldParticipantsPage, ParticipantsPage,
    ExerciseLibraryPage, ExercisePage
)

fun main() {
    val funLog = debugFunStart("main")

    // Start authentication as soon as possible
    doInPromise {
        setSplashText("Login sisse")
        initAuthentication()
        setSplashText("Uuendan andmeid")
        updateAccountData()
        buildStatics()
        EzSpa.PageManager.updatePage()
    }

    // Do stuff that does not require auth
    initApplication()
    EzSpa.Navigation.enableAnchorLinkInterception()
    EzSpa.Navigation.enableHistoryNavInterception()

    funLog?.end()
}

fun setSplashText(text: String) {
    getElemById("loading-splash-text").textContent = text
}

suspend fun buildStatics() {
    getElemById("loading-splash-container").clear()
    getHeader().innerHTML = """<div id="nav-wrap"></div>"""
    getMain().innerHTML = """<div id="sidenav-wrap"></div><div id="content-container" class="container"></div>"""
    Navbar.build()
    Sidenav.build()
}


private suspend fun updateAccountData() {
    val funLog = debugFunStart("updateAccountData")

    val firstName = Auth.firstName
    val lastName = Auth.lastName
    val email = Auth.email

    debug { "Updating account data to [email: $email, first name: $firstName, last name: $lastName]" }

    val personalData = mapOf("first_name" to firstName, "last_name" to lastName)

    fetchEms(
        "/account/checkin",
        ReqMethod.POST,
        personalData,
        successChecker = { http200 },
        errorHandler = {
            it.handleByCode(RespError.ACCOUNT_MIGRATION_FAILED) {
                permanentErrorMessage(false) { "Kasutaja andmeid uuendades tekkis viga. Administraatorit on veast teavitatud. Palun proovi mõne aja pärast uuesti." }
                error("Account migration failed")
            }
        },
        cancellable = false
    ).await()
    debug { "Account data updated" }

    funLog?.end()
}

private suspend fun initAuthentication() {
    val funLog = debugFunStart("initAuthentication")
    Auth.initialize().await()
    funLog?.end()
}

private fun initApplication() {
    EzSpa.PageManager.registerPages(PAGES)
    EzSpa.PageManager.preUpdateHook = ::abortAllFetchesAndClear
    EzSpa.PageManager.pageNotFoundHandler = ::handlePageNotFound

    EzSpa.Logger.logPrefix = "[EZ-SPA] "
    EzSpa.Logger.debugFunction = ::debug
    EzSpa.Logger.warnFunction = ::warn

    CodeMirror.modeURL = AppProperties.CM_MODE_URL_TEMPLATE

    loadContainerQueries()
}

private fun handlePageNotFound(@Suppress("UNUSED_PARAMETER") path: String) {
    getContainer().innerHTML = tmRender(
        "tm-broken-page", mapOf(
            "title" to Str.notFoundPageTitle(),
            "msg" to Str.notFoundPageMsg()
        )
    )
    Sidenav.refresh(Sidenav.Spec())
}

private fun loadContainerQueries() {
    val supportsContainerQueries = try {
         document.documentElement?.asDynamic().style.container != null
    } catch (e: Throwable) {
        false
    }

    if (!supportsContainerQueries) {
        debug { "Native container queries NOT supported, using polyfill :(" }
        // Just including the reference in code forces the module to be included/loaded
        ContainerQueryPolyfill
    } else {
        debug { "Native container queries supported :)" }
    }
}
