package me.yuugiri.hutil.processor

import me.yuugiri.hutil.obfuscation.ObfuscationMap
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import java.io.BufferedReader

class AccessProcessor : IClassProcessor {

    val records = mutableListOf<AccessRecord>()

    override fun selectClass(name: String) = records.any { it.owner == name }

    override fun processClass(obfuscationMap: ObfuscationMap?, klass: ClassNode): Boolean {
        val selectedRecords = records.filter { it.owner == klass.name }
        if (selectedRecords.isEmpty()) return false

        klass.fields.forEach { field ->
            val obf = ObfuscationMap.fieldObfuscationRecord(obfuscationMap, klass.name, field)
            records.forEach { ar ->
                if (ar.target == obf.name) {
                    field.access = processAccess(field.access, ar.rules)
                }
            }
            if (obf.name != field.name) {
                records.forEach { ar ->
                    if (ar.target == field.name) {
                        field.access = processAccess(field.access, ar.rules)
                    }
                }
            }
        }
        klass.methods.forEach { method ->
            val obf = ObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var id = obf.name + obf.description
            records.forEach { ar ->
                if (ar.target == id) {
                    method.access = processAccess(method.access, ar.rules)
                }
            }
            if (obf.name != method.name) {
                id = method.name + method.desc
                records.forEach { ar ->
                    if (ar.target == id) {
                        method.access = processAccess(method.access, ar.rules)
                    }
                }
            }
        }

        return true
    }

    /**
     * transform method/field access
     * @param access original access flags
     * @return transformed access flags
     */
    private fun processAccess(access: Int, rules: List<AccessRule>): Int {
        val t = when {
            rules.contains(AccessRule.PUBLIC) -> ACC_PUBLIC
            rules.contains(AccessRule.PRIVATE) -> ACC_PRIVATE
            rules.contains(AccessRule.PROTECTED) -> ACC_PROTECTED
            else -> 0
        }

        val ret = (access and 7.inv()).let {
            when (access and 7) {
                ACC_PRIVATE -> it or t
                ACC_PROTECTED -> it or (if (t != ACC_PRIVATE && t != 0) t else ACC_PROTECTED)
                ACC_PUBLIC -> it or ACC_PUBLIC
                else -> it or (if (t != ACC_PRIVATE) t else 0)
            }
        }

        return when {
            rules.contains(AccessRule.REMOVE_FINAL) -> ret and ACC_FINAL.inv()
            rules.contains(AccessRule.ADD_FINAL) -> ret or ACC_FINAL
            else -> ret
        }
    }

    enum class AccessRule {
        PUBLIC,
        PRIVATE,
        PROTECTED,
        REMOVE_FINAL,
        ADD_FINAL
    }

    class AccessRecord(val rules: List<AccessRule>, var owner: String, var target: String)

    companion object {

        /**
         * load [AccessProcessor] from FMLAT format
         */
        fun fromFMLAccessTransformer(at: BufferedReader, srgs: Map<String, String> = emptyMap()): AccessProcessor {
            val processor = AccessProcessor()

            at.readLines().forEach {
                val line = it.substring(0, it.indexOf("#")).trim().split(" ")
                if (line.size != 3) return@forEach

                processor.records.add(AccessRecord(mutableListOf<AccessRule>().also {
                    val target = line[0]
                    if (target.startsWith("public")) {
                        it.add(AccessRule.PUBLIC)
                    } else if (target.startsWith("private")) {
                        it.add(AccessRule.PRIVATE)
                    } else if (target.startsWith("protected")) {
                        it.add(AccessRule.PROTECTED)
                    }
                    if (target.endsWith("-f")) {
                        it.add(AccessRule.REMOVE_FINAL)
                    } else if (target.endsWith("+f"))  {
                        it.add(AccessRule.ADD_FINAL)
                    }
                }, line[1].replace('.', '/'), line[2].let { srgs[it] ?: it }))
            }

            return processor
        }
    }
}