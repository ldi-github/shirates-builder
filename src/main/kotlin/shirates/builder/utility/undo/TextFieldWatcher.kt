package shirates.builder.utility.undo

import javafx.beans.value.ChangeListener
import javafx.scene.control.TextField

class TextFieldWatcher(
    val textField: TextField,
    val undoManager: UndoManager,
    val onCommitted: ((TextFieldWatcher) -> Unit)? = null,
    val getEditingItem: () -> Any?,
    val undoTargets: UndoTargets? = null,
) {
    var startText = ""
    var endText = ""

    var committedText = ""
        get() {
            if (field.isBlank()) {
                field = textField.text
            }
            return field
        }
        private set

    var isEditing = false
        private set;

    private var tempRecord: UndoableRecord? = null

    var editingItem: Any? = null

    init {
        val focusedPropertyChangeListener = ChangeListener { _, _, hasFocus ->
            if (hasFocus) {
                if (isEditing.not()) {
                    editingItem = getEditingItem()
                    if (editingItem == null) {
                        return@ChangeListener
                    }
                    isEditing = true
                    startText = textField.text
                    val targets = UndoTargets(editingItem!!).merge(undoTargets)
                    tempRecord = UndoableRecord(undoTargets = targets)
                    tempRecord!!.captureForUndo()
                }
                committedText = textField.text
            } else {
                if (isEditing) {
                    isEditing = false
                    endText = textField.text
                }
                if (startText != endText) {
                    committedText = endText
                    if (tempRecord != null) {
                        tempRecord!!.captureForRedo()
                        undoManager.push(tempRecord!!)
                        tempRecord = null
                    }
                }
            }
        }

        textField.focusedProperty().addListener(focusedPropertyChangeListener)
    }

}