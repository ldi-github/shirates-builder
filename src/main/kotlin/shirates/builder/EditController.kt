package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.util.Callback
import shirates.builder.utility.DialogHelper
import shirates.builder.utility.bindDisable
import shirates.builder.utility.draganddrop.acceptLink
import shirates.builder.utility.draganddrop.pushObject
import shirates.builder.utility.runAsync
import shirates.builder.utility.undo.*
import shirates.core.configuration.PropertiesManager
import shirates.core.configuration.ScreenInfo
import shirates.core.configuration.repository.ScreenRepository
import shirates.core.driver.Bounds
import shirates.core.driver.TestElement
import shirates.core.driver.TestMode
import shirates.core.driver.commandextension.*
import shirates.core.driver.testDrive
import shirates.core.logging.TestLog
import shirates.core.utility.exists
import shirates.core.utility.fileExists
import shirates.core.utility.toPath
import shirates.spec.utilily.removeBrackets
import java.net.URL
import java.util.*

class EditController : Initializable {

    lateinit var screenBuilderController: ScreenBuilderController
    val screenBuilderViewModel: ScreenBuilderViewModel
        get() {
            return screenBuilderController.screenBuilderViewModel
        }

    lateinit var identityController: CombinationEditorController
    lateinit var satellitesController: CombinationEditorController
    lateinit var headerElementsController: CombinationEditorController
    lateinit var footerElementsController: CombinationEditorController
    lateinit var overlayElementsController: CombinationEditorController
    lateinit var startElementsController: CombinationEditorController
    lateinit var endElementsController: CombinationEditorController

    lateinit var editViewModel: EditViewModel

    @FXML
    lateinit var rootNode: AnchorPane

    /**
     * Edit tab
     */
    @FXML
    lateinit var editTabGridPane: GridPane

    @FXML
    lateinit var editorGridPane: GridPane

    @FXML
    lateinit var workDirectoryTextField: TextField

    @FXML
    lateinit var workDirectoryButton: Button

    @FXML
    lateinit var loadButton: Button

    @FXML
    private lateinit var previewJsonButton: Button

    @FXML
    lateinit var captureButton: Button

    @FXML
    lateinit var captureProgressIndicator: ProgressIndicator

    @FXML
    lateinit var captureWithScrollButton: Button

    @FXML
    lateinit var downRadioButton: RadioButton

    @FXML
    lateinit var rightRadioButton: RadioButton

    @FXML
    lateinit var leftRadioButton: RadioButton

    @FXML
    lateinit var upRadioButton: RadioButton

    @FXML
    lateinit var nextScreenButton: Button

    @FXML
    lateinit var previousScreenButton: Button

    @FXML
    lateinit var screenListView: ListView<ScreenItem>

    @FXML
    lateinit var imageScrollPane: ScrollPane

    @FXML
    lateinit var imagePane: AnchorPane

    @FXML
    private lateinit var selectorsTableView: TableView<SelectorItem>

    @FXML
    private lateinit var editorTabPane: TabPane

    @FXML
    private lateinit var nicknameTab: Tab

    @FXML
    private lateinit var screenTab: Tab

    @FXML
    private lateinit var nicknameColumn: TableColumn<*, *>

    @FXML
    private lateinit var selectorExpressionColumn: TableColumn<*, *>

    @FXML
    private lateinit var keyInfoColumn: TableColumn<*, *>

    @FXML
    private lateinit var nicknamesTab: Tab

    @FXML
    private lateinit var elementsTab: Tab

    @FXML
    private lateinit var elementsTreeView: TreeView<TestElement>

    @FXML
    private lateinit var nicknameTextField: TextField

    @FXML
    private lateinit var selectorExpressionTextField: TextField

    @FXML
    private lateinit var widgetTextField: TextField

    @FXML
    private lateinit var classOrTypeTextField: TextField

    @FXML
    private lateinit var idOrNameTextField: TextField

    @FXML
    private lateinit var accessTextField: TextField

    @FXML
    private lateinit var textOrLabelTextField: TextField

    @FXML
    private lateinit var boundsTextField: TextField

    @FXML
    private lateinit var cellTextField: TextField

    @FXML
    private lateinit var scrollHostTextField: TextField

    @FXML
    private lateinit var upSelectorItemButton: Button

    @FXML
    private lateinit var downSelectorItemButton: Button

