package shirates.builder

class LoopCanceler {

    var isBusy: Boolean = false

    fun doAction(
        action: () -> Unit
    ) {
        if (isBusy) {
            return
        }

        try {
            isBusy = true
            action()
        } finally {
            isBusy = false
        }
    }
}