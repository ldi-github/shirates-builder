package shirates.builder.utility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shirates.builder.SelectorItem
import shirates.builder.utility.undo.UndoManager
import shirates.builder.utility.undo.UndoTargets
import shirates.core.testcode.UnitTest

class UndoManagerTest : UnitTest() {

    @Test
    fun doAction() {

        // Arrange
        val item1 = SelectorItem(nickname = "nickname1", selectorExpression = "expression1")
        val list1 = mutableListOf("string1", "string2")
        // Act
        val manager = UndoManager()
        manager.doAction(
            undoAction = { data ->
                item1.nickname = "nickname1"
                item1.selectorExpression = "expression1"
                list1.clear()
                list1.add("string1")
                list1.add("string2")
            },
            undoTargets = UndoTargets(item1, list1)
        ) {
            item1.nickname = "NICKNAME1"
            item1.selectorExpression = "EXPRESSION1"
            list1.add("string3")
            list1.removeAt(0)
        }
        // Assert
        assertThat(item1.nickname).isEqualTo("NICKNAME1")
        assertThat(item1.selectorExpression).isEqualTo("EXPRESSION1")
        assertThat(list1).containsExactly("string2", "string3")
        // Act
        manager.undo()
        // Assert
        assertThat(item1.nickname).isEqualTo("nickname1")
        assertThat(item1.selectorExpression).isEqualTo("expression1")
        assertThat(list1).containsExactly("string1", "string2")

    }

    @Test
    fun propertyUndoable() {

        // Arrange
        val item1 = SelectorItem(nickname = "nickname1", selectorExpression = "expression1")
        val item2 = SelectorItem(nickname = "nickname2", selectorExpression = "expression2")
        // Act
        val manager = UndoManager()
        manager.doAction(
            description = "test1",
            undoTargets = UndoTargets(item1, item2)
        ) { data ->
            item1.nickname = "NICKNAME1"
            item1.selectorExpression = "EXPRESSION1"
            item2.nickname = "NICKNAME2"
            item2.selectorExpression = "EXPRESSION2"
        }
        // Assert
        assertThat(item1.nickname).isEqualTo("NICKNAME1")
        assertThat(item1.selectorExpression).isEqualTo("EXPRESSION1")
        assertThat(item2.nickname).isEqualTo("NICKNAME2")
        assertThat(item2.selectorExpression).isEqualTo("EXPRESSION2")
        // Act
        manager.undo()
        // Assert
        assertThat(item1.nickname).isEqualTo("nickname1")
        assertThat(item1.selectorExpression).isEqualTo("expression1")
        assertThat(item2.nickname).isEqualTo("nickname2")
        assertThat(item2.selectorExpression).isEqualTo("expression2")
    }

    @Test
    fun listundoable() {

        // Arrange
        val list1 = mutableListOf("string1", "string2")
        val list2 = mutableListOf(1, 2)
        // Act
        val manager = UndoManager()
        manager.doAction(
            undoTargets = UndoTargets(list1, list2)
        ) { data ->
            list1.sortByDescending { it }
            list2.sortByDescending { it }
        }
        // Assert
        assertThat(list1).containsExactly("string2", "string1")
        assertThat(list2).containsExactly(2, 1)
        // Act
        manager.undo()
        // Assert
        assertThat(list1).containsExactly("string1", "string2")
        assertThat(list2).containsExactly(1, 2)
    }

}