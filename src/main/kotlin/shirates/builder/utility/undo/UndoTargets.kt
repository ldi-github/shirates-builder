package shirates.builder.utility.undo

class UndoTargets(
    vararg targets: Any?
) {
    val objects = mutableListOf<Any>()

    init {
        for (t in targets) {
            addCore(t)
        }
    }

    private fun addCore(t: Any?) {
        if (t == null) {
            return
        }
        if (this.objects.contains(t).not()) {
            objects.add(t)
        }
    }

    fun add(target: Any?): UndoTargets {
        addCore(target)
        return this
    }

    fun merge(undoTargets: UndoTargets?): UndoTargets {
        if (undoTargets == null) {
            return this
        }
        for (t in undoTargets.objects) {
            addCore(t)
        }
        return this
    }
}