    @FXML
    private lateinit var commentButton: Button

    @FXML
    private lateinit var identityMessageLabel: Label

    @FXML
    private lateinit var nicknameDefinitionVbox: VBox

    @FXML
    private lateinit var screenConstraintsVbox: VBox


    val undoManager = UndoManager()

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

    }

    internal fun setup(screenBuilderController: ScreenBuilderController) {

        this.screenBuilderController = screenBuilderController

        setupViewModel()
        setupViewModelListeners()

        setupEditTabGridPane()
        setupImageScrollPane()
        setupImagePane()
        setupScreenListView()
        setupSelectorsTablesView()
        setupElementsTreeView()

        setupTextFieldListeners()
        setupTextFieldWatchers()
        setupButtonActions()

        setupCombinationEditors()

        setupBinding()
    }

    private fun setupCombinationEditors() {

        identityController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = nicknameDefinitionVbox,
            viewModel = editViewModel.identityViewModel
        )
        satellitesController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = nicknameDefinitionVbox,
            viewModel = editViewModel.satellitesViewModel,
            isCombinedKeyVisible = false,
            isMoveButtonVisible = false,
            isAutoCheckBoxVisible = true
        )
        satellitesController.rootNode.minHeight = 400.0

        headerElementsController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = screenConstraintsVbox,
            viewModel = editViewModel.headerElementsViewModel
        )
        footerElementsController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = screenConstraintsVbox,
            viewModel = editViewModel.footerElementsViewModel
        )
        overlayElementsController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = screenConstraintsVbox,
            viewModel = editViewModel.overlayElementsViewModel
        )
        startElementsController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = screenConstraintsVbox,
            viewModel = editViewModel.startElementsViewModel
        )
        endElementsController = CombinationEditorController.setupCombinationEditor(
            editController = this,
            vbox = screenConstraintsVbox,
            viewModel = editViewModel.endElementsViewModel
        )
    }

    private fun setupViewModel() {

        editViewModel = screenBuilderController.screenBuilderViewModel.editViewModel
        editViewModel.screenItems = screenListView.items
        editViewModel.selectorItems = selectorsTableView.items
    }

    private fun setupViewModelListeners() {

        editViewModel.selectedScreenItemProperty.addListener { _, _, new ->
            screenListView.selectionModel.select(new)
            editViewModel.selectorItems.clear()
            changeToSelectedScreen()
        }
        editViewModel.selectedSelectorItemProperty.addListener { _, _, new ->
            editViewModel.selectedNicknameProperty.set(new?.nickname ?: "")
            editViewModel.selectedSelectorExpressionProperty.set(new?.selectorExpression ?: "")
            selectorsTableView.selectionModel.select(new)
        }
        editViewModel.selectedIdentityItemProperty.addListener { _, _, new ->

        }
        editViewModel.selectedSelectorExpressionProperty.addListener { _, _, new ->
            val exp = new ?: ""
            if (exp.contains(">:") || exp.contains("}:") || exp.contains("]:")) {
                val style = "-fx-text-inner-color: darkgreen; -fx-background-color: honeydew; -fx-border-color: gray"
                nicknameTextField.style = style
                selectorExpressionTextField.style = style
            } else {
                nicknameTextField.style = ""
                selectorExpressionTextField.style = ""
            }
        }
    }

    private fun setupEditTabGridPane() {
        /**
         * editTabGridPane (Drag & Drop)
         */
        editTabGridPane.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        editTabGridPane.setOnDragDropped { e ->
            val dragboard = e.dragboard
            if (dragboard.hasFiles()) {
                val file = dragboard.files.first()
                editViewModel.workDirectoryProperty.set(file.path)
                editViewModel.loadFromDirectory(file.path)
            }
            e.isDropCompleted = true
            e.consume()
        }
    }

    private fun setupBinding() {

        editViewModel.apply {
            workDirectoryTextField.textProperty().bindBidirectional(workDirectoryProperty)
            downRadioButton.selectedProperty().bindBidirectional(downSelectedProperty)
            rightRadioButton.selectedProperty().bindBidirectional(rightSelectedProperty)
            leftRadioButton.selectedProperty().bindBidirectional(leftSelectedProperty)
            upRadioButton.selectedProperty().bindBidirectional(upSelectedProperty)

            nicknameTextField.textProperty().bindBidirectional(selectedNicknameProperty)
            selectorExpressionTextField.textProperty().bindBidirectional(selectedSelectorExpressionProperty)
            widgetTextField.textProperty().bind(widgetProperty)
            classOrTypeTextField.textProperty().bind(classOrTypeProperty)
            textOrLabelTextField.textProperty().bind(textOrLabelProperty)
            idOrNameTextField.textProperty().bind(idOrNameProperty)
            accessTextField.textProperty().bind(accessProperty)
            boundsTextField.textProperty().bind(boundsProperty)
            cellTextField.textProperty().bind(cellProperty)
            scrollHostTextField.textProperty().bind(scrollHostProperty)
        }
        screenBuilderViewModel.disabledProperty.bindDisable(
            workDirectoryTextField, workDirectoryButton, loadButton, previewJsonButton,
            captureButton, previousScreenButton, nextScreenButton,
            captureWithScrollButton, downRadioButton, rightRadioButton, leftRadioButton, upRadioButton,
            upSelectorItemButton, downSelectorItemButton, commentButton,
            nicknameTextField, selectorExpressionTextField
        )
        captureProgressIndicator.visibleProperty().bind(screenBuilderViewModel.disabledProperty)
    }

    private fun setupImageScrollPane() {

        imageScrollPane.widthProperty().addListener { _, _, new ->
            val newWidth = new?.toDouble() ?: return@addListener
            resizeImage(width = newWidth)
        }
        imageScrollPane.heightProperty().addListener { _, _, new ->
            val newHeight = new?.toDouble() ?: return@addListener
            resizeImage(height = newHeight)
        }
        editTabGridPane.setOnKeyPressed { e ->
//            println("${e.code}, control=${e.isControlDown}, meta=${e.isMetaDown}, alt=${e.isAltDown}, shift=${e.isShiftDown}")
            if (e.isUndoRequested) {
                undoManager.undo()
                refresh()
            } else if (e.isRedoRequested) {
                undoManager.redo()
                refresh()
            }
        }
    }

    private fun setupImagePane() {

        imagePane.setOnMouseClicked { e ->
            refreshImageRatio()
            if (e.isControlDown || e.isAltDown || e.isMetaDown) {
                val baseItem = editViewModel.selectedSelectorItem ?: return@setOnMouseClicked
                val relativeItem = editViewModel.getItemOnImage(e) ?: return@setOnMouseClicked

                undoManager.doAction(
                    description = "Set relative selector",
                    undoAction = { _ ->
                        clearRectangles()
                    },
                    redoAction = { _ ->
                        clearRectangles()
                    },
                    undoTargets = UndoTargets(relativeItem)
                )
                { _ ->
                    val r = editViewModel.setRelativeSelectorFromBaseElement(
                        relativeItem = relativeItem,
                        baseItem = baseItem
                    )
                    if (r == null) {
                        DialogHelper.showError(
                            "Can not set relative selector to the element.",
                            content = relativeItem.testElement.toString()
                        )
                        return@doAction
                    }
                    drawRectangleForBaseItem(baseItem)
                    drawRectangleForRelativeItem(relativeItem)
                    editViewModel.refreshKeyInfo()
                }
            } else {
                clearRectangles()
                val baseItem = editViewModel.selectItemOnImage(e)
                if (baseItem != null) {
                    drawRectangleForBaseItem(baseItem)
                    selectorsTableView.selectionModel.select(baseItem)
                    selectTreeItemOf(baseItem.testElement)
                } else {
                    val elm = editViewModel.getElementOnImage(e)
                    if (elm.isEmpty.not()) {
                        /**
                         * selectorTableView
                         */
                        val nonWidgetItem = editViewModel.selectorItems
                            .firstOrNull() { it.testElement == elm }
                            ?: elm.toSelectorItem(
                                idPrefix = editViewModel.getIdPrefix(),
                                screenItem = editViewModel.selectedScreenItem
                            )
                        drawRectangleForNonWidgetItem(nonWidgetItem)
                        selectorsTableView.selectionModel.select(nonWidgetItem)
                        /**
                         * elementsTreeView
                         */
                        selectTreeItemOf(elm)
                    }
                }
            }
            refresh()
        }
    }

    private fun selectTreeItemOf(testElement: TestElement) {

        val treeItem = editViewModel.getTreeItemOf(testElement = testElement)
        if (treeItem != null) {
            elementsTreeView.selectionModel.select(treeItem)
        }
    }

    private fun setupScreenListView() {

        screenListView.cellFactory = Callback {
            object : ListCell<ScreenItem>() {
                override fun updateItem(item: ScreenItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        graphic = null
                    } else {
                        val imageView =
                            if (item.imageFile.fileExists()) {
                                val iv = ImageView(item.imageFile.toPath().toUri().toString())
                                iv.fitWidth = 144.0
                                iv.isPreserveRatio = true
                                iv
                            } else Rectangle()
                        val label = Label(item.fileNo)
                        val vbox = VBox()
                        vbox.alignment = Pos.CENTER
                        vbox.children.addAll(imageView, label)
                        graphic = vbox

                        val deleteMenu = MenuItem()
                        deleteMenu.text = "Delete"
                        contextMenu = ContextMenu(deleteMenu)
                        screenListView.setOnKeyPressed { e ->
                            if (e.code == KeyCode.DELETE || e.code == KeyCode.BACK_SPACE) {
                                deleteScreenItem()
                            }
                        }
                        deleteMenu.setOnAction { e ->
                            deleteScreenItem()
                        }
                    }
                }
            }
        }
        screenListView.selectionModel.selectedItemProperty().addListener { _, _, new ->
            val item = new ?: return@addListener
            editViewModel.selectedScreenItem = item
            changeToSelectedScreen()
        }
    }

    private fun deleteScreenItem() {

        val item = editViewModel.selectedScreenItem ?: return

        undoManager.doAction(
            undoTargets = UndoTargets(editViewModel),
            undoAction = {
                editViewModel.recoverScreenItemFiles()
                screenBuilderViewModel.refresh()
            },
            redoAction = {
                editViewModel.deleteScreenItem(item)
                screenBuilderViewModel.refresh()
            }
        ) { data ->
            editViewModel.deleteScreenItem(item)
        }
    }

    private fun setupSelectorsTablesView() {

        nicknameColumn.cellValueFactory = PropertyValueFactory<SelectorItem, String>("nickname")
        selectorExpressionColumn.cellValueFactory = PropertyValueFactory<SelectorItem, String>("selectorExpression")
        keyInfoColumn.cellValueFactory = PropertyValueFactory<SelectorItem, Boolean>("keyInfo")

        selectorsTableView.setRowFactory { tv ->
            object : TableRow<SelectorItem>() {
                override fun updateItem(item: SelectorItem?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        style = ""
                    } else {
                        if (item.nickname.startsWith("#")) {
                            style = "-fx-background-color: lightsteelblue;"
                        } else {
                            style = ""
                        }
                    }
                }

                init {
                    setOnDragDetected { e ->
                        if (isEmpty.not()) {
                            startDragAndDrop(TransferMode.COPY)
                                .pushObject(item)
                            e.consume()
                        }
                    }
                }
            }
        }
        selectorsTableView.selectionModel.selectionMode = SelectionMode.SINGLE
        selectorsTableView.selectionModel.selectedItemProperty().addListener { _, _, new ->
            val item = new ?: return@addListener
            editViewModel.selectedSelectorItemProperty.set(item)
            clearRectangles()
            drawRectangle(item.testElement)
        }
    }

    private fun setupElementsTreeView() {

        elementsTreeView.selectionModel.selectedItemProperty().addListener { _, _, new ->
            val item = new ?: return@addListener
            val testElement = item.value
            clearRectangles()
            drawRectangle(testElement)
            editViewModel.refreshFields(testElement)
            val selectorItem = editViewModel.getSelectorItemOf(testElement = testElement)
            if (selectorItem != null) {
                selectorsTableView.selectionModel.select(selectorItem)
            }
        }
        elementsTreeView.setOnDragDetected { e ->
            val selectorItem = elementsTreeView.selectionModel.selectedItem.value.toSelectorItem(
                idPrefix = editViewModel.getIdPrefix(),
                screenItem = editViewModel.selectedScreenItem
            )
            elementsTreeView.startDragAndDrop(TransferMode.COPY)
                .pushObject(selectorItem)
            e.consume()
        }
    }

    private fun setupTextFieldListeners() {
        nicknameTextField.textProperty().addListener { _, _, new ->
            val item = editViewModel.selectedSelectorItem ?: return@addListener
            item.nickname = new
            screenBuilderController.refresh()
        }
        selectorExpressionTextField.textProperty().addListener { _, _, new ->
            val item = editViewModel.selectedSelectorItem ?: return@addListener
            item.selectorExpression = new
            screenBuilderController.refresh()
        }
    }

    lateinit var nicknameTextFieldWatcher: TextFieldWatcher
    lateinit var selectorExpressionTextFieldWatcher: TextFieldWatcher

    private fun setupTextFieldWatchers() {

        nicknameTextFieldWatcher = TextFieldWatcher(
            textField = nicknameTextField,
            undoManager = undoManager,
            getEditingItem = { editViewModel.selectedSelectorItem },
            undoTargets = UndoTargets(editViewModel)
        )
        selectorExpressionTextFieldWatcher = TextFieldWatcher(
            textField = selectorExpressionTextField,
            undoManager = undoManager,
            getEditingItem = { editViewModel.selectedSelectorItem },
            undoTargets = UndoTargets(editViewModel)
        )
    }

    fun workingDirectionButtonAction() {

        val initDir = editViewModel.workDirectoryProperty.value
        val directory = DialogHelper.openDirectoryDialog(initialDirectory = initDir)?.toString()
            ?: return
        editViewModel.loadFromDirectory(directory)
        screenBuilderViewModel.savePreferences()
    }

    fun loadButtonAction() {
        val workDirectory = editViewModel.workDirectoryProperty.value
        if (workDirectory.isNullOrBlank()) {
            DialogHelper.showError("Working Directory is required.")
            return
        }
        if (workDirectory.toPath().exists().not()) {
            DialogHelper.showError("Directory not found.", workDirectory)
            return
        }
        if (workDirectory.toPath().toFile().isDirectory.not()) {
            DialogHelper.showError("This is not directory.", workDirectory)
            return
        }
        if (editViewModel.selectorItems.any()) {
            val result =
                DialogHelper.showOkCancel("Do you want to reload data? Editing data will be discarded.")
            if (result == ButtonType.CANCEL) {
                return
            }
        }
        editViewModel.loadFromDirectory(workDirectory)
        screenBuilderController.selectTab("Edit")
    }

    private fun setupButtonActions() {

        captureButton.setOnAction {
            if (testDrive.driver.isReady.not()) {
                DialogHelper.showInformation("Appium is not ready. Start Appium session.")
                screenBuilderController.selectTab("Settings")
                return@setOnAction
            }
            screenBuilderViewModel.disable()
            val startLineNo = TestLog.lines.count()
            var endLineNo = 0
            runAsync(
                backgroundAction = {
                    testDrive.refreshCache()
                    testDrive.screenshot(force = true)
                    endLineNo = TestLog.lines.count()
                },
                foregroundAction = {
                    editViewModel.copyFilesToWorkDirectory(startLineNo = startLineNo, endLineNo = endLineNo)
                    editViewModel.loadFromDirectory()
                },
                onProgressEnd = {
                    screenBuilderViewModel.enable()
                }
            )
        }
        captureWithScrollButton.setOnAction {
            if (testDrive.driver.isReady.not()) {
                DialogHelper.showInformation("Appium is not ready. Start Appium session.")
                screenBuilderController.selectTab("Settings")
                return@setOnAction
            }
            screenBuilderViewModel.disable()
            PropertiesManager.setPropertyValue("enableInnerCommandLog", "true")
            val startLineNo = TestLog.lines.count()
            var endLineNo = 0
            runAsync(
                backgroundAction = {
                    testDrive.refreshCache()
                    testDrive.screenshot(force = true)
                    if (editViewModel.downSelectedProperty.value) {
                        testDrive.scrollToBottom(flick = false)
                    } else if (editViewModel.rightSelectedProperty.value) {
                        testDrive.scrollToRightEdge(flick = false)
                    } else if (editViewModel.leftSelectedProperty.value) {
                        testDrive.scrollToLeftEdge(flick = false)
                    } else {
                        testDrive.scrollToTop(flick = false)
                    }
                    endLineNo = TestLog.lines.count()
                },
                foregroundAction = {
                    editViewModel.copyFilesToWorkDirectory(startLineNo = startLineNo, endLineNo = endLineNo)
                    editViewModel.loadFromDirectory()
                }, onProgressEnd = {
                    screenBuilderViewModel.enable()
                }
            )
        }
        workDirectoryButton.setOnAction {
            workingDirectionButtonAction()
        }
        loadButton.setOnAction {
            try {
                loadButtonAction()
            } catch (t: Throwable) {
                DialogHelper.showError(t.message ?: t.toString())
            }
        }
        previewJsonButton.setOnAction {
            screenBuilderController.selectTab("Preview")
        }
        nextScreenButton.setOnAction {
            val item = editViewModel.nextScreenItem()
            screenListView.scrollTo(item)
            screenListView.requestFocus()
        }
        previousScreenButton.setOnAction {
            val item = editViewModel.previousScreenItem()
            screenListView.scrollTo(item)
            screenListView.requestFocus()
        }
        upSelectorItemButton.setOnAction {
            val item = editViewModel.selectedSelectorItem ?: return@setOnAction

            undoManager.doAction(
                undoTargets = UndoTargets(editViewModel)
            ) { _ ->
                val ix = editViewModel.selectorItems.indexOf(item)
                if (ix < 1) {
                    return@doAction
                }
                val item2 = editViewModel.selectorItems[ix - 1]
                editViewModel.selectorItems.set(ix - 1, item)
                editViewModel.selectorItems.set(ix, item2)
                selectorsTableView.selectionModel.select(item)
            }
        }
        downSelectorItemButton.setOnAction {
            val item = editViewModel.selectedSelectorItem ?: return@setOnAction

            undoManager.doAction(
                undoTargets = UndoTargets(editViewModel)
            ) { _ ->
                val ix = editViewModel.selectorItems.indexOf(item)
                if (ix > editViewModel.selectorItems.count() - 2) {
                    return@doAction
                }
                val item2 = editViewModel.selectorItems[ix + 1]
                editViewModel.selectorItems.set(ix + 1, item)
                editViewModel.selectorItems.set(ix, item2)
                selectorsTableView.selectionModel.select(item)
            }
        }

        fun comment(item: SelectorItem) {

            undoManager.doAction(
                undoTargets = UndoTargets(editViewModel, item)
            ) { _ ->
                if (item.nickname.startsWith("#")) {
                    item.nickname = item.nickname.substring(1)
                } else {
                    item.nickname = "#" + item.nickname
                }
                val ix = editViewModel.selectorItems.indexOf(item)
                editViewModel.selectorItems.remove(item)
                editViewModel.selectorItems.add(item)
                selectorsTableView.selectionModel.select(ix)
            }
        }

        commentButton.setOnAction {
            val item = editViewModel.selectedSelectorItem ?: return@setOnAction
            val items = editViewModel.selectorItems.filter { it.nickname == item.nickname }
            val count = items.count()
            if (count >= 2) {
                val r = DialogHelper.showOkCancel("Do you comment all ${item.nickname}? ($count)")
                if (r == ButtonType.OK) {
                    for (i in items) {
                        comment(i)
                    }
                    return@setOnAction
                }
            }
            comment(item)
        }

        selectorsTableView.setOnKeyPressed { e ->
            if (e.code == KeyCode.DELETE) {
                val item = editViewModel.selectedSelectorItem ?: return@setOnKeyPressed
                comment(item)
            }
        }
    }

    fun refresh() {

        validateIdentity()

        selectorsTableView.selectionModel.select(editViewModel.selectedSelectorItem)
        identityController.refresh()
        headerElementsController.refresh()
        footerElementsController.refresh()
        overlayElementsController.refresh()
        startElementsController.refresh()
        endElementsController.refresh()
    }

    fun getImageView(): ImageView? {
        return imagePane.children.filter { it is ImageView }.map { it as ImageView }.firstOrNull()
    }

    fun clearImagePane() {

        for (e in imagePane.children.filter { (it is Label).not() }) {
            imagePane.children.remove(e)
        }
    }

    fun refreshImageRatio(): Double {

        val imageView = getImageView() ?: return 0.0
        val image = imageView.image
        val r1 = imageView.fitWidth / image.width
        val r2 = imageView.fitHeight / image.height
        val r = if (r1 > r2) r2 else r1
        editViewModel.imageRatioProperty.set(r)
        return r
    }

    fun resizeImage(width: Double? = null, height: Double? = null) {
        val imageView = getImageView() ?: return
        if (width != null) {
            imageView.fitWidth = imageScrollPane.width - 4
        }
        if (height != null) {
            imageView.fitHeight = imageScrollPane.height - 4
        }
        clearRectangles()
    }

    internal fun changeToSelectedScreen() {

        editViewModel.changeToSelectedScreen()

        clearImagePane()

        val screenItem = editViewModel.selectedScreenItem ?: return

        val image = Image(screenItem.imageFile.toPath().toUri().toString())
        val imageView = ImageView(image)
        imageView.isPreserveRatio = true
        imagePane.children.add(imageView)
        resizeImage(width = imageScrollPane.width, height = imageScrollPane.height)

        elementsTreeView.root = screenItem.rootTreeItem
    }

    fun clearRectangles() {
        val removingShapes = imagePane.children.filter { it is Rectangle }
        imagePane.children.removeAll(removingShapes)
    }

    fun drawRectangle(bounds: Bounds, color: Color?) {
        val removingShapes = imagePane.children.filter { it is Rectangle && it.stroke == color }
        imagePane.children.removeAll(removingShapes)

        refreshImageRatio()
        val r = editViewModel.imageRatioProperty.value * (if (TestMode.isiOS) editViewModel.IOS_RATIO else 1.0)

        val rect =
            Rectangle(
                bounds.x1 * r,
                bounds.y1 * r,
                bounds.width * r,
                bounds.height * r
            )
        rect.strokeWidth = 2.0
        rect.stroke = color
        rect.fill = Color.rgb(0, 0, 0, 0.05)
        rect.setOnDragDetected { e ->
            val item = editViewModel.selectedSelectorItem ?: return@setOnDragDetected
            rect.startDragAndDrop(TransferMode.COPY)
                .pushObject(item)
            e.consume()
        }
        imagePane.children.add(rect)
    }

    fun drawRectangle(testElement: TestElement) {

        val color =
            if (testElement.isWidget) Const.WIDGET_COLOR
            else if (testElement.selector?.isContainingRelative == true) Const.RELATIVE_COLOR
            else Const.NON_WIDGET_COLOR
        drawRectangle(testElement.bounds, color = color)
    }

    fun drawRectangleForBaseItem(baseItem: SelectorItem) {

        drawRectangle(bounds = baseItem.testElement.bounds, color = Const.WIDGET_COLOR)
    }

    fun drawRectangleForRelativeItem(relativeItem: SelectorItem) {

        drawRectangle(bounds = relativeItem.testElement.bounds, color = Const.RELATIVE_COLOR)
    }

    fun drawRectangleForNonWidgetItem(item: SelectorItem) {

        clearRectangles()
        drawRectangle(bounds = item.testElement.bounds, color = Const.NON_WIDGET_COLOR)
    }

    private fun ScreenInfo.containsAllNicknames(list: List<String>): Boolean {

        val screenInfo = this
        val screenInfoNicknames = screenInfo.identitySelectors.map { it.nickname }
        return screenInfoNicknames.containsAll(list)
    }

    private fun ScreenInfo.containsAllExpressions(list: List<String>): Boolean {

        val screenInfo = this
        val screenInfoExpressions =
            screenInfo.identitySelectors.filter { it.expression != null }.map { it.expression!!.removeBrackets() }
        return screenInfoExpressions.containsAll(list)
    }

    fun validateIdentity() {

        val list = mutableListOf<ScreenInfo>()
        val screenInfos = ScreenRepository.screenInfoSearchList.toMutableList()
        val editingNicknames = editViewModel.identityViewModel.items.map { it.nickname }
        val editingExpressions = editViewModel.identityViewModel.items.map { it.selectorExpression.removeBrackets() }
        if (editingExpressions.any()) {
            for (screenInfo in screenInfos) {
                if (screenInfo.containsAllNicknames(editingNicknames) ||
                    screenInfo.containsAllExpressions(editingExpressions)
                ) {
                    list.add(screenInfo)
                }
            }
        }
        if (list.isEmpty()) {
            showIdentityMessage("Identity is valid", Alert.AlertType.INFORMATION)
        } else if (list.count() >= 1) {
            showIdentityMessage("Identity is not unique.", Alert.AlertType.ERROR)
        } else {
            throw IllegalStateException("Something wrong")
        }
    }

    fun showIdentityMessage(message: String, alertType: Alert.AlertType = Alert.AlertType.INFORMATION) {

        identityMessageLabel.text = message
        identityMessageLabel.textFill = when (alertType) {
            Alert.AlertType.ERROR -> Color.RED
            else -> Color.BLUE
        }
    }
}