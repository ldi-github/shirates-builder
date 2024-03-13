package shirates.builder.utility.draganddrop

import javafx.scene.input.ClipboardContent
import javafx.scene.input.Dragboard

fun Dragboard.pushObject(obj: Any) {

    val content = ClipboardContent()
    content.putString("dummy")
    this.setContent(content)

    DragAndDropHelper.pushObject(obj)
}
