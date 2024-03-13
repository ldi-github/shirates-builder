package shirates.builder.utility.undo

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.hasAnnotation

class PropertyUndoable(
) : IUndoable {
    class UndoRedoData(
        val property: KMutableProperty<*>
    ) {
        var undoValue: Any? = null
        var redoValue: Any? = null
    }

    lateinit var obj: Any

    internal val undoRedoDataMap = mutableMapOf<KMutableProperty<*>, UndoRedoData>()

    constructor(obj: Any) : this() {
        this.obj = obj
    }

    override fun captureForUndo() {

        captureData(undo = true)
    }

    override fun captureForRedo() {

        captureData(undo = false)
    }

    private fun captureData(undo: Boolean = true) {
        val properties = obj::class.members.filter { it is KMutableProperty<*> && it.hasAnnotation<Undoable>() }
            .map { it as KMutableProperty<*> }
        for (p in properties) {
            val value = p.getter.call(obj)
            if (undoRedoDataMap.containsKey(p).not()) {
                val r = UndoRedoData(p)
                undoRedoDataMap[p] = r
            }
            val undoRedoRecord = undoRedoDataMap[p]!!
            if (value is MutableList<*>) {
                if (undo) {
                    undoRedoRecord.undoValue = value.toMutableList()
                } else {
                    undoRedoRecord.redoValue = value.toMutableList()
                }
            } else {
                if (undo) {
                    undoRedoRecord.undoValue = value
                } else {
                    undoRedoRecord.redoValue = value
                }
            }
        }
    }

    override fun undo() {

        for (propertyUndoRedo in undoRedoDataMap.values) {
            restoreValue(property = propertyUndoRedo.property, value = propertyUndoRedo.undoValue)
        }
    }

    override fun redo() {

        for (propertyUndoRedo in undoRedoDataMap.values) {
            restoreValue(property = propertyUndoRedo.property, value = propertyUndoRedo.redoValue)
        }
    }

    private fun restoreValue(property: KMutableProperty<*>, value: Any?) {
        if (value is MutableList<*>) {
            val original = value as MutableList<*>
            val list = property.getter.call(obj) as MutableList<*>
            list.clear()
            list.addAll(original as Collection<Nothing>)
        } else {
            property.setter.call(obj, value)
        }
    }
}
