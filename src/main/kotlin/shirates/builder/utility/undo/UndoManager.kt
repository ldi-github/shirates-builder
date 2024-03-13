package shirates.builder.utility.undo

import java.util.*

class UndoManager {

    internal val undoStack = Stack<UndoableRecord>()
    internal val redoStack = Stack<UndoableRecord>()

    fun push(undoableRecord: UndoableRecord) {

        undoStack.push(undoableRecord)
    }

    fun doAction(
        description: String = undoStack.count().toString(),
        undoAction: ((UndoableRecord) -> Unit)? = null,
        redoAction: ((UndoableRecord) -> Unit)? = null,
        undoTargets: UndoTargets,
        action: ((UndoableRecord) -> Unit)? = null,
    ): UndoableRecord {
        val record = UndoableRecord(
            description = description,
            undoAction = undoAction,
            redoAction = redoAction,
            undoTargets = undoTargets,
        )
        record.captureForUndo()
        undoStack.push(record)
        redoStack.clear()
        if (action != null) {
            action.invoke(record)
            record.captureForRedo()
        }
        return record
    }

    fun undo() {

        if (undoStack.isEmpty()) {
            return
        }
        val record = undoStack.pop()
        record.undo()
        redoStack.push(record)
    }

    fun redo() {

        if (redoStack.isEmpty()) {
            return
        }
        val record = redoStack.pop()
        record.redo()
        undoStack.push(record)
    }
}