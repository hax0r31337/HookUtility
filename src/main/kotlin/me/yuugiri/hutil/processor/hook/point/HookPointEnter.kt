package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.HookInsnPoint
import me.yuugiri.hutil.processor.hook.IHookInsnPoint
import me.yuugiri.hutil.util.forEach
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodNode

/**
 * inject hook at first [LabelNode] found in instructions
 */
class HookPointEnter : IHookPoint {

    override fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<IHookInsnPoint> {
        // find first label
        method.instructions.forEach {
            if (it is LabelNode) {
                return listOf(HookInsnPoint(it))
            }
        }
        return emptyList()
    }
}