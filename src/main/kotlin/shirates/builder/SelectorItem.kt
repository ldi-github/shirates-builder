package shirates.builder

import javafx.beans.property.SimpleStringProperty
import shirates.builder.utility.undo.Undoable
import shirates.core.driver.TestElement
import shirates.core.driver.commandextension.getCell
import shirates.core.driver.commandextension.getUniqueSelector

class SelectorItem(
    nickname: String = "",
    selectorExpression: String = "",
    keyInfo: String = "",
    testElement: TestElement = TestElement.emptyElement,
    val screenItem: ScreenItem? = null
) {
    private val _nicknameProperty = SimpleStringProperty()
    private val _selectorExpressionProperty = SimpleStringProperty()
    private val _keyInfoProperty = SimpleStringProperty()

    private var _testElement = TestElement.emptyElement
    var testElement: TestElement
        get() {
            return _testElement
        }
        set(value) {
            _testElement = value
        }

    /**
     * DO NOT DELETE!!
     * This function is required for TableView cellValueFactory
     */
    fun nicknameProperty(): SimpleStringProperty {
        return _nicknameProperty
    }

    /**
     * DO NOT DELETE!!
     * This function is required for TableView cellValueFactory
     */
    fun selectorExpressionProperty(): SimpleStringProperty {
        return _selectorExpressionProperty
    }

    /**
     * DO NOT DELETE!!
     * This function is required for TableView cellValueFactory
     */
    fun keyInfoProperty(): SimpleStringProperty {
        return _keyInfoProperty
    }

    @Undoable
    var nickname: String
        get() {
            return _nicknameProperty.value
        }
        set(value) {
            _nicknameProperty.set(value)
        }

    @Undoable
    var selectorExpression: String
        get() {
            return _selectorExpressionProperty.value
        }
        set(value) {
            _selectorExpressionProperty.set(value)
        }

    @Undoable
    var keyInfo: String
        get() {
            return _keyInfoProperty.value
        }
        set(value) {
            _keyInfoProperty.set(value)
        }

    var cell: String
        get() {
            return cellProperty.value
        }
        set(value) {
            cellProperty.set(value)
        }
    private val cellProperty = SimpleStringProperty()

    var scrollHost: String
        get() {
            return scrollHostProperty.value
        }
        set(value) {
            scrollHostProperty.set(value)
        }
    private val scrollHostProperty = SimpleStringProperty("")

    init {
        this.nickname = nickname.ifBlank { "[${testElement.selector}]" }
        this.selectorExpression = selectorExpression.ifBlank { "${testElement.selector}" }
        this.keyInfo = keyInfo
        this.cell = testElement.getCell().toString()
        val ancestorScrollable = testElement.ancestorScrollable
        this.scrollHost =
            if (ancestorScrollable.isEmpty) ""
            else {
                if (ancestorScrollable.idOrNameSimple.isNotBlank()) {
                    "#${ancestorScrollable.idOrNameSimple}"
                } else {
                    ancestorScrollable.getUniqueSelector().toString()
                }
            }
        this.testElement = testElement
    }

    override fun toString(): String {
        return "($nickname, $selectorExpression, $keyInfo)"
    }

}

fun TestElement.toSelectorItem(idPrefix: String, screenItem: ScreenItem?): SelectorItem {

    var nickname = ""
    var expression = ""
    if (this.textOrLabel.isNotBlank()) {
        expression = this.textOrLabel
        nickname = "[$expression]"
    } else if (this.idOrNameSimple.isNotBlank()) {
        expression = "#" + this.idOrNameSimple
        nickname = "[$expression]"
    } else if (this.access.isNotBlank()) {
        expression = "@" + this.access
        nickname = "[$expression]"
    } else {
        expression = this.getUniqueSelector().toString().replace(idPrefix, "")
        nickname = "[nickname]"
    }

    val item = SelectorItem(
        nickname = nickname,
        selectorExpression = expression,
        testElement = this,
        screenItem = screenItem
    )
    return item
}
