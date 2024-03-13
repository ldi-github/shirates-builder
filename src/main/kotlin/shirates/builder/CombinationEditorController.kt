package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.Callback
import kotlinx.coroutines.DelicateCoroutinesApi
import shirates.builder.utility.draganddrop.DragAndDropHelper
import shirates.builder.utility.draganddrop.acceptCopy
import shirates.builder.utility.undo.UndoTargets
import java.net.URL
import java.util.*

class CombinationEditorController : Initializable {

    @FXML
    lateinit var rootNode: AnchorPane

    @FXML
    lateinit var headerHBox: HBox

    @FXML
    lateinit var bodyAnchorPane: AnchorPane

    @FXML
    lateinit var bodyHBox: HBox

    @FXML
    lateinit var keyNameLabel: Label

    @FXML
    lateinit var autoCheckBox: CheckBox

    @FXML
    lateinit var combinedKyTextField: TextField

    @FXML
    lateinit var itemsListView: ListView<SelectorItem>

    @FXML
    lateinit var rightVBox: VBox

    @FXML
    lateinit var upButton: Button

    @FXML
    lateinit var downButton: Button

    @FXML
    lateinit var promptLabel: Label

    lateinit var editController: EditController

    lateinit var vbox: VBox

    lateinit var viewModel: CombinationEditorViewModel


    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {

    }

    @OptIn(DelicateCoroutinesApi::class)
    internal fun setup(
        editController: EditController,
        parent: VBox,
        viewModel: CombinationEditorViewModel,
        isCombinedKeyVisible: Boolean = true,
        isMoveButtonVisible: Boolean = true,
        isAutoCheckBoxVisible: Boolean = false
    ) {
        if (isAutoCheckBoxVisible.not()) {
            headerHBox.children.remove(this.autoCheckBox)
        }
        if (isCombinedKeyVisible.not()) {
            headerHBox.children.remove(this.combinedKyTextField)
        }
        if (isMoveButtonVisible.not()) {
            rightVBox.children.remove(upButton)
            rightVBox.children.remove(downButton)
        }
        this.editController = editController
        this.vbox = parent
        this.viewModel = viewModel
        viewModel.items = itemsListView.items

        setupItemsListView()
        setupButtonActions()
        setupBindings()
    }

    private fun setupItemsListView() {

        itemsListView.selectionModel.selectionMode = SelectionMode.SINGLE
        itemsListView.itemsProperty().addListener { o, old, new ->
        }
        itemsListView.cellFactory = Callback {
            object : ListCell<SelectorItem>() {
                override fun updateItem(item: SelectorItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = ""
                    } else {
                        text = item.nickname
                    }
                }
            }
        }
        val loopCanceler = LoopCanceler()
        itemsListView.selectionModel.selectedItemProperty().addListener { o, old, new ->
            val item = new
            loopCanceler.doAction {
                viewModel.selectedItemProperty.set(item)
                viewModel.editViewModel.selectedSelectorItem = item
                if (item.screenItem != null) {
                    viewModel.editViewModel.selectedScreenItem = item.screenItem
                    itemsListView.selectionModel.select(item)
                }
            }
        }
        itemsListView.setOnMouseClicked { e ->
        }
        itemsListView.setOnDragOver { e ->
            e.acceptCopy()
        }
        itemsListView.setOnDragDropped { e ->
            if (DragAndDropHelper.isEmpty) {
                return@setOnDragDropped
            }
            val item = DragAndDropHelper.popObject<SelectorItem>()
            editController.undoManager.doAction(
                undoTargets = UndoTargets(viewModel, viewModel.editViewModel)
            ) { data ->
                viewModel.addItem(item)
                editController.mainController.refresh()
            }
            e.consume()
        }
        itemsListView.setOnKeyPressed { e ->
            if (e.code == KeyCode.DELETE || e.code == KeyCode.BACK_SPACE) {
                viewModel.removeItem()
            }
        }
    }

    private fun setupButtonActions() {

        downButton.setOnAction {
            val item = viewModel.selectedItem ?: return@setOnAction
            editController.undoManager.doAction(
                undoTargets = UndoTargets(viewModel)
            ) { data ->
                val ix = viewModel.items.indexOf(item)
                if (ix > viewModel.items.count() - 2) {
                    return@doAction
                }
                val item2 = viewModel.items[ix + 1]
                viewModel.items.set(ix + 1, item)
                viewModel.items.set(ix, item2)
                itemsListView.selectionModel.select(item)
                viewModel.refresh()
            }
        }
        upButton.setOnAction {
            val item = viewModel.selectedItem ?: return@setOnAction
            editController.undoManager.doAction(
                undoTargets = UndoTargets(viewModel)
            ) { data ->
                val ix = viewModel.items.indexOf(item)
                if (ix < 1) {
                    return@doAction
                }
                val item2 = viewModel.items[ix - 1]
                viewModel.items.set(ix - 1, item)
                viewModel.items.set(ix, item2)
                itemsListView.selectionModel.select(item)
                viewModel.refresh()
            }
        }
    }

    private fun setupBindings() {
        keyNameLabel.textProperty().bind(viewModel.keyNameProperty)
        combinedKyTextField.textProperty().bind(viewModel.combinedKeyProperty)
        combinedKyTextField.visibleProperty().bind(viewModel.isCombinedKeyVisibleProperty)
        promptLabel.visibleProperty().bind(viewModel.isPromptVisibleProperty)

        autoCheckBox.selectedProperty().bindBidirectional(viewModel.isAutoCheckBoxSelectedProperty)
    }

    fun refresh() {

        viewModel.refresh()
        itemsListView.selectionModel.select(viewModel.selectedItem)
        itemsListView.refresh()
    }

    companion object {
        fun setupCombinationEditor(
            editController: EditController,
            vbox: VBox,
            viewModel: CombinationEditorViewModel,
            isCombinedKeyVisible: Boolean = true,
            isMoveButtonVisible: Boolean = true,
            isAutoCheckBoxVisible: Boolean = false
        ): CombinationEditorController {

            val fxmlLoader =
                FXMLLoader(shirates.builder.BuilderApplication::class.java.getResource("combination-editor-view.fxml"))
            val node = fxmlLoader.load<Node>()
            vbox.children.add(node)

            val controller = fxmlLoader.getController<CombinationEditorController>()
            controller.setup(
                editController = editController,
                parent = vbox,
                viewModel = viewModel,
                isCombinedKeyVisible = isCombinedKeyVisible,
                isMoveButtonVisible = isMoveButtonVisible,
                isAutoCheckBoxVisible = isAutoCheckBoxVisible
            )
            return controller
        }
    }
}