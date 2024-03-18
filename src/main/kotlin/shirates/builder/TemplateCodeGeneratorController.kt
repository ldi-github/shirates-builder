package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import shirates.builder.utility.DialogHelper
import shirates.builder.utility.draganddrop.acceptLink
import shirates.core.UserVar
import shirates.core.testcode.normalize
import java.net.URL
import java.util.*

class TemplateCodeGeneratorController : Initializable {

    lateinit var generatorViewModel: TemplateCodeGeneratorViewModel

    @FXML
    lateinit var inputDirectoryTextField: TextField

    @FXML
    lateinit var specReportHBox: HBox

    @FXML
    lateinit var outputDirectoryTextField: TextField

    @FXML
    lateinit var outputDirectoryHBox: HBox

    @FXML
    lateinit var specReportFileButton: Button

    @FXML
    lateinit var outputDirectoryButton: Button

    @FXML
    lateinit var generateTemplateButton: Button


    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        setupViewModel()
        setupButtonActions()
        setupBinding()
        setupDragAndDrop()
    }

    private fun setupViewModel() {

        generatorViewModel = TemplateCodeGeneratorViewModel()
    }

    private fun setupButtonActions() {

        specReportFileButton.setOnAction {
            val file = DialogHelper.openFileDialog(initialDirectory = UserVar.DOWNLOADS, extensions = "xlsx")
            if (file != null) {
                generatorViewModel.inputDirectory = file.path.normalize()
            }
        }
        outputDirectoryButton.setOnAction {
            val file = DialogHelper.openDirectoryDialog(initialDirectory = UserVar.DOWNLOADS)
            if (file != null) {
                generatorViewModel.outputDirectory = file.path.normalize()
            }
        }
        generateTemplateButton.setOnAction {
            generatorViewModel.execute()
        }
    }

    private fun setupBinding() {

        inputDirectoryTextField.textProperty().bindBidirectional(generatorViewModel.inputDirectoryProperty)
        outputDirectoryTextField.textProperty().bindBidirectional(generatorViewModel.outputDirectoryProperty)
    }

    private fun setupDragAndDrop() {

        specReportHBox.setOnDragOver { e ->
            e.acceptLink()
            e.consume()
        }
        specReportHBox.setOnDragDropped { e ->
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