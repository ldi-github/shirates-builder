package shirates.builder.utility.undo


class ListUndoable<T>(
    val list: MutableList<T>
) : IUndoable {
    internal val undoState = mutableListOf<T>()
    internal val redoState = mutableListOf<T>()

    override fun captureForUndo() {

        undoState.addAll(list)
    }

    override fun captureForRedo() {

        redoState.addAll(list)
    }


    override fun undo() {

        list.clear()
        list.addAll(undoState)
    }

    override fun redo() {

        list.clear()
        list.addAll(redoState)
    }
}
