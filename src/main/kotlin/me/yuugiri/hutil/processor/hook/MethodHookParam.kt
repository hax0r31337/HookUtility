package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.util.ModifyRecordArray

class MethodHookParam(
    /**
     * The reference for an instance method, or null for static methods
     */
    val thisObject: Any?,
    /**
     * Arguments to the method call
     */
    argsIn: Array<Any?>,
    /**
     * return value of the method call
     */
    resultIn: Any? = null,
    /**
     * [Throwable] thrown by the method, or null
     */
    var throwable: Throwable? = null) {

    val args = ModifyRecordArray(argsIn)

    var resultModified = false
        private set
    var result = resultIn
        set(value) {
            resultModified = true
            field = value
        }



    companion object {

        fun raw(thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args)
        }

        fun withReturn(result: Any?, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, resultIn = result)
        }

        fun withThrowable(throwable: Throwable, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, throwable = throwable)
        }
    }
}