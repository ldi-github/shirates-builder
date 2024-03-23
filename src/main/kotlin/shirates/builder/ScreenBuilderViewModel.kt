package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import java.util.prefs.Preferences

class ScreenBuilderViewModel {

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
        with(settingsViewModel) {
            prefs.put("testrunFileProperty", testrunFileProperty.value)
            prefs.put("android", androidSelectedProperty.value.toString())
            prefs.put("androidVersion", androidVersion)
            prefs.put("iosVersion", iosVersion)
            prefs.put("packageOrBundleId", packageOrBundleId)
            prefs.put("startupPackageOrBundleId", startupPackageOrBundleId)
            prefs.put("startupActivity", startupActivity)
            prefs.put("language", language)
            prefs.put("locale", locale)
            prefs.put("udid", udid)
        }
        with(editViewModel) {
            prefs.put("xmlFile", xmlFile)
            prefs.put("satelliteAutoCandidate", satellitesViewModel.isAutoCheckBoxSelected.toString())
        }
    }

    fun getPreferences(): Preferences {
        return Preferences.userNodeForPackage(javaClass)
    }

    fun loadPreferences() {

        val prefs = getPreferences()
        with(settingsViewModel) {
            testrunFileProperty.set(prefs.get("testrunFileProperty", ""))
            val isAndroidSelected = prefs.get("android", "true").toBoolean()
            if (isAndroidSelected) {
                androidSelected = true
            } else {
                iosSelected = true
            }
            androidVersion = prefs.get("androidVersion", "")
            iosVersion = prefs.get("iosVersion", "")
            packageOrBundleId = prefs.get("packageOrBundleId", "")
            startupPackageOrBundleId = prefs.get("startupPackageOrBundleId", "")
            startupActivity = prefs.get("startupActivity", "")
            language = prefs.get("language", "")
            locale = prefs.get("locale", "")
            udid = prefs.get("udid", "")
        }
        with(editViewModel) {
            xmlFileProperty.set(prefs.get("xmlFile", ""))
            val isAutoCheckBoxSelected = prefs.get("satelliteAutoCandidate", "true").toBoolean()
            satellitesViewModel.isAutoCheckBoxSelectedProperty.set(isAutoCheckBoxSelected)
        }
    }

    fun refresh() {

        editViewModel.refresh()
        previewViewModel.refresh()
    }


}