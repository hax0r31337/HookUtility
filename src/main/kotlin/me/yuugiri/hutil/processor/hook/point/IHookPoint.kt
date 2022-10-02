package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.IHookInsnPoint
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

interface IHookPoint {

    /**
     * @return hook information
     */
    fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<IHookInsnPoint>
}