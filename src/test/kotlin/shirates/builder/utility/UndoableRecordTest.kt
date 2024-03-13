package shirates.builder.utility

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shirates.builder.SelectorItem
import shirates.builder.utility.undo.ListUndoable
import shirates.builder.utility.undo.PropertyUndoable
import shirates.builder.utility.undo.UndoTargets
import shirates.builder.utility.undo.UndoableRecord
import shirates.core.testcode.UnitTest

class UndoableRecordTest : UnitTest() {

    @Test
    fun PropertyUndoable() {

        // Arrange
        val item1 = SelectorItem(nickname = "nickname1", selectorExpression = "expressiion1")
        // Act
        val undoableRecord = UndoableRecord(undoTargets = UndoTargets(item1))
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoAction).isNull()
        assertThat(undoableRecord.redoAction).isNull()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)
            val u0 = undoableObjects[0] as PropertyUndoable
            assertThat(u0.obj).isEqualTo(item1)
            assertThat(u0.undoRedoDataMap.count()).isEqualTo(0)
        }

        // Act
        undoableRecord.captureForUndo()
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)

            val u0 = undoableObjects[0] as PropertyUndoable
            assertThat(u0.obj).isEqualTo(item1)
            assertThat(u0.undoRedoDataMap.count()).isEqualTo(2)

            val undoRedo0 = u0.undoRedoDataMap.values.toList()[0]
            assertThat(undoRedo0.property.name).isEqualTo("nickname")
            assertThat(undoRedo0.undoValue).isEqualTo("nickname1")
            assertThat(undoRedo0.redoValue).isEqualTo(null)

            val undoRedo1 = u0.undoRedoDataMap.values.toList()[1]
            assertThat(undoRedo1.property.name).isEqualTo("selectorExpression")
            assertThat(undoRedo1.undoValue).isEqualTo("expressiion1")
            assertThat(undoRedo1.redoValue).isEqualTo(null)
        }

        // Arrange
        item1.nickname = "NICKNAME1"
        item1.selectorExpression = "EXPRESSION1"
        // Act
        undoableRecord.captureForRedo()
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)

            val u0 = undoableObjects[0] as PropertyUndoable
            assertThat(u0.obj).isEqualTo(item1)
            assertThat(u0.undoRedoDataMap.count()).isEqualTo(2)

            val undoRedo0 = u0.undoRedoDataMap.values.toList()[0]
            assertThat(undoRedo0.property.name).isEqualTo("nickname")
            assertThat(undoRedo0.undoValue).isEqualTo("nickname1")
            assertThat(undoRedo0.redoValue).isEqualTo("NICKNAME1")

            val undoRedo1 = u0.undoRedoDataMap.values.toList()[1]
            assertThat(undoRedo1.property.name).isEqualTo("selectorExpression")
            assertThat(undoRedo1.undoValue).isEqualTo("expressiion1")
            assertThat(undoRedo1.redoValue).isEqualTo("EXPRESSION1")
        }
    }

    @Test
    fun listUndoable() {

        // Arrange
        val item1 = SelectorItem(nickname = "nickname1", selectorExpression = "expression1")
        val item2 = SelectorItem(nickname = "nickname2", selectorExpression = "expression2")
        var list1 = mutableListOf(item1, item2)
        // Act
        val undoableRecord = UndoableRecord(undoTargets = UndoTargets(list1))
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoAction).isNull()
        assertThat(undoableRecord.redoAction).isNull()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)
            val u0 = undoableObjects[0] as ListUndoable<*>
            assertThat(u0.undoState.count()).isEqualTo(0)
            assertThat(u0.redoState.count()).isEqualTo(0)
        }

        // Act
        undoableRecord.captureForUndo()
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)

            val u0 = undoableObjects[0] as ListUndoable<*>
            assertThat(u0.undoState).containsExactly(item1, item2)
            assertThat(u0.redoState.count()).isEqualTo(0)
        }

        // Arrange
        list1.sortByDescending { it.nickname }
        // Act
        undoableRecord.captureForRedo()
        // Assert
        assertThat(undoableRecord.objects.count()).isEqualTo(1)
        assertThat(undoableRecord.description).isEmpty()
        assertThat(undoableRecord.undoableObjectMap.count()).isEqualTo(1)
        run {
            val undoableObjects = undoableRecord.undoableObjectMap.values.toList()
            assertThat(undoableObjects.count()).isEqualTo(1)

            val u0 = undoableObjects[0] as ListUndoable<*>
            assertThat(u0.undoState).containsExactly(item1, item2)
            assertThat(u0.redoState).containsExactly(item2, item1)
        }
    }
}