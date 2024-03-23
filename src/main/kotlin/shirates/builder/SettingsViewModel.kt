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
import shirates.core.driver.befavior.LanguageHelperAndroid
import shirates.core.driver.befavior.LanguageHelperIos
import shirates.core.driver.commandextension.canSelect
import shirates.core.driver.commandextension.isAppInstalled
import shirates.core.driver.commandextension.launchApp
import shirates.core.driver.commandextension.pressHome
import shirates.core.driver.testDrive
import shirates.core.logging.Message
import shirates.core.logging.TestLog
import shirates.core.macro.MacroRepository
import shirates.core.server.AppiumServerManager
import shirates.core.utility.toPath
import java.nio.file.Path

class SettingsViewModel(
    val screenBuilderViewModel: ScreenBuilderViewModel
) {

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

    var startupPackageOrBundleId: String
        get() {
            return startupPackageOrBundleIdProperty.value
        }
        set(value) {
            startupPackageOrBundleIdProperty.set(value)
        }
    val startupPackageOrBundleIdProperty = SimpleStringProperty("")

    var startupActivity: String
        get() {
            return startupActivityProperty.value
        }
        set(value) {
            startupActivityProperty.set(value)
        }
    val startupActivityProperty = SimpleStringProperty("")

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

    var udid: String
        get() {
            return udidProperty.value
        }
        set(value) {
            udidProperty.set(value)
        }
    val udidProperty = SimpleStringProperty("")

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

        appPackageFile = testProfile.appPackageFile ?: ""
        packageOrBundleId = testProfile.packageOrBundleId ?: ""
        startupPackageOrBundleId = testProfile.startupPackageOrBundleId ?: ""
        startupActivity = testProfile.startupActivity ?: ""
        language = testProfile.language
        locale = testProfile.locale
        udid = testProfile.udid
    }

    fun startSession() {

        screenBuilderViewModel.editViewModel.clearEditData()

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
        val lastProfile = TestDriver.lastTestContext.profile
        if (lastProfile.profileName.isBlank()) {
            // First time
            TestDriver.createAppiumDriver()
        } else {
            // Second time or later
            if (testProfile.isSameProfile(lastProfile) && TestDriver.canReuse) {
                // Reuse Appium session if possible
                TestLog.info(
                    Message.message(
                        id = "reusingAppiumDriverSession",
                        arg1 = configPath.toString(),
                        arg2 = testProfile.profileName
                    )
                )
                TestDriver.testContext = TestDriver.lastTestContext
            } else {
                // Reset Appium session
                TestDriver.resetAppiumSession()
            }
        }
        screenBuilderViewModel.editViewModel.xmlFile = ""

        if (testProfile.isAndroid) {
            setLanguageAndroid()
            if (testProfile.startupPackageOrBundleId != null) {
                if (testDrive.isAppInstalled(packageOrBundleId = testProfile.startupPackageOrBundleId)) {
                    if (testProfile.startupActivity != null) {
                        testDrive.launchApp(appNameOrAppIdOrActivityName = "${testProfile.startupPackageOrBundleId}/${testProfile.startupActivity}")
                    } else {
                        testDrive.launchApp(testProfile.startupPackageOrBundleId!!)
                    }
                }
            } else {
                testDrive.pressHome()
            }
        } else {
            LanguageHelperIos.setLanguage()
        }

        TestLog.info("Click [Capture] button in Screen Builder to capture screen.")
    }

    private fun setLanguageAndroid() {

        if (testProfile.language == "ja") {
            var isJa = testDrive.canSelect("*ホーム*||*設定*")
            if (isJa) return

            LanguageHelperAndroid.gotoLocaleSettings()
            val language = LanguageHelperAndroid.getLanguage()
            isJa = language == "日本語"
            if (isJa) return
        }
        LanguageHelperAndroid.setLanguage("日本語", "日本")
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
//        testProfile = TestProfile()

        val profile = testProfile
        profile.packageOrBundleId = packageOrBundleId
        profile.startupPackageOrBundleId = startupPackageOrBundleId
        profile.startupActivity = startupActivity
        profile.language = language
        profile.locale = locale
        profile.udid = udid
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