package shirates.builder

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import java.net.URL
import java.util.*

class MainController : Initializable {

    var screenBuilderStage: Stage? = null

    var templateCodeGeneratorStage: Stage? = null

    @FXML
    lateinit var screenBuilderButton: Button

    @FXML
    lateinit var templateCodeGeneratorButton: Button

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {

        screenBuilderButton.setOnAction {
            openScreenBuilder()
        }
        templateCodeGeneratorButton.setOnAction {
            openTemplateCodeGenerator()
        }
    }

    private fun createStage(fxml: String): Stage {
        val fxmlLoader = FXMLLoader(BuilderApplication::class.java.getResource(fxml))
        val node = fxmlLoader.load<Node>()
        val scene = Scene(node as Parent)
        val stage = Stage()
        stage.scene = scene
        return stage
    }

    fun openScreenBuilder() {

        if (screenBuilderStage != null) {
            screenBuilderStage!!.show()
            return
        }
        screenBuilderStage = createStage("screen-builder-view.fxml")
        screenBuilderStage!!.title = "Screen Builder"
        screenBuilderStage!!.show()
        screenBuilderStage!!.setOnCloseRequest {
            screenBuilderStage = null
        }
    }

    fun openTemplateCodeGenerator() {

        if (templateCodeGeneratorStage != null) {
            templateCodeGeneratorStage!!.show()
            return
        }

        templateCodeGeneratorStage = createStage("template-code-generator-view.fxml")
        templateCodeGeneratorStage!!.title = "Template Code Generator"
        templateCodeGeneratorStage!!.show()
        templateCodeGeneratorStage!!.setOnCloseRequest {
            templateCodeGeneratorStage = null
        }
    }
}