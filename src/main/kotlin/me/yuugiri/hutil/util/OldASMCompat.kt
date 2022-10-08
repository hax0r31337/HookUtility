package me.yuugiri.hutil.util

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode

val ClassNode.fields_: List<FieldNode>
    get() = this.fields.map { it as FieldNode }

val ClassNode.methods_: List<MethodNode>
    get() = this.methods.map { it as MethodNode }

// use inline to support return while looping
inline fun InsnList.forEach(action: (AbstractInsnNode) -> Unit) {
    for (i in 0 until this.size()) {
        action(this.get(i))
    }
}

inline fun InsnList.forEachReversed(action: (AbstractInsnNode) -> Unit) {
    for (i in this.size() - 1 downTo 0) {
        action(this.get(i))
    }
}