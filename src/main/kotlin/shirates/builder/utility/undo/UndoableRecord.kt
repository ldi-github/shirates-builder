package shirates.builder.utility.undo

class UndoableRecord(
    val description: String = "",
    internal var undoAction: ((UndoableRecord) -> Unit)? = null,
    internal var redoAction: ((UndoableRecord) -> Unit)? = null,
    undoTargets: UndoTargets
) : IUndoable {
    val objects: MutableList<Any?> = mutableListOf()

    val items: List<Any?>
        get() {
            return objects.toList()
        }

    internal val undoableObjectMap = mutableMapOf<Any, IUndoable>()

    init {
        this.objects.addAll(undoTargets.objects)
        setup()
    }

    override fun captureForUndo() {

        undoableObjectMap.values.forEach { it.captureForUndo() }
    }

    override fun captureForRedo() {

        undoableObjectMap.values.forEach { it.captureForRedo() }
    }

    fun setup() {

        for (obj in objects) {
            if (obj == null) {
                continue
            }
            if (obj is MutableList<*>) {
                if (undoableObjectMap.containsKey(obj).not()) {
                    undoableObjectMap[obj] = ListUndoable(obj)
                }
            } else {
                if (undoableObjectMap.containsKey(obj).not()) {
                    undoableObjectMap[obj] = PropertyUndoable(obj)
                }
            }
        }
    }

    override fun undo() {

        for (undoable in undoableObjectMap.values) {
            undoable.undo()
        }
        undoAction?.invoke(this)
    }

    override fun redo() {

        for (undoable in undoableObjectMap.values) {
            undoable.redo()
        }
        redoAction?.invoke(this)
    }
}