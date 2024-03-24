package shirates.builder

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import shirates.builder.utility.ProjectHelper
import shirates.core.UserVar
import shirates.core.configuration.PropertiesManager
import shirates.core.configuration.TestConfig
import shirates.core.configuration.TestProfile
import shirates.core.configuration.repository.ScreenRepository
import shirates.core.customobject.CustomFunctionRepository
import shirates.core.driver.TestContext
import shirates.core.driver.TestDriver
import shirates.core.driver.commandextension.*
import shirates.core.driver.testDrive
import shirates.core.logging.TestLog
import shirates.core.macro.MacroRepository
import shirates.core.server.AppiumServerManager
import shirates.core.utility.fileExists
import shirates.core.utility.toPath
import java.nio.file.Path

class SettingsViewModel(
    val screenBuilderViewModel: ScreenBuilderViewModel
) {
    val editViewModel: EditViewModel
        get() {
            return screenBuilderViewModel.editViewModel
        }

    val projectDirectory: String
        get() {
            return projectDirectoryProperty.value ?: ""
        }
    val projectDirectoryProperty = SimpleStringProperty()

    val testrunFile: String
        get() {
            return testrunFileProperty.value ?: ""
        }
    val testrunFileProperty = SimpleStringProperty()

    var androidSelected: Boolean
        get() {
            return androidSelectedProperty.value
        }
        set(value) {
            androidSelectedProperty.set(value)
        }
    val androidSelectedProperty = SimpleBooleanProperty()

    var androidVersion: String
        get() {
            return androidVersionProperty.value
        }
        set(value) {
            androidVersionProperty.set(value)
        }
    val androidVersionProperty = SimpleStringProperty("")

    var iosSelected: Boolean
        get() {
            return iosSelectedProperty.value
        }
        set(value) {
            iosSelectedProperty.set(value)
        }
    val iosSelectedProperty = SimpleBooleanProperty()

    var iosVersion: String
        get() {
            return iosVersionProperty.value
        }
        set(value) {
            iosVersionProperty.set(value)
        }
    val iosVersionProperty = SimpleStringProperty("")

    var profileName: String
        get() {
            return profileNameProperty.value
        }
        set(value) {
            profileNameProperty.set(value)
        }
    val profileNameProperty = SimpleStringProperty("")

    var appPackageFile: String
        get() {
            return appPackageFileProperty.value
        }
        set(value) {
            appPackageFileProperty.set(value)
        }
    val appPackageFileProperty = SimpleStringProperty("")

    var packageOrBundleId: String
        get() {
            return packageOrBundleIdProperty.value
        }
        set(value) {
            packageOrBundleIdProperty.set(value)
        }
    val packageOrBundleIdProperty = SimpleStringProperty("")

    var language: String
        get() {
            return languageProperty.value
        }
        set(value) {
            languageProperty.set(value)
        }
    val languageProperty = SimpleStringProperty("")

    var locale: String
        get() {
            return localeProperty.value
        }
        set(value) {
            localeProperty.set(value)
        }
    val localeProperty = SimpleStringProperty("")


    /**
     * Other Properties
     */
    var testConfig: TestConfig? = null

    var testProfile = TestProfile()
    lateinit var configPath: String

    init {
        androidSelectedProperty.set(true)
        testrunFileProperty.addListener { _, _, new ->
            System.setProperty(UserVar.SHIRATES_PROJECT_DIR, "")
            val projectDir = ProjectHelper.getProjectDirectory(new)
            projectDirectoryProperty.set(projectDir)
            System.setProperty(UserVar.SHIRATES_PROJECT_DIR, projectDir)
        }
    }

    fun loadFromTestrunFile() {

        System.setProperty(UserVar.SHIRATES_PROJECT_DIR, projectDirectory)
        PropertiesManager.setup(testrunFile = testrunFile)
        val os = if (androidSelected) "android" else "ios"
        PropertiesManager.setPropertyValue("os", os)
        if (PropertiesManager.properties.containsKey("${os}.configFile").not()) {
            return
        }

        configPath = projectDirectory.toPath().resolve(PropertiesManager.configFile).toString()

        val testConfigName = configPath.toPath().toFile().nameWithoutExtension

        // testResults
        if (testConfigName.isNotBlank()) {
            TestLog.setupTestResults(
                testResults = PropertiesManager.testResults,
                testConfigName = testConfigName
            )
        }

        // setup config
        testProfile = setupConfigAndProfile(
            configPath = configPath.toPath(),
            profileName = PropertiesManager.profile
        )

        // setup ScreenRepository
        ScreenRepository.setup(
            screensDirectory = configPath.toPath().parent.resolve("screens"),
            importDirectories = testProfile.testConfig!!.importScreenDirectories
        )

        if (testProfile.udid.isNotBlank()) {
            profileName = testProfile.udid
        }
        appPackageFile = testProfile.appPackageFile ?: ""
        packageOrBundleId = testProfile.packageOrBundleId ?: ""
        language = testProfile.language
        locale = testProfile.locale
    }

    fun startSession() {

        screenBuilderViewModel.editViewModel.clearEditData()
        TestLog.clear()
        TestLog.write("")
        TestLog.write("Starting new session")
        setupProfile()

        // testContext
        val testContext = TestContext(profile = testProfile)
        testContext.enableIrregularHandler = false

        // CustomFunctionRepository
        CustomFunctionRepository.initialize()

        // MacroRepository
        MacroRepository.setup()

        // testContext
        TestDriver.setupContext(testContext = testContext)

        // profile
        testProfile.completeProfileWithTestMode()
        if (testContext.isLocalServer) {
            testProfile.completeProfileWithDeviceInformation()
        }

        TestLog.currentTestClass = ScreenBuilderViewModel::class.java

        // Appium Server
        if (testContext.isLocalServer) {
            AppiumServerManager.setupAppiumServerProcess(
                sessionName = TestLog.currentTestClassName,
                profile = testProfile
            )
        }

        // AppiumDriver
        TestDriver.createAppiumDriver()

        if (testDrive.isAppInstalled().not()) {
            testDrive.installApp()
        }
        if (testDrive.isApp()) {
            testDrive.terminateApp()
        }
        testDrive.launchApp(sync = false)

        editViewModel.workDirectory = TestLog.directoryForLog.resolve("work").toString()
        if (editViewModel.workDirectory.fileExists().not()) {
            editViewModel.workDirectory.toPath().toFile().mkdir()
        }

        TestLog.info("Click [Capture] button in Screen Builder to capture screen.")
    }

    fun setupProfile(): TestProfile {
        System.setProperty(UserVar.SHIRATES_PROJECT_DIR, projectDirectory)
        PropertiesManager.setup()
        PropertiesManager.setPropertyValue("screenshotScale", "1.0")
        if (androidSelected) {
            PropertiesManager.properties.set("os", "android")
        } else {
            PropertiesManager.properties.set("os", "ios")
        }

        val testConfigName = "autoConfig"

        // testResults
        TestLog.setupTestResults(
            testResults = PropertiesManager.testResults,
            testConfigName = testConfigName
        )

        // Profile
        val profile = TestProfile()
        profile.profileName = profileName
        profile.appPackageFile = appPackageFile
        profile.packageOrBundleId = packageOrBundleId
        profile.language = language
        profile.locale = locale
        if (androidSelected) {
            profile.platformName = "android"
            profile.platformVersion = androidVersion
            if (profile.profileName.isBlank()) {
                profile.profileName = "Android $androidVersion"
            }
        } else {
            profile.platformName = "ios"
            if (iosVersion.contains(".").not()) {
                iosVersion = "$iosVersion.0"
            }
            profile.platformVersion = iosVersion
            if (profile.profileName.isBlank()) {
                profile.profileName = iosVersion
            }
        }

        profile.appIconName = "App Icon Name"
        profile.appiumServerUrl = PropertiesManager.getPropertyValue("appiumServerUrl")
//        if (profile.isiOS) {
//            profile.capabilities["appium:bundleId"] = profile.packageOrBundleId
//        }
        profile.appiumPath = "appium"

        if (profile.testConfig?.importScreenDirectories.isNullOrEmpty().not()) {
            // setup ScreenRepository
            ScreenRepository.setup(
                screensDirectory = configPath.toPath().parent.resolve("screens"),
                importDirectories = profile.testConfig!!.importScreenDirectories
            )
        }

        testProfile = profile
        return testProfile
    }

    private fun setupConfigAndProfile(
        configPath: Path,
        profileName: String
    ): TestProfile {
        TestLog.info("Loading config.(configFile=$configPath, profileName=$profileName)")
        testConfig = TestConfig(configPath.toString())
        if (testConfig!!.profileMap.containsKey(profileName)) {
            return testConfig!!.profileMap[profileName]!!
        }
        val defaultProfile = testConfig!!.profileMap["_default"]!!
        defaultProfile.profileName = profileName
        return defaultProfile
    }

}