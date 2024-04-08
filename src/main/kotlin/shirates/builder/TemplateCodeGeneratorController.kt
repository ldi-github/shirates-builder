package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import shirates.builder.utility.DialogHelper
import shirates.builder.utility.async
import shirates.builder.utility.bindDisable
import shirates.builder.utility.draganddrop.acceptLink
import shirates.core.UserVar
import shirates.core.testcode.normalize
import shirates.core.utility.toPath
import java.net.URL
import java.nio.file.Files
import java.util.*

class TemplateCodeGeneratorController : Initializable {

    lateinit var generatorViewModel: TemplateCodeGeneratorViewModel

    @FXML
    lateinit var outputDirectoryTextField: TextField

    @FXML
    lateinit var outputDirectoryHBox: HBox

    @FXML
    lateinit var outputDirectoryButton: Button

    @FXML
    lateinit var inputDirectoryTextField: TextField

    @FXML
    lateinit var inputDirectoryHBox: HBox

    @FXML
    lateinit var inputDirectoryButton: Button

    @FXML
    lateinit var logLanguageTextField: TextField

    @FXML
    lateinit var generateTemplateCodeButton: Button

    @FXML
    lateinit var generateTemplateProgressIndicator: ProgressIndicator


    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        setupViewModel()
        setupButtonActions()
        setupBinding()
        setupDragAndDrop()

        generatorViewModel.loadPreferences()
    }

    private fun setupViewModel() {

        generatorViewModel = TemplateCodeGeneratorViewModel()
    }

    private fun setupButtonActions() {

        outputDirectoryButton.setOnAction {
            val file = DialogHelper.openDirectoryDialog(initialDirectory = UserVar.DOWNLOADS)
            if (file != null) {
                generatorViewModel.outputDirectory = file.path.normalize()
            }
        }
        inputDirectoryButton.setOnAction {
            val file = DialogHelper.openFileDialog(initialDirectory = UserVar.DOWNLOADS, extensions = "xlsx")
            if (file != null) {
                generatorViewModel.inputDirectory = file.path.normalize()
            }
        }
        generateTemplateCodeButton.setOnAction {
            if (validate().not()) {
                return@setOnAction
            }
            generatorViewModel.savePreferences()
            generatorViewModel.disable()
            async()
                .background {
                    generatorViewModel.execute()
                }.progressEnd {
                    generatorViewModel.enable()
                }.start()
        }
    }

    fun validate(): Boolean {

        if (generatorViewModel.outputDirectory.isBlank()) {
            DialogHelper.showError("Output Directory is required.")
            return false
        }
        if (generatorViewModel.inputDirectory.isBlank()) {
            DialogHelper.showError("Input Directory is required.")
            return false
        }
        if (Files.exists(generatorViewModel.outputDirectory.toPath()).not()) {
            DialogHelper.showError("Output Directory not found.", generatorViewModel.outputDirectory)
            return false
        }
        if (Files.exists(generatorViewModel.inputDirectory.toPath()).not()) {
            DialogHelper.showError("Input Directory not found.", generatorViewModel.inputDirectory)
            return false
        }
        return true
    }

    private fun setupBinding() {

        inputDirectoryTextField.textProperty().bindBidirectional(generatorViewModel.inputDirectoryProperty)
        outputDirectoryTextField.textProperty().bindBidirectional(generatorViewModel.outputDirectoryProperty)
        logLanguageTextField.textProperty().bindBidirectional(generatorViewModel.logLanguageProperty)

        generateTemplateProgressIndicator.visibleProperty().bind(generatorViewModel.disabledProperty)
        generatorViewModel.disabledProperty.bindDisable(
            outputDirectoryTextField, outputDirectoryButton,
            inputDirectoryTextField, inputDirectoryButton,
            logLanguageTextField,
            generateTemplateCodeButton
        )
    }

    private fun setupDragAndDrop() {

        inputDirectoryHBox.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        inputDirectoryHBox.setOnDragDropped { e ->
            val dragboard = e.dragboard
            if (dragboard.hasFiles()) {
                val file = dragboard.files.first()
                generatorViewModel.inputDirectory = file.path.normalize()
            }
            e.isDropCompleted = true
            e.consume()
        }

        outputDirectoryHBox.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        outputDirectoryHBox.setOnDragDropped { e ->
            val dragboard = e.dragboard
            if (dragboard.hasFiles()) {
                val file = dragboard.files.first()
                generatorViewModel.outputDirectory = file.path.normalize()
            }
            e.isDropCompleted = true
            e.consume()
        }
    }

}