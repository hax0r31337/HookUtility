package me.yuugiri.hutil

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap.Companion.classObfuscationRecord
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

class HookUtility {

    var obfuscationMap: AbstractObfuscationMap? = null
    val processorList = mutableListOf<IClassProcessor>()

    /**
     * process and inject hooks into [klass]
     * @return has changes or not
     */
    fun dealWithClassNode(klass: ClassNode): Boolean {
        if (processorList.isEmpty()) return false

        val classObf = classObfuscationRecord(obfuscationMap, klass)
        var hasProcessed = false
        processorList.forEach {
            if (!it.selectClass(classObf.name) && (klass.name != classObf.name && !it.selectClass(klass.name))) return@forEach
            hasProcessed = it.processClass(obfuscationMap, classObf, klass) || hasProcessed
        }
        return hasProcessed
    }

    /**
     * process and inject hooks into [bytes]
     * @return modified class file
     */
    private fun dealWithClassDataDirect(bytes: ByteArray): ByteArray {
        if (processorList.isEmpty()) return bytes // don't waste time to do nothing

        // read into ClassNode
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)

        if (!dealWithClassNode(classNode)) {
            return bytes // save performance when no changes has apply to the class
        }

        // write back to bytecode form
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        classNode.accept(classWriter)
        return classWriter.toByteArray()
    }

    /**
     * process and inject hooks into [bytes]
     * @param name **RECOMMENDED** class name
     * @return modified class file
     */
    fun dealWithClassData(bytes: ByteArray, name: String = ""): ByteArray {
        if (name.isEmpty()) return dealWithClassDataDirect(bytes)

        // check name before process, this will save performance
        val classObf = classObfuscationRecord(obfuscationMap, name)
        if (!processorList.any { it.selectClass(name) || it.selectClass(classObf.name) }) {
            return bytes
        }

        return dealWithClassDataDirect(bytes)
    }
}