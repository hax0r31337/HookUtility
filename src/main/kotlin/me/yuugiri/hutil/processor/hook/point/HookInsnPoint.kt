package me.yuugiri.hutil.processor.hook.point

import org.objectweb.asm.tree.AbstractInsnNode

class HookInsnPoint(
    /**
     * AbstractInsnNodes(hook points) that matches the hook rule
     */
    val node: AbstractInsnNode,
    /**
     * pass the return object
     */
    val isReturn: Boolean = false,
    /**
     * pass the throwable object
     */
    val isThrown: Boolean = false) {
}