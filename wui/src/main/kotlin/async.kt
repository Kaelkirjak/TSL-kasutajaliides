import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise


fun <T> doInPromise(action: suspend () -> T): Promise<T> = MainScope().promise {
    action()
}


fun <T> Collection<Promise<T>>.unionPromise(): Promise<List<T>> =
        Promise.all(this.toTypedArray()).then { it.asList() }
