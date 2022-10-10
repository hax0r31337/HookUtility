package me.yuugiri.hutil.processor.hook.point

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.hook.HookInsnPoint
import me.yuugiri.hutil.processor.hook.IHookInsnPoint
import me.yuugiri.hutil.util.forEach
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * inject hook at every matched [MethodInsnNode]
 */
class HookPointInvoke(val matcher: IHookPointMatcher) : IHookPoint {

    override fun hookPoints(obfuscationMap: AbstractObfuscationMap?, klass: ClassNode, method: MethodNode): List<IHookInsnPoint> {
        val nodes = mutableListOf<IHookInsnPoint>()

        method.instructions.forEach {
            if (it is MethodInsnNode) {
                var id = "${it.owner};${it.name}${it.desc}"
                if (!matcher.matches(id)) {
                    val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, it.owner, it.name, it.desc)
                    id = "${obf.owner};${obf.name}${obf.description}"
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