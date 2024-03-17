package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import shirates.builder.utility.undo.Undoable

class CombinationEditorViewModel(
    keyName: String,
    val editViewModel: EditViewModel
) {
    val keyNameProperty = SimpleStringProperty()

    var isCombinedKeyVisible: Boolean
        get() {
            return isCombinedKeyVisibleProperty.value
        }
        set(value) {
            isCombinedKeyVisibleProperty.set(value)
        }
    val isCombinedKeyVisibleProperty = SimpleBooleanProperty(true)

    @Undoable
    var combinedKey: String
        get() {
            return combinedKeyProperty.value ?: ""
        }
        set(value) {
            combinedKeyProperty.set(value)
        }
    val combinedKeyProperty = SimpleStringProperty()

    @Undoable
    var selectedItem: SelectorItem?
        get() {
            return selectedItemProperty.value
        }
        set(value) {
            selectedItemProperty.set(value)
        }
    val selectedItemProperty = SimpleObjectProperty<SelectorItem>()

    @Undoable
    var isPromptVisible: Boolean
        get() {
            return isPromptVisibleProperty.value
        }
        set(value) {
            isPromptVisibleProperty.set(value)
        }
    val isPromptVisibleProperty = SimpleBooleanProperty()

    @Undoable
    var isAutoCheckBoxSelected: Boolean
        get() {
            return isAutoCheckBoxSelectedProperty.value
        }
        set(value) {
            isAutoCheckBoxSelectedProperty.set(value)
        }
    val isAutoCheckBoxSelectedProperty = SimpleBooleanProperty()

    @Undoable
    lateinit var items: ObservableList<SelectorItem>


    init {
        keyNameProperty.set(keyName)
        combinedKeyProperty.set("")
        isPromptVisibleProperty.set(true)

        isAutoCheckBoxSelectedProperty.addListener { _, _, new ->
            editViewModel.screenBuilderViewModel.savePreferences()
            if (new == true) {
                editViewModel.refreshSatelliteCandidates()
            }
        }
    }

    /**
     * addItem
     */
    fun addItem(item: SelectorItem? = selectedItem): SelectorItem? {

        if (item == null) {
            return null
        }
        if (items.contains(item)) {
            return null
        }
        items.add(item)
        refresh()
        return item
    }

    /**
     * removeItem
     */
    fun removeItem(item: SelectorItem? = selectedItem) {

        if (selectedItem == null) {
            return
        }
        if (items.contains(item).not()) {
            return
        }
        items.remove(item)
        refresh()
    }

    /**
     * refresh
     */
    fun refresh() {

        val identity = items.map { it.nickname }.joinToString("")
        combinedKeyProperty.set(identity)

        isPromptVisibleProperty.set(items.isEmpty())
    }

}