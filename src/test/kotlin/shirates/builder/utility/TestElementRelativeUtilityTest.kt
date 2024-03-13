package shirates.builder.utility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shirates.builder.SelectorItem
import shirates.builder.testdata.XmlDataAndroid
import shirates.core.driver.TestElementCache
import shirates.core.driver.TestMode
import shirates.core.testcode.UnitTest

class TestElementRelativeUtilityTest : UnitTest() {

    @Test
    fun getRelativeExpression() {

        fun selectItem(exp: String): SelectorItem {
            return SelectorItem(testElement = TestElementCache.select(exp))
        }

        TestMode.runAsAndroid {
            // Arrange
            TestElementCache.loadXml(XmlDataAndroid.SettingsTopScreen)
            run {
                // Act
                val exp = TestElementRelativeUtility.getRelativeExpression(
                    baseItem = selectItem("Apps"),
                    thisItem = selectItem("Assistant, recent apps, default apps")
                )
                // Assert
                assertThat(exp).isEqualTo(":belowLabel")
            }
            run {
                // Act
                val exp = TestElementRelativeUtility.getRelativeExpression(
                    baseItem = selectItem("Assistant, recent apps, default apps"),
                    thisItem = selectItem("Apps")
                )
                // Assert
                assertThat(exp).isEqualTo(":aboveLabel")
            }
            run {
                // Act
                val exp = TestElementRelativeUtility.getRelativeExpression(
                    baseItem = selectItem("Network & internet"),
                    thisItem = selectItem("Assistant, recent apps, default apps")
                )
                // Assert
                assertThat(exp).isEqualTo(":belowLabel(5)")
            }
            run {
                // Act
                val exp = TestElementRelativeUtility.getRelativeExpression(
                    baseItem = selectItem("Assistant, recent apps, default apps"),
                    thisItem = selectItem("Network & internet")
                )
                // Assert
                assertThat(exp).isEqualTo(":aboveLabel(5)")
            }
        }

    }
}