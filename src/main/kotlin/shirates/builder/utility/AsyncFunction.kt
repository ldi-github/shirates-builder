package shirates.builder.utility

import javafx.application.Platform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun runAsync(
    onProgressStart: (() -> Unit)? = null,
    backgroundAction: (() -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    onProgressEnd: (() -> Unit)? = null,
    foregroundAction: () -> Unit
) {
    onProgressStart?.invoke()

    var error: Throwable? = null

    GlobalScope.launch {
        try {
            backgroundAction?.invoke()
        } catch (t: Throwable) {
            error = t
            Platform.runLater {
                onProgressEnd?.invoke()
                if (onError != null) {
                    onError(t)
                } else {
                    DialogHelper.showError(t.message!!, t.toString())
                    throw t
                }
            }
        }
        if (error != null) {
            return@launch
        }
        Platform.runLater {
            try {
                foregroundAction()
            } catch (t: Throwable) {
                error = t
                if (onError != null) {
                    onError(t)
                } else {
                    DialogHelper.showError(t.message!!, t.toString())
                    throw t
                }
            } finally {
                onProgressEnd?.invoke()
            }
        }
    }
}
