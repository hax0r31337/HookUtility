package me.yuugiri.hutil.processor

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.ClassNode
import java.io.BufferedReader

/**
 * change accesses like AccessMap in SpecialSource
 */
class AccessProcessor : IClassProcessor {

    val records = mutableListOf<AccessRecord>()

    override fun selectClass(name: String) = records.any { it.owner == name }

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var name = map.name
        var selectedRecords = records.filter { it.owner == name }
        if (selectedRecords.isEmpty()) {
            name = map.obfuscatedName
            selectedRecords = records.filter { it.owner == name }
            if (selectedRecords.isEmpty()) return false
        }

        selectedRecords.filter { it.target.isEmpty() }.forEach { ar ->
            klass.access = ar.apply(klass.access)
        }

        klass.fields.forEach { field ->
            val obf = AbstractObfuscationMap.fieldObfuscationRecord(obfuscationMap, klass.name, field)
            selectedRecords.forEach { ar ->
                if (ar.target == obf.name) {
                    field.access = ar.apply(field.access)
                }
            }
            if (obf.name != field.name) {
                selectedRecords.forEach { ar ->
                    if (ar.target == field.name) {
                        field.access = ar.apply(field.access)
                    }
                }
            }
        }
        klass.methods.forEach { method ->
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var id = obf.name + obf.description
            selectedRecords.forEach { ar ->
                if (ar.target == id) {
                    method.access = ar.apply(method.access)
                }
            }
            if (obf.name != method.name) {
                id = method.name + method.desc
                selectedRecords.forEach { ar ->
                    if (ar.target == id) {
                        method.access = ar.apply(method.access)
                    }
                }
            }
        }

        return true
    }

    class AccessRecord {
        var vis: Int
        var clear: Int
        var set: Int
        var owner: String
        var target: String

        constructor(vis: Int, clear: Int, set: Int, owner: String, target: String) {
            this.vis = vis
            this.clear = clear
            this.set = set
            this.owner = owner
            this.target = target
        }

        /**
         * load access from SpecialSource format
         */
        constructor(rule: String, owner: String, target: String) {
            this.owner = owner
            this.target = target

            val parts = rule.split(splitRule)

            // symbol visibility
            val visibilityString = parts[0]
            vis = accessCodes[visibilityString] ?: throw IllegalArgumentException("Invalid access visibility: $visibilityString")
            set = 0
            clear = 0

            if (parts.size > 1) {
                // modifiers
                for (i in 1 until parts.size) {
                    require(parts[i].length >= 2) { "Invalid modifier length ${parts[i]} in access string: $rule" }
                    val actionChar = parts[i][0]
                    val modifierString = parts[i].substring(1)
                    val modifier = accessCodes[modifierString] ?: throw IllegalArgumentException("Invalid modifier string $modifierString in access string: $rule")
                    when (actionChar) {
                        '+' -> set = set or modifier
                        '-' -> clear = clear or modifier
                        else -> throw java.lang.IllegalArgumentException("Invalid action $actionChar in access string: $rule")
                    }
                }
            }
        }

        /**
         * transform access
         * @param access original access flags
         * @return transformed access flags
         */
        fun apply(access: Int): Int {
            return (setVisibility(access, upgradeVisibility(access and MASK_ALL_VISIBILITY, vis)) and clear.inv()) or set
        }
    }

    companion object {

        private val splitRule = Regex("(?=[+-])")
        private const val MASK_ALL_VISIBILITY = ACC_PUBLIC or ACC_PRIVATE or ACC_PROTECTED
        private val accessCodes = mapOf("public" to ACC_PUBLIC,
            "private" to ACC_PRIVATE, "protected" to ACC_PROTECTED,
            "default" to 0, "" to 0, "package-private" to 0,
            "static" to ACC_STATIC, "final" to ACC_FINAL, "f" to ACC_FINAL,
            "super" to ACC_SUPER, "synchronized" to ACC_SYNCHRONIZED, "volatile" to ACC_VOLATILE,
            "bridge" to ACC_BRIDGE, "varargs" to ACC_VARARGS, "transient" to ACC_TRANSIENT,
            "interface" to ACC_INTERFACE, "native" to ACC_NATIVE, "abstract" to ACC_ABSTRACT,
            "strict" to ACC_STRICT, "synthetic" to ACC_SYNTHETIC, "annotation" to ACC_ANNOTATION,
            "enum" to ACC_ENUM, "deprecated" to ACC_DEPRECATED)
        private val visibilityOrder = mapOf(ACC_PRIVATE to 100, 0 to 200, ACC_PROTECTED to 300, ACC_PUBLIC to 400)

        /**
         * load [AccessProcessor] from FMLAT format
         */
        fun fromFMLAccessTransformer(at: BufferedReader, srgs: Map<String, String> = emptyMap()): AccessProcessor {
            val processor = AccessProcessor()

            at.readLines().forEach { rawLine ->
                val line = (if (rawLine.contains("#")) rawLine.substring(0, rawLine.indexOf("#")) else rawLine ).trim().split(" ")
                processor.records.add(if (line.size == 3) {
                    val name = line[2].let {
                        if (it.contains("(")) {
                            val desc = it.substring(it.indexOf("("))
                            val name = it.substring(0, it.indexOf("(")).let { srgs[it] ?: it }
                            name + desc
                        } else {
                            srgs[it] ?: it
                        }
                    }
                    AccessRecord(line[0], line[1].replace('.', '/'), name)
                } else if (line.size == 2) {
                    AccessRecord(line[0], line[1].replace('.', '/'), "")
                } else return@forEach)
            }

            return processor
        }

        /**
         * Get modified visibility access, never decreased (either same or higher)
         *
         * @param existing The current visibility access
         * @param desired The new desired target visibility access
         * @return The greater visibility of the two arguments
         */
        private fun upgradeVisibility(existing: Int, desired: Int): Int {
            val existingOrder = visibilityOrder[existing] ?: throw IllegalArgumentException("Unrecognized visibility: $existing")
            val desiredOrder = visibilityOrder[desired]  ?: throw IllegalArgumentException("Unrecognized visibility: $desired")
            val newOrder = existingOrder.coerceAtLeast(desiredOrder)
            return visibilityOrder.entries.associate { (k, v)-> v to k }.get(newOrder)!!
        }

        /**
         * Set visibility on access flags, overwriting existing, preserving other
         * flags
         *
         * @param access
         * @param visibility
         */
        private fun setVisibility(access: Int, visibility: Int): Int {
            return (access and MASK_ALL_VISIBILITY.inv()) or visibility
        }
    }
}