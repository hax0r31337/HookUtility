package me.yuugiri.hutil.processor.hook

abstract class AbstractHookTarget {

    /**
     * is class able to be target
     */
    abstract fun classMatches(className: String): Boolean

    /**
     * checks [classMatches] before this
     */
    abstract fun methodMatches(methodName: String, methodDesc: String): Boolean

//    open fun methodMatches(method: MethodNode): Boolean {
//        return methodMatches(method.name, method.desc)
//    }
//
//    open fun matches(className: String, method: MethodNode): Boolean {
//        return classMatches(className) && methodMatches(method)
//    }

    open fun matches(className: String, methodName: String, methodDesc: String): Boolean {
        return classMatches(className) && methodMatches(methodName, methodDesc)
    }
}

class HookTargetImpl(val className: String, val methodName: String,
                     /**
                      * "*" for wildcard
                      */
                     val methodDesc: String = "*") : AbstractHookTarget() {

    override fun classMatches(classNameIn: String) = classNameIn == className

    override fun methodMatches(methodNameIn: String, methodDescIn: String) =
            methodNameIn == methodName && (methodDesc == methodDescIn || methodDesc == "*")
}