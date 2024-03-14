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

    @FXML
    lateinit var screenBuilderButton: Button


    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {

        screenBuilderButton.setOnAction {
            openScreenBuilder()
        }
    }

    fun openScreenBuilder() {

        if (screenBuilderStage != null) {
            screenBuilderStage!!.show()
            return
        }

        val fxmlLoader = FXMLLoader(BuilderApplication::class.java.getResource("screen-builder-view.fxml"))
        val node = fxmlLoader.load<Node>()
        val scene = Scene(node as Parent)
        val stage = Stage()
        stage.scene = scene
        stage.show()
        stage.setOnCloseRequest {
            screenBuilderStage = null
        }
        screenBuilderStage = stage
    }
}