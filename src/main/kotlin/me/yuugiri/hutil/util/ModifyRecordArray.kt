package me.yuugiri.hutil.util

class ModifyRecordArray<T>(private val arr: Array<T>) {

    /**
     * is the array got modified
     */
    var hasModified = false
        private set

    /**
     * set [hasModified] to false
     */
    fun clearModifyRecord() {
        hasModified = false
    }

    operator fun get(idx: Int) = arr[idx]

    operator fun set(idx: Int, item: T) {
        arr[idx] = item
        hasModified = true
    }
}