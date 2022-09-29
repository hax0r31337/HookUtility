package me.yuugiri.hutil.processor.hook

class MethodHookParam(
    /**
     * The this reference for an instance method, or null for static methods
     */
    val thisObject: Any?,
    /**
     * Arguments to the method call
     */
    var args: Array<Any?>,
    /**
     * return value of the method call
     */
    var result: Any? = null,
    /**
     * [Throwable] thrown by the method, or null
     */
    var throwable: Throwable? = null) {

    companion object {
        fun withReturn(result: Any?, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, result = result)
        }

        fun withThrowable(throwable: Throwable, thisObject: Any?, args: Array<Any?>): MethodHookParam {
            return MethodHookParam(thisObject, args, throwable = throwable)
        }
    }
}