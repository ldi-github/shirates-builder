package shirates.builder.utility.undo

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import java.io.File

val KeyEvent.isUndoRequested: Boolean
    get() {
        if (File.separator == "/") {
            return this.isShiftDown.not() && this.isMetaDown && this.code == KeyCode.Z
        } else {
            return this.isControlDown && this.code == KeyCode.Z
        }
    }

val KeyEvent.isRedoRequested: Boolean
    get() {
        if (File.separator == "/") {
            return this.isShiftDown && this.isMetaDown && this.code == KeyCode.Z
        } else {
            return this.isControlDown && this.code == KeyCode.Y
        }
    }