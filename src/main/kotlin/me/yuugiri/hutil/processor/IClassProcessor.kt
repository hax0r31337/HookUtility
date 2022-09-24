package me.yuugiri.hutil.processor

import me.yuugiri.hutil.obfuscation.ObfuscationMap
import org.objectweb.asm.tree.ClassNode

interface IClassProcessor {

    /**
     * this method will called before [processClass]
     * @return is the class need to be processed or not
     */
    fun selectClass(name: String): Boolean

    /**
     * apply changes to [klass]
     * @return has changes or not
     */
    fun processClass(obfuscationMap: ObfuscationMap?, klass: ClassNode): Boolean
}