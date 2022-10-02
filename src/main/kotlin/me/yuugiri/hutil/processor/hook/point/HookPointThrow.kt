package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.EnumPointType
import me.yuugiri.hutil.processor.hook.HookInsnPoint
import me.yuugiri.hutil.processor.hook.IHookInsnPoint
import me.yuugiri.hutil.util.isThrowNode
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * inject hook at last [AbstractInsnNode] for throw, do not use [EnumHookShift.AFTER] unless you want a hook that never been triggered
 */
class HookPointThrow : IHookPoint {

    override fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<IHookInsnPoint> {
        val nodes = mutableListOf<IHookInsnPoint>()
        method.instructions.forEach {
            if (isThrowNode(it)) {
                nodes.add(HookInsnPoint(it, EnumPointType.THROWN))
            }
        }
        return nodes
    }
}