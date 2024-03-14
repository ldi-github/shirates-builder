package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import kotlinx.coroutines.DelicateCoroutinesApi
import shirates.builder.utility.DialogHelper
import shirates.builder.utility.bindDisable
import shirates.builder.utility.draganddrop.acceptLink
import shirates.builder.utility.runAsync
import shirates.core.utility.exists
import shirates.core.utility.toPath
import java.net.URL
import java.util.*

class SettingsController : Initializable {

    lateinit var screenBuilderController: ScreenBuilderController
    val screenBuilderViewModel: ScreenBuilderViewModel
        get() {
            return screenBuilderController.screenBuilderViewModel
        }

    lateinit var settingsViewModel: SettingsViewModel

    @FXML
    lateinit var rootNode: AnchorPane


    @FXML
    private lateinit var settingsGridPane: GridPane

    @FXML
    lateinit var testrunfileHBox: HBox

    @FXML
    lateinit var projectDirectoryTextField: TextField

    @FXML
    lateinit var testrunFileTextField: TextField

    @FXML
    lateinit var testrunFileButton: Button

    @FXML
    lateinit var androidRadioButton: RadioButton

    @FXML
    lateinit var iosRadioButton: RadioButton

    @FXML
    lateinit var startButton: Button

    @FXML
    lateinit var sessionProgressIndicator: ProgressIndicator

    @FXML
    lateinit var loadXmlButton: Button

    @FXML
    lateinit var xmlfileHBox: HBox

    @FXML
    lateinit var xmlFileTextField: TextField

    @FXML
    lateinit var xmlFileButton: Button


    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        setupDragAndDrop()
    }

    internal fun setup(screenBuilderController: ScreenBuilderController) {

        this.screenBuilderController = screenBuilderController

        setupViewModel()

        setupButtonActions()
        setupBinding()
    }

    private fun setupViewModel() {

        settingsViewModel = screenBuilderController.screenBuilderViewModel.settingsViewModel
    }

    private fun setupBinding() {

        projectDirectoryTextField.textProperty().bindBidirectional(settingsViewModel.projectDirectoryProperty)
        testrunFileTextField.textProperty().bindBidirectional(settingsViewModel.testrunFileProperty)
        androidRadioButton.selectedProperty().bindBidirectional(settingsViewModel.androidSelectedProperty)
        iosRadioButton.selectedProperty().bindBidirectional(settingsViewModel.iosSelectedProperty)
        xmlFileTextField.textProperty().bindBidirectional(screenBuilderViewModel.editViewModel.xmlFileProperty)

        screenBuilderViewModel.disabledProperty.bindDisable(
            testrunFileTextField, testrunFileButton,
            androidRadioButton, iosRadioButton, startButton,
            loadXmlButton, xmlFileTextField, xmlFileButton
        )
        sessionProgressIndicator.visibleProperty().bind(screenBuilderViewModel.disabledProperty)
    }

    private fun setupDragAndDrop() {

        testrunfileHBox.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        testrunfileHBox.setOnDragDropped { e ->
            val dragboard = e.dragboard
            if (dragboard.hasFiles()) {
                val file = dragboard.files.first()
                settingsViewModel.testrunFileProperty.set(file.path)
            }
            e.isDropCompleted = true
            e.consume()
        }
        xmlfileHBox.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        xmlfileHBox.setOnDragDropped { e ->
            val dragboard = e.dragboard
            if (dragboard.hasFiles()) {
                val file = dragboard.files.first()
                screenBuilderViewModel.editViewModel.xmlFileProperty.set(file.path)
            }
            e.isDropCompleted = true
            e.consume()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setupButtonActions() {

        testrunFileButton.setOnAction {
            var initDir = settingsViewModel.testrunFile
            if (initDir.isBlank()) {
                initDir = settingsViewModel.projectDirectory
            }
            val file = DialogHelper.openFileDialog(initialDirectory = initDir, extensions = "properties")
            if (file != null) {
                settingsViewModel.testrunFileProperty.set(file.path)
            }
        }
        startButton.setOnAction {
            if (validateTestrunFile().not()) {
                return@setOnAction
            }
            screenBuilderViewModel.savePreferences()
            screenBuilderController.editController.clearImagePane()
            screenBuilderViewModel.disable()

            runAsync(
                backgroundAction = {
                    settingsViewModel.startSession()
                },
                onProgressEnd = {
                    screenBuilderViewModel.enable()
                }
            ) {
                screenBuilderController.selectTab("Edit")
            }
        }
        xmlFileButton.setOnAction {
            if (validateTestrunFile().not()) {
                return@setOnAction
            }
            screenBuilderController.editController.xmlFileButtonAction()
        }
        loadXmlButton.setOnAction {
            screenBuilderController.editController.loadXmlButtonAction()
        }
    }

    fun validateTestrunFile(): Boolean {

        val testrunFile = settingsViewModel.testrunFile
        if (testrunFile.isBlank()) {
            DialogHelper.showError("testrun file is required.")
            return false
        }
        if (testrunFile.toPath().exists().not()) {
            DialogHelper.showError("File not found.", testrunFile)
            return false
        }
        return true
    }

    fun refresh() {

    }
}