package shirates.builder.utility

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.Node
import javafx.scene.control.*

fun SimpleBooleanProperty.bindDisable(
    vararg nodes: Node
) {
    for (n in nodes) {
        n.disableProperty().bind(this)
    }
}

fun SimpleBooleanProperty.bindDisableAllUnder(
    node: Node,
    excepts: List<Node> = mutableListOf()
) {
    val allNodes = NodeUtility.getDescendants(node)
    for (n in allNodes) {
        bindDisableAllUnderCore(n, excepts)
    }
}

private fun SimpleBooleanProperty.bindDisableAllUnderCore(
    n: Node,
    excepts: List<Node>,
) {
    if (excepts.contains(n)) {
        return
    }
    if (n is ProgressIndicator) {
        return
    }
    if (n is ButtonBase || n is TextInputControl || n is ListView<*> || n is TableView<*>) {
        n.disableProperty().bind(this)
    }
}

