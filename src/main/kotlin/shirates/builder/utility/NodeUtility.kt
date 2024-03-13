package shirates.builder.utility

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ScrollPane
import javafx.scene.control.TabPane

object NodeUtility {

    fun getAllNodes(node: Node, list: MutableList<Node> = mutableListOf()): MutableList<Node> {

        list.add(node)

        val children = if (node is TabPane) {
            node.tabs.map { it.content }
        } else if (node is ScrollPane) {
            listOf(node.content)
        } else {
            (node as Parent).childrenUnmodifiable
        }

        for (c in children) {
            getAllNodes(c, list)
        }
        return list
    }
}