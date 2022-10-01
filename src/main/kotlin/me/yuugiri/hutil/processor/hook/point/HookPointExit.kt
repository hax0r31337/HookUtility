package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.util.isReturnNode
import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.HookInsnPoint
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

/**
 * inject hook at every [AbstractInsnNode] for return, do not use [EnumHookShift.AFTER] unless you want a hook that never been triggered
 */
class HookPointExit : IHookPoint {

    override fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<HookInsnPoint> {
        // find return nodes
        val nodes = mutableListOf<HookInsnPoint>()
        method.instructions.forEach {
            if (isReturnNode(it)) {
                nodes.add(HookInsnPoint(it, HookInsnPoint.EnumPointType.RETURN))
            }
        }
        return nodes
    }
}