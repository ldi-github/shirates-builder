package shirates.builder.utility

import javafx.application.Platform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun async(): Async {

    val a = Async()
    return a
}

class Async {
    private var progressStartAction: ((Async) -> Unit)? = null
    private var backgroundAction: ((Async) -> Unit)? = null
    private var foregroundAction: ((Async) -> Unit)? = null
    private var onErrorAction: ((Async) -> Unit)? = null
    private var progressEndAction: ((Async) -> Unit)? = null
    var lastError: Throwable? = null

    fun progressStart(func: (Async) -> Unit): Async {
        this.progressStartAction = func
        return this
    }

    fun background(func: (Async) -> Unit): Async {
        this.backgroundAction = func
        return this
    }

    fun foreground(func: (Async) -> Unit): Async {
        this.foregroundAction = func
        return this
    }

    fun onError(func: (Async) -> Unit): Async {
        this.onErrorAction = func
        return this
    }

    fun progressEnd(func: (Async) -> Unit): Async {
        this.progressEndAction = func
        return this
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun start() {

        val async = this

        progressStartAction?.invoke(this)

        GlobalScope.launch {
            try {
                backgroundAction?.invoke(async)
            } catch (t: Throwable) {
                lastError = t
                Platform.runLater {
                    progressEndAction?.invoke(async)
                    if (onErrorAction != null) {
                        onErrorAction!!(async)
                    } else {
                        DialogHelper.showError(t.message ?: "", t.toString())
                        throw t
                    }
                }
            }
            if (lastError != null) {
                return@launch
            }
            Platform.runLater {
                try {
                    foregroundAction?.invoke(async)
                } catch (t: Throwable) {
                    lastError = t
                    if (onErrorAction != null) {
                        onErrorAction!!.invoke(async)
                    } else {
                        DialogHelper.showError(t.message ?: "", t.toString())
                        throw t
                    }
                } finally {
                    progressEndAction?.invoke(async)
                }
            }
        }
    }
}
