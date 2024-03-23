package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*

class ScreenBuilderController : Initializable {

    lateinit var screenBuilderViewModel: ScreenBuilderViewModel
    lateinit var settingsController: SettingsController
    lateinit var editController: EditController
    lateinit var previewController: PreviewController

    @FXML
    lateinit var rootNode: VBox

    @FXML
    lateinit var mainTabPane: TabPane

    @FXML
    lateinit var settingsTab: Tab

    @FXML
    lateinit var editTab: Tab

    @FXML
    lateinit var previewTab: Tab


    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {

        setupViewModel()

        setupSettingsView()
        setupEditView()
        setupPreviewView()

        setupObjectListener()

        screenBuilderViewModel.loadPreferences()
    }

    private fun setupViewModel() {

        screenBuilderViewModel = ScreenBuilderViewModel()
    }

    private fun setupSettingsView() {
        val fxmlLoader = FXMLLoader(BuilderApplication::class.java.getResource("settings-view.fxml"))
        val node = fxmlLoader.load<Node>()
        settingsTab.content = node
        settingsController = fxmlLoader.getController()
        settingsController.setup(screenBuilderController = this)
    }

    private fun setupEditView() {
        val fxmlLoader = FXMLLoader(BuilderApplication::class.java.getResource("edit-view.fxml"))
        val node = fxmlLoader.load<Node>()
        editTab.content = node
        editController = fxmlLoader.getController()
        editController.setup(screenBuilderController = this)
    }

    private fun setupPreviewView() {
        val fxmlLoader = FXMLLoader(shirates.builder.BuilderApplication::class.java.getResource("preview-view.fxml"))
        val node = fxmlLoader.load<Node>()
        previewTab.content = node
        previewController = fxmlLoader.getController()
        previewController.setup(screenBuilderController = this)
    }

    private fun setupObjectListener() {
        mainTabPane.selectionModel.selectedItemProperty().addListener { o, old, new ->
            if (new == previewTab) {
                screenBuilderViewModel.refresh()
            }
        }
    }

    fun selectTab(name: String) {

        val tab = mainTabPane.tabs.firstOrNull() { it.text.trim() == name } ?: return
        mainTabPane.selectionModel.select(tab)
    }

    fun refresh() {

        settingsController.refresh()
        editController.refresh()
        previewController.refresh()
    }
}