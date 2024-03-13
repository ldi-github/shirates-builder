package shirates.builder.utility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shirates.core.testcode.UnitTest
import shirates.core.utility.toPath

class ProjectHelperTest : UnitTest() {

    @Test
    fun getProjectDirectory() {

        run {
            // Arrange
            val file = "testData/project1/testrun.global.properties"
            // Act
            val actual = ProjectHelper.getProjectDirectory(file = file)
            // Assert
            val expected = "testData/project1".toPath().toString()
            assertThat(actual).isEqualTo(expected)
        }
        run {
            // Arrange
            val file = "testData/project1/testConfig/testrun.properties"
            // Act
            val actual = ProjectHelper.getProjectDirectory(file = file)
            // Assert
            val expected = "testData/project1".toPath().toString()
            assertThat(actual).isEqualTo(expected)
        }
        run {
            // Arrange
            val file = "build.gradle.kts"
            // Act
            val actual = ProjectHelper.getProjectDirectory(file = file)
            // Assert
            val expected = file.toPath().parent.toString()
            assertThat(actual).isEqualTo(expected)
        }
    }
}