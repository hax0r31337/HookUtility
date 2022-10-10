package me.yuugiri.hutil.obfuscation

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

abstract class AbstractObfuscationMap {

    /**
     * @return null when no record found
     */
    abstract fun mapClass(name: String): ClassObfuscationRecord?

    /**
     * @return null when no record found
     */
    abstract fun mapField(owner: String, name: String): FieldObfuscationRecord?

    /**
     * @return null when no record found
     */
    abstract fun mapMethod(owner: String, name: String, desc: String): MethodObfuscationRecord?

    abstract class RecognizableIdentifier {
        abstract val identifier: String

        override fun toString(): String {
            return "${javaClass.simpleName}[${identifier}]"
        }
    }

    data class ClassObfuscationRecord(val obfuscatedName: String, val name: String) : RecognizableIdentifier() {
        override val identifier = obfuscatedName
    }

    data class FieldObfuscationRecord(val obfuscatedOwner: String, val owner: String,
                                      val obfuscatedName: String, val name: String) : RecognizableIdentifier() {
        override val identifier = "$obfuscatedOwner/$obfuscatedName"
    }

    data class MethodObfuscationRecord(val obfuscatedOwner: String, val owner: String, val obfuscatedName: String, val name: String,
                                      val obfuscatedDesc: String, val description: String) : RecognizableIdentifier() {
        override val identifier = "$obfuscatedOwner/$obfuscatedName$obfuscatedDesc"
    }

    companion object {
        fun classObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode) =
            classObfuscationRecord(obfuscationMap, klass.name)

        fun classObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, name: String) =
            obfuscationMap?.mapClass(name) ?: ClassObfuscationRecord(name, name)

        fun fieldObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, owner: String, field: FieldNode) =
            fieldObfuscationRecord(obfuscationMap, owner, field.name)

        fun fieldObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, owner: String, name: String) =
            obfuscationMap?.mapField(owner, name) ?: FieldObfuscationRecord(owner, owner, name, name)

        fun methodObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, owner: String, method: MethodNode) =
            methodObfuscationRecord(obfuscationMap, owner, method.name, method.desc)

        fun methodObfuscationRecord(obfuscationMap: AbstractObfuscationMap?, owner: String, name: String, desc: String) =
            obfuscationMap?.mapMethod(owner, name, desc) ?: MethodObfuscationRecord(owner, owner, name, name, desc, desc)
    }
}