package shirates.builder

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ButtonType
import shirates.builder.utility.DialogHelper
import shirates.builder.utility.undo.Undoable
import shirates.core.configuration.isValidNickname
import shirates.core.utility.element.ElementCategory
import shirates.core.utility.exists
import shirates.core.utility.toPath
import shirates.spec.utilily.removeBrackets

class PreviewViewModel(
    val screenBuilderViewModel: ScreenBuilderViewModel
) {
    @Undoable
    var key: String
        get() {
            return keyProperty.value
        }
        set(value) {
            keyProperty.set(value)
        }
    val keyProperty = SimpleStringProperty("")

    @Undoable
    var nicknameFile: String
        get() {
            return nicknameFileProperty.value
        }
        set(value) {
            nicknameFileProperty.set(value)
        }
    val nicknameFileProperty = SimpleStringProperty("")

    val nicknameFileShort: String
        get() {
            return nicknameFile.toPath().toFile().name
        }

    val directory: String
        get() {
            return nicknameFile.toPath().parent.toString()
        }

    var previewJson: String
        get() {
            return previewJsonProperty.value
        }
        set(value) {
            previewJsonProperty.set(value)
        }
    val previewJsonProperty = SimpleStringProperty("")

    val isKeyRequired: Boolean
        get() {
            return key.isBlank()
        }

    val isKeyValid: Boolean
        get() {
            if (key.startsWith("[").not() || key.endsWith("]").not()) {
                return false
            }
            return key.isValidNickname()
        }

    init {
        keyProperty.addListener { o, old, new ->
            refresh()
        }
    }

    fun validateForOpenDirectory(): Boolean {
        val dir = nicknameFile.toPath().parent
        if (dir.exists().not()) {
            DialogHelper.showError("Directory not found.")
            return false
        }
        return true
    }

    fun validateForSave(): Boolean {

        if (isKeyRequired) {
            DialogHelper.showError("Screen Name is required.")
            return false
        }
        if (isKeyValid.not()) {
            DialogHelper.showError("Screen Name format must be [Screen Name].")
            return false
        }
        if (nicknameFile.toPath().exists()) {
            val result =
                DialogHelper.showOkCancel("File exist. Do you want to rver write it?", content = nicknameFile)
            if (result == ButtonType.CANCEL) {
                return false
            }
        }
        val parentDir = nicknameFile.toPath().parent
        if (parentDir.exists().not()) {
            DialogHelper.showError("Directory not found.", parentDir.toString())
            return false
        }
        return true
    }

    fun refresh() {

        val xmlFilePath = screenBuilderViewModel.editViewModel.xmlFile.toPath()
        if (xmlFilePath.exists()) {
            val filename =
                if (key.isNotBlank()) "$key.json"
                else ""
            nicknameFile = xmlFilePath.parent.resolve(filename).toString()
        }

        refreshPreviewJson()
    }

    fun refreshPreviewJson() {

        val vm = screenBuilderViewModel.editViewModel
        val scrollableToSelectorItemsMap = vm.getScrollHostSelectorItemsMap()
        val identity = vm.identityViewModel.combinedKey
        val satellites = vm.satellitesViewModel.items
        val headerElements = vm.headerElementsViewModel.combinedKey
        val footerElements = vm.footerElementsViewModel.combinedKey
        val overlayElements = vm.overlayElementsViewModel.combinedKey
        val startElements = vm.startElementsViewModel.combinedKey
        val endElements = vm.endElementsViewModel.combinedKey

        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"key\": \"${keyProperty.value}\",")
        sb.appendLine("")

        sb.appendLine("  \"identity\": \"$identity\",")
        sb.appendLine("")

        sb.appendLine("  \"satellites\": [")
        for (i in 0 until satellites.count()) {
            val comma = if (i == satellites.count() - 1) "" else ","
            val s = satellites[i]
            sb.appendLine("    \"${s.nickname}\"$comma")
        }
        sb.appendLine("  ],")
        sb.appendLine()

        sb.appendLine("  \"selectors\": {")
        var lastCell = ""
        val selectorItems = mutableListOf<SelectorItem>()
        if (scrollableToSelectorItemsMap.containsKey("")) {
            selectorItems.addAll(scrollableToSelectorItemsMap[""]!!)
        }
        for (kv in scrollableToSelectorItemsMap) {
            if (kv.key.isNotBlank()) {
                selectorItems.addAll(kv.value)
            }
        }
        for (i in 0 until selectorItems.count()) {
            val comma = if (i == selectorItems.count() - 1) "" else ","
            val s = selectorItems[i]
            if (lastCell != s.cell) {
                sb.appendLine()
                lastCell = s.cell
            }
            val exp =
                if (s.testElement.category == ElementCategory.LABEL && s.nickname.removeBrackets() == s.selectorExpression) ""
                else s.selectorExpression
            sb.appendLine("    \"${s.nickname}\": \"$exp\"$comma")
        }
        sb.appendLine("  },")
        sb.appendLine()

        sb.appendLine("  \"scroll\": {")
        sb.appendLine("    \"header-elements\": \"$headerElements\",")
        sb.appendLine("    \"footer-elements\": \"$footerElements\",")
        sb.appendLine("    \"overlay-elements\": \"$overlayElements\",")
        sb.appendLine("    \"start-elements\": \"$startElements\",")
        sb.appendLine("    \"end-elements\": \"$endElements\"")
        sb.appendLine("  }")
        sb.appendLine("}")

        previewJson = sb.toString()
    }

    fun saveNicknameFile() {

        val file = nicknameFile.toPath().toFile()
        val text = previewJsonProperty.value
        file.writeText(text)
    }

}