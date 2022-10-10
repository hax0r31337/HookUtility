package me.yuugiri.hutil.util

class ModifyRecordArray<T>(private val array: Array<T>) {

    /**
     * is the array got modified
     */
    var hasModified = false
        private set

    val size: Int
        get() = array.size

    /**
     * set [hasModified] to false
     */
    fun clearModifyRecord() {
        hasModified = false
    }

    operator fun get(idx: Int) = array[idx]

    operator fun set(idx: Int, item: T) {
        array[idx] = item
        hasModified = true
    }

    @JvmName("getArray")
    internal fun getArray(): Array<T> {
        return array
    }
}