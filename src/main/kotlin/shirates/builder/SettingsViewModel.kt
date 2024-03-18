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
import shirates.core.logging.Message
import shirates.core.logging.TestLog
import shirates.core.macro.MacroRepository
import shirates.core.server.AppiumServerManager
import shirates.core.utility.toPath
import java.nio.file.Path

class SettingsViewModel(
    val screenBuilderViewModel: ScreenBuilderViewModel
) {

    val projectDirectoryProperty = SimpleStringProperty()
    val projectDirectory: String
        get() {
            return projectDirectoryProperty.value ?: ""
        }
    val testrunFileProperty = SimpleStringProperty()
    val testrunFile: String
        get() {
            return testrunFileProperty.value ?: ""
        }

    val androidSelectedProperty = SimpleBooleanProperty()
    val iosSelectedProperty = SimpleBooleanProperty()

    /**
     * Other Properties
     */
    var testConfig: TestConfig? = null

    lateinit var profile: TestProfile
    lateinit var configPath: String

    init {
        androidSelectedProperty.set(true)
        testrunFileProperty.addListener { o, old, new ->
            val projectDir = ProjectHelper.getProjectDirectory(new)
            projectDirectoryProperty.set(projectDir)
        }
    }

    fun startSession() {

        screenBuilderViewModel.editViewModel.clearEditData()

        setupConfig()

        // testContext
        val testContext = TestContext(profile = profile)
        testContext.enableIrregularHandler = false

        // CustomFunctionRepository
        CustomFunctionRepository.initialize()

        // MacroRepository
        MacroRepository.setup()

        // testContext
        TestDriver.setupContext(testContext = testContext)

        // profile
        profile.completeProfileWithTestMode()
        if (testContext.isLocalServer) {
            profile.completeProfileWithDeviceInformation()
        }
        profile.validate()

        TestLog.currentTestClass = ScreenBuilderViewModel::class.java

        // Appium Server
        if (testContext.isLocalServer) {
            AppiumServerManager.setupAppiumServerProcess(
                sessionName = TestLog.currentTestClassName,
                profile = profile
            )
        }

        // AppiumDriver
        val lastProfile = TestDriver.lastTestContext.profile
        if (lastProfile.profileName.isBlank()) {
            // First time
            TestDriver.createAppiumDriver()
        } else {
            // Second time or later
            if (profile.isSameProfile(lastProfile) && TestDriver.canReuse) {
                // Reuse Appium session if possible
                TestLog.info(
                    Message.message(
                        id = "reusingAppiumDriverSession",
                        arg1 = configPath.toString(),
                        arg2 = profile.profileName
                    )
                )
                TestDriver.testContext = TestDriver.lastTestContext
            } else {
                // Reset Appium session
                TestDriver.resetAppiumSession()
            }
        }
        screenBuilderViewModel.editViewModel.xmlFile = ""
        TestLog.info("Click [Capture] button in Screen Builder to capture screen.")
    }

    fun setupConfig() {

        System.setProperty(UserVar.SHIRATES_PROJECT_DIR, projectDirectory)
        PropertiesManager.setup(testrunFile = testrunFile)
        PropertiesManager.setPropertyValue("screenshotScale", "1.0")
        if (androidSelectedProperty.value) {
            PropertiesManager.properties.set("os", "android")
        } else {
            PropertiesManager.properties.set("os", "ios")
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
        profile = setupConfigAndProfile(
            configPath = configPath.toPath(),
            profileName = PropertiesManager.profile
        )

        // setup ScreenRepository
        ScreenRepository.setup(
            screensDirectory = configPath.toPath().parent.resolve("screens"),
            importDirectories = profile.testConfig!!.importScreenDirectories
        )
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