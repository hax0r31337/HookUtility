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
    throwableIn: Throwable? = null) {

    val args = ModifyRecordArray(argsIn)

    var resultModified = false
        private set
    var result = resultIn
        set(value) {
            resultModified = true
            field = value
        }

    private val throwableCanModify = throwableIn != null
    var throwable = throwableIn
        set(value) {
            if (!throwableCanModify) throw IllegalStateException("this not a hook for throwable, consider throw the throwable in your callback code")
            field = value
        }

    companion object {

        @JvmStatic
        fun raw(thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args)
        }

        @JvmStatic
        fun withReturn(result: Any?, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, resultIn = result)
        }

        @JvmStatic
        fun withThrowable(throwable: Throwable, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, throwableIn = throwable)
        }
    }
}