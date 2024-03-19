package shirates.builder

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import shirates.builder.utility.TreeItemUtility.getDescendantAndSelf
import shirates.core.driver.TestElement
import shirates.core.utility.toPath
import kotlin.io.path.nameWithoutExtension

class ScreenItem(
    xmlFile: String
) {
    val xmlFileProperty = SimpleStringProperty()
    var xmlFile: String
        set(value) {
            xmlFileProperty.set(value)
            fileNoProperty.set(value.toPath().nameWithoutExtension)
            imageFileProperty.set(value.toPath().parent.resolve("${fileNo}.png").toString())
        }
        get() {
            return xmlFileProperty.value ?: ""
        }
    val selectorItems = mutableListOf<SelectorItem>()

    var rootElement = TestElement.emptyElement

    var rootTreeItem = TreeItem<TestElement>()

    val fileNoProperty = SimpleStringProperty()
    val fileNo: String
        get() {
            return fileNoProperty.value ?: ""
        }

    val imageFileProperty = SimpleStringProperty()
    val imageFile: String
        get() {
            return imageFileProperty.value ?: ""
        }

    init {
        this.xmlFile = xmlFile
    }

    fun getTreeItemOf(testElement: TestElement): TreeItem<TestElement>? {

        val items = rootTreeItem.getDescendantAndSelf()
        return items.firstOrNull() { it.value == testElement }
    }
}