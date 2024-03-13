package shirates.builder.utility

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import shirates.builder.BuilderApplication
import shirates.core.UserVar
import shirates.core.utility.exists
import shirates.core.utility.toPath
import java.io.File

object DialogHelper {

    /**
     * openDirectoryDialog
     */
    fun openDirectoryDialog(
        initialDirectory: String,
        title: String = "Select folder"
    ): File? {
        val dirChooser = DirectoryChooser()
        dirChooser.title = title
        val initialDir =
            if (initialDirectory.isNotBlank() && initialDirectory.toPath().exists()) initialDirectory
            else System.getProperty("user.home")
        dirChooser.initialDirectory = File(initialDir)
        val directory = dirChooser.showDialog(shirates.builder.BuilderApplication.currentStage)
        return directory
    }

    /**
     * openFileDialog
     */
    fun openFileDialog(
        initialDirectory: String? = "",
        title: String = "Select file",
        extensions: String = ""
    ): File? {
        val fileChooser = FileChooser()
        fileChooser.title = title
        val exts = extensions.split(",").filter { it.isNotBlank() }
        for (ext in exts) {
            val filter = ExtensionFilter("$ext file (*.$ext)", "*.$ext")
            fileChooser.extensionFilters.add(filter)
        }

        val f = initialDirectory.toPath().toFile()
        val initDir = if (f.isFile) f.parentFile else f
        if (initDir.toPath().exists()) {
            fileChooser.initialDirectory = initDir
        } else {
            fileChooser.initialDirectory = UserVar.userHome.toFile()
        }

        val file = fileChooser.showOpenDialog(shirates.builder.BuilderApplication.currentStage)
        return file
    }

    /**
     * createAlert
     */
    fun createAlert(
        alertType: AlertType,
        headerText: String,
        content: String? = null,
        title: String? = null,
    ): Alert {
        val alert = Alert(alertType)
        alert.headerText = headerText
        alert.contentText = content
        alert.title = title
        return alert
    }

    /**
     * showError
     */
    fun showError(
        headerText: String,
        content: String? = null,
        title: String? = null,
    ) {
        val alert = createErrorAlert(headerText, content, title)
        alert.showAndWait()
    }

    /**
     * createErrorAlert
     */
    fun createErrorAlert(
        headerText: String,
        content: String? = null,
        title: String? = null
    ): Alert {
        val alert = createAlert(
            alertType = AlertType.ERROR,
            headerText = headerText,
            content = content,
            title = title,
        )
        return alert
    }

    /**
     * showWarning
     */
    fun showWarning(
        headerText: String,
        content: String? = null,
        title: String? = null,
    ) {
        val alert = createWarningAlert(headerText, content, title)
        alert.showAndWait()
    }

    /**
     * createWarningAlert
     */
    fun createWarningAlert(
        headerText: String,
        content: String? = null,
        title: String? = null
    ): Alert {
        val alert = createAlert(
            alertType = AlertType.WARNING,
            headerText = headerText,
            content = content,
            title = title,
        )
        return alert
    }

    /**
     * showInformation
     */
    fun showInformation(
        headerText: String,
        content: String? = null,
        title: String? = null,
    ) {
        val alert = createInformationAlert(headerText, content, title)
        alert.showAndWait()
    }

    /**
     * createInformationAlert
     */
    fun createInformationAlert(
        headerText: String,
        content: String? = null,
        title: String? = null
    ): Alert {
        val alert = createAlert(
            alertType = AlertType.INFORMATION,
            headerText = headerText,
            content = content,
            title = title,
        )
        return alert
    }

    /**
     * showOkCancel
     */
    fun showOkCancel(
        headerText: String,
        content: String? = null,
        title: String? = null,
    ): ButtonType {
        val alert = createAlert(
            alertType = AlertType.CONFIRMATION,
            headerText = headerText,
            content = content,
            title = title,
        )
        val result = alert.showAndWait().orElse(ButtonType.CANCEL)
        return result
    }

}