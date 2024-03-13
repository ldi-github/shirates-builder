package shirates.builder.utility.undo

interface IUndoable {
    fun captureForUndo()
    fun captureForRedo()
    fun undo()
    fun redo()
}