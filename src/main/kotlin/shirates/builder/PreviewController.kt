package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import shirates.builder.utility.DialogHelper
import shirates.core.utility.toPath
import java.awt.Desktop
import java.net.URL
import java.util.*

class PreviewController : Initializable {

    lateinit var mainController: MainController
    val mainViewModel: MainViewModel
        get() {
            return mainController.mainViewModel
        }

    lateinit var previewViewModel: PreviewViewModel

    @FXML
    lateinit var rootNode: AnchorPane

    @FXML
    private lateinit var keyTextField: TextField

    @FXML
    lateinit var nicknameFileTextField: TextField

    @FXML
    private lateinit var openDirectoryButton: Button

    @FXML
    private lateinit var saveNicknameFileButton: Button

    @FXML
    private lateinit var previewJsonTextArea: TextArea


    override fun initialize(p0: URL?, p1: ResourceBundle?) {

    }

    internal fun setup(mainController: MainController) {

        this.mainController = mainController

        setupViewModel()

        setupButtonActions()
        setupBinding()
    }

    private fun setupViewModel() {

        previewViewModel = mainViewModel.previewViewModel

    }

    private fun setupBinding() {

        keyTextField.textProperty().bindBidirectional(previewViewModel.keyProperty)
        nicknameFileTextField.textProperty().bind(previewViewModel.nicknameFileProperty)
        previewJsonTextArea.textProperty().bind(previewViewModel.previewJsonProperty)
    }

    private fun setupButtonActions() {

        openDirectoryButton.setOnAction {
            if (previewViewModel.validateForOpenDirectory().not()) {
                return@setOnAction
            }
            Desktop.getDesktop().open(previewViewModel.directory.toPath().toFile())
        }
        saveNicknameFileButton.setOnAction {
            if (previewViewModel.validateForSave().not()) {
                return@setOnAction
            }
            previewViewModel.saveNicknameFile()
            DialogHelper.showInformation("Saved.", content = previewViewModel.nicknameFileShort)
        }
    }

    fun refresh() {

    }
}