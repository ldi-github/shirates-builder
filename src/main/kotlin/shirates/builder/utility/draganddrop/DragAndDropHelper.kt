package shirates.builder.utility.draganddrop

import java.util.*

object DragAndDropHelper {

    val stack = Stack<Any>()

    val isEmpty: Boolean
        get() {
            return stack.isEmpty()
        }

    val hasObject: Boolean
        get() {
            return stack.any()
        }

    fun pushObject(obj: Any) {
        stack.push(obj)
    }

    fun <T> popObject(): T {
        return stack.pop() as T
    }
}