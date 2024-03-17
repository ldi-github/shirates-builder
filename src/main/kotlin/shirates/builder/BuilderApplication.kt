package shirates.builder

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class BuilderApplication : Application() {
    override fun start(stage: Stage) {
        shirates.builder.BuilderApplication.Companion.current = this
        shirates.builder.BuilderApplication.Companion.currentStage = stage
        val fxmlLoader = FXMLLoader(shirates.builder.BuilderApplication::class.java.getResource("main-view.fxml"))
        val scene = Scene(fxmlLoader.load())
        scene.stylesheets.addAll(
            this.javaClass.getResource("builder.css").toExternalForm()
        )

        stage.title = "Shirates Builder"
        stage.scene = scene
        stage.show()
    }

    companion object {
        lateinit var current: Application
        lateinit var currentStage: Stage
    }
}

object BuilderApplicationExecute {

    @JvmStatic
    fun main(args: Array<String>) {

        Application.launch(shirates.builder.BuilderApplication::class.java)
    }
}
