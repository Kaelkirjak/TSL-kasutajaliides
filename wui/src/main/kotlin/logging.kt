import kotlin.js.Date

private const val DEBUG_PREFIX = "[DEBUG]"
private const val WARN_PREFIX = "[WARN]"

private const val DEBUG_ENABLED = true
private const val WARN_ENABLED = true


// No console.debug in kotlin/js...
fun debug(msgProvider: () -> String) {
    if (DEBUG_ENABLED)
        console.log("$DEBUG_PREFIX ${datetimeString()}: ${msgProvider()}")
}

fun warn(msgProvider: () -> String) {
    if (WARN_ENABLED)
        console.warn("$WARN_PREFIX ${datetimeString()}: ${msgProvider()}")
}


class FunLog(private val funName: String, private val funStartTime: Double) {
    fun end() {
        debug { "<-- ${this.funName} (took ${Date.now() - this.funStartTime} ms)" }
    }
}

fun debugFunStart(funName: String): FunLog? {
    if (DEBUG_ENABLED) {
        debug { "--> $funName" }
        return FunLog(funName, Date.now())
    }
    return null
}


private fun datetimeString(): String {
    return Date().toISOString().replace("T", " ").replace("Z", "")
}