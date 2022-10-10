package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.HookInsnPoint
import me.yuugiri.hutil.processor.hook.IHookInsnPoint
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * inject hook at every matched [FieldInsnNode]
 */
class HookPointField(val matcher: IHookPointMatcher) : IHookPoint {

    override fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<IHookInsnPoint> {
        val nodes = mutableListOf<IHookInsnPoint>()

        method.instructions.forEach {
            if (it is FieldInsnNode) {
                var id = "${it.owner};${it.name}"
                if (!matcher.matches(id)) {
                    val obf = AbstractObfuscationMap.fieldObfuscationRecord(obfuscationMap, it.owner, it.name)
                    id = "${obf.owner};${obf.name}"
                    if (!matcher.matches(id)) {
                        return@forEach
                    }
                }
                nodes.add(HookInsnPoint(it))
            }
        }

        return nodes
    }
}