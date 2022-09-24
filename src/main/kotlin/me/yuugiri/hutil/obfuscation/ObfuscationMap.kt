package me.yuugiri.hutil.obfuscation

import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

abstract class ObfuscationMap {

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

    interface RecognizableIdentifier {
        val identifier: String
    }

    data class ClassObfuscationRecord(val obfuscatedName: String, val name: String) : RecognizableIdentifier {
        override val identifier = obfuscatedName
    }

    data class FieldObfuscationRecord(val obfuscatedOwner: String, val owner: String,
                                      val obfuscatedName: String, val name: String) : RecognizableIdentifier {
        override val identifier = "$obfuscatedOwner/$obfuscatedName"
    }

    data class MethodObfuscationRecord(val obfuscatedOwner: String, val owner: String, val obfuscatedName: String, val name: String,
                                      val obfuscatedDesc: String, val description: String) : RecognizableIdentifier {
        override val identifier = "$obfuscatedOwner/$obfuscatedName$obfuscatedDesc"
    }

    companion object {
        fun classObfuscationRecord(obfuscationMap: ObfuscationMap?, klass: ClassNode) =
            obfuscationMap?.mapClass(klass.name) ?: ClassObfuscationRecord(klass.name, klass.name)

        fun fieldObfuscationRecord(obfuscationMap: ObfuscationMap?, owner: String, field: FieldNode) =
            obfuscationMap?.mapField(owner, field.name) ?: FieldObfuscationRecord(owner, owner, field.name, field.name)

        fun methodObfuscationRecord(obfuscationMap: ObfuscationMap?, owner: String, method: MethodNode) =
            obfuscationMap?.mapMethod(owner, method.name, method.desc) ?: MethodObfuscationRecord(owner, owner, method.name, method.name, method.desc, method.desc)
    }
}