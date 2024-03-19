package shirates.builder.utility

import shirates.builder.SelectorItem
import shirates.core.driver.RelativeDirection
import shirates.core.driver.TestElement
import shirates.core.driver.TestElementCache
import shirates.core.driver.commandextension.helper.HorizontalBand
import shirates.core.driver.commandextension.helper.VerticalBand
import shirates.core.logging.Message.message
import shirates.core.logging.TestLog
import shirates.core.utility.element.ElementCategory
import shirates.core.utility.element.ElementCategoryExpressionUtility
import shirates.spec.utilily.removeBrackets

object TestElementRelativeUtility {

    /**
     * getRelativeDirection
     */
    fun getRelativeDirection(
        baseElement: TestElement,
        thisElement: TestElement
    ): RelativeDirection {

        val hb = HorizontalBand(baseElement)
        hb.merge(thisElement)
        if (hb.getElements().count() == 2) {
            if (baseElement.bounds.left < thisElement.bounds.left) {
                return RelativeDirection.right
            }
            return RelativeDirection.left
        }

        val vb = VerticalBand(baseElement)
        vb.merge(thisElement)
        if (vb.getElements().count() == 2) {
            if (baseElement.bounds.top < thisElement.bounds.top) {
                return RelativeDirection.below
            }
            return RelativeDirection.above
        }

        return RelativeDirection.right
    }

    /**
     * getRelativeExpression
     */
    fun getRelativeExpression(
        baseItem: SelectorItem,
        thisItem: SelectorItem
    ): String {

        val thisCategory = ElementCategoryExpressionUtility.getCategory(thisItem.testElement).toString().lowercase()

        val direction = getRelativeDirection(baseItem.testElement, thisItem.testElement)

        val relativeExpression = ":$direction${thisCategory[0].uppercase()}${thisCategory.substring(1)}"
        val baseExpression = baseItem.selectorExpression
        val r = trySelectRelative(baseExpression, relativeExpression, thisItem = thisItem)
        if (r.isNotBlank()) {
            return r
        }
        val r2 = trySelectRelative(baseExpression, ":flow", thisItem = thisItem)
        if (r2.isNotBlank()) {
            return r2
        }
        return ""
    }

    private fun trySelectRelative(
        baseExpression: String,
        relativeExpression: String,
        thisItem: SelectorItem,
        maxCount: Int = 5
    ): String {

        for (i in 1..maxCount) {
            val expression = "$baseExpression$relativeExpression($i)"
            val e = TestElementCache.select(expression = expression, throwsException = false)
            if (e.isEmpty) {
                return ""
            }
            if (e.toString() == thisItem.testElement.toString()) {
                if (i == 1) {
                    return relativeExpression
                }
                return "$relativeExpression($i)"
            }
        }
        return ""
    }

    /**
     * getNicknameCandidate
     */
    fun getNicknameCandidate(
        baseItem: SelectorItem,
        thisItem: SelectorItem
    ): String {

        val c = ElementCategoryExpressionUtility.getCategory(thisItem.testElement)
        var suffix = ""
        if (c == ElementCategory.INPUT ||
            c == ElementCategory.IMAGE ||
            c == ElementCategory.BUTTON ||
            c == ElementCategory.SWITCH
        ) {
            val id = ":${c.toString().lowercase()}"
            suffix = message(id = id).removeBrackets().removePrefix(":")
        }
        var nicknameTitle = baseItem.nickname.removeBrackets()
        if (c == ElementCategory.INPUT) {
            return "{$nicknameTitle}"
        }
        if (suffix.isNotBlank()) {
            if (TestLog.logLanguage == "ja") {
                nicknameTitle = "${nicknameTitle}${suffix}"
            } else {
                nicknameTitle = "${nicknameTitle} ${suffix}"
            }
            return "[$nicknameTitle]"
        } else {
            return "{$nicknameTitle}"
        }
    }
}