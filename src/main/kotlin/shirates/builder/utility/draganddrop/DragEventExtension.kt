package shirates.builder.utility.draganddrop

import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode

fun DragEvent.acceptLink() {

    if (this.dragboard.hasFiles()) {
        this.acceptTransferModes(TransferMode.LINK)
    }

}

fun DragEvent.acceptCopy() {

    val db = this.dragboard
    if (db.hasString()) {
        this.acceptTransferModes(TransferMode.COPY)
    }
}
