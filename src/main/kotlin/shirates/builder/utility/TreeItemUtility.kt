package shirates.builder.utility

import javafx.scene.control.TreeItem

object TreeItemUtility {

    fun <T> getDescendantsAndSelf(
        treeItem: TreeItem<T>,
        list: MutableList<TreeItem<T>> = mutableListOf()
    ): MutableList<TreeItem<T>> {

        list.add(treeItem)

        for (c in treeItem.children) {
            getDescendantsAndSelf(c, list)
        }
        return list
    }

    fun <T> TreeItem<T>.getDescendantAndSelf(): List<TreeItem<T>> {

        return getDescendantsAndSelf(treeItem = this)
    }
}