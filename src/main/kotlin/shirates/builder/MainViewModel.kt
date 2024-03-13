package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import java.util.prefs.Preferences

class MainViewModel {

    val settingsViewModel = SettingsViewModel(this)
    val editViewModel = EditViewModel(this)
    val previewViewModel = PreviewViewModel(this)

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

    fun savePreferences() {

        val prefs = Preferences.userNodeForPackage(javaClass)
        prefs.put("testrunFileProperty", settingsViewModel.testrunFileProperty.value)
        prefs.put("android", settingsViewModel.androidSelectedProperty.value.toString())
        prefs.put("xmlFile", editViewModel.xmlFile)
        prefs.put("satelliteAutoCandidate", editViewModel.satellitesViewModel.isAutoCheckBoxSelected.toString())
    }

    fun getPreferences(): Preferences {
        return Preferences.userNodeForPackage(javaClass)
    }

    fun loadPreferences() {

        val prefs = getPreferences()
        settingsViewModel.testrunFileProperty.set(prefs.get("testrunFileProperty", ""))
        val isAndroidSelected = prefs.get("android", "true").toBoolean()
        if (isAndroidSelected) {
            settingsViewModel.androidSelectedProperty.set(true)
        } else {
            settingsViewModel.iosSelectedProperty.set(true)
        }
        editViewModel.xmlFileProperty.set(prefs.get("xmlFile", ""))
        val isAutoCheckBoxSelected = prefs.get("satelliteAutoCandidate", "true").toBoolean()
        editViewModel.satellitesViewModel.isAutoCheckBoxSelectedProperty.set(isAutoCheckBoxSelected)
    }

    fun refresh() {

        editViewModel.refresh()
        previewViewModel.refresh()
    }


}