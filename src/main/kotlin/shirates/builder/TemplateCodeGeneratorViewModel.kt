package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import shirates.builder.utility.DialogHelper
import shirates.core.UserVar
import shirates.core.configuration.PropertiesManager
import shirates.core.utility.toPath
import shirates.spec.code.model.CodeGenerationExecutor
import java.util.prefs.Preferences

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

    val logLanguageProperty = SimpleStringProperty("")
    var logLanguage: String
        get() {
            return logLanguageProperty.value ?: ""
        }
        set(value) {
            logLanguageProperty.set(value)
        }

    var disabled: Boolean
        get() {
            return disabledProperty.value
        }
        set(value) {
            disabledProperty.set(value)
        }
    val disabledProperty = SimpleBooleanProperty()

    fun disable() {
        disabled = true
    }

    fun enable() {
        disabled = false
    }

    init {
        PropertiesManager.setup()
        logLanguage = PropertiesManager.logLanguage
    }

    fun execute() {

        try {
            CodeGenerationExecutor().execute(
                codegenOutputFile = outputDirectory.toPath(),
                specInputDirectory = inputDirectory.toPath(),
                logLanguage = logLanguage
            )
        } catch (t: Throwable) {
            DialogHelper.showError(t.message ?: "")
        }
    }

    fun getPreferences(): Preferences {

        return Preferences.userNodeForPackage(javaClass)
    }

    fun savePreferences() {

        val prefs = getPreferences()
        prefs.put("outputDirectory", outputDirectory)
        prefs.put("inputDirectory", inputDirectory)
        prefs.put("logLanguage", logLanguage)
    }

    fun loadPreferences() {

        val prefs = getPreferences()
        val output = prefs.get("outputDirectory", "")
        if (output.isNotBlank()) {
            outputDirectory = output
        }
        val input = prefs.get("inputDirectory", "")
        if (input.isNotBlank()) {
            inputDirectory = input
        }
        val lang = prefs.get("logLanguage", "")
        if (lang.isNotBlank()) {
            logLanguage = lang
        }
    }

}