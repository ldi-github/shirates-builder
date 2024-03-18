package shirates.builder

import javafx.beans.property.SimpleStringProperty
import shirates.builder.utility.DialogHelper
import shirates.core.UserVar
import shirates.core.utility.toPath
import shirates.spec.code.model.CodeGenerationExecutor

class TemplateCodeGeneratorViewModel(
) {

    val inputDirectoryProperty = SimpleStringProperty()
    var inputDirectory: String
        get() {
            return inputDirectoryProperty.value ?: ""
        }
        set(value) {
            inputDirectoryProperty.set(value)
        }

    val outputDirectoryProperty = SimpleStringProperty(UserVar.DOWNLOADS)
    var outputDirectory: String
        get() {
            return outputDirectoryProperty.value ?: ""
        }
        set(value) {
            outputDirectoryProperty.set(value)
        }

    init {
    }

    fun execute() {

        try {
            CodeGenerationExecutor().execute(
                codegenOutputFile = outputDirectory.toPath(),
                specInputDirectory = inputDirectory.toPath()
            )
        } catch (t: Throwable) {
            DialogHelper.showError(t.message ?: "")
        }
    }
}