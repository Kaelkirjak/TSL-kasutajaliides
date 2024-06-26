package pages

import AppProperties
import rip.kspar.ezspa.getElemBySelector
import warn

object Title {

    private const val appName = AppProperties.AppName

    /* Formats:
        Lahendus
        Page title - Lahendus
        Parent title (e.g. course) - Lahendus
        Page title - Parent title (e.g. course) - Lahendus
     */

    data class Spec(
        var pageTitle: String? = null,
        var parentPageTitle: String? = null,
    )

    private var currentSpec: Spec = Spec(appName)

    fun replace(updater: (Spec) -> Spec) {
        currentSpec = updater(currentSpec)
        refresh()
    }

    fun update(updater: (Spec) -> Unit) {
        updater(currentSpec)
        refresh()
    }


    private fun refresh() {
        val title = getElemBySelector("title")
        if (title == null) {
            warn { "<title> element not found when refreshing title" }
            return
        }

        title.textContent = listOfNotNull(currentSpec.pageTitle, currentSpec.parentPageTitle, appName)
            .joinToString(" - ")
    }
}