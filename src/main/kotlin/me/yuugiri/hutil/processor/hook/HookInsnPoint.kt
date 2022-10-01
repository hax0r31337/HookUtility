package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.util.getInsnInt
import me.yuugiri.hutil.util.getPrimitiveValueOf
import me.yuugiri.hutil.util.getTypeLoadOpcode
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class HookInsnPoint(
    /**
     * AbstractInsnNodes(hook points) that matches the hook rule
     */
    val node: AbstractInsnNode,
    val type: EnumPointType = EnumPointType.COMMON) {

    fun injectHook(method: MethodNode, callbackId: Int, shift: EnumHookShift) {
        val insnList = method.instructions

        if (!insnList.contains(node)) {
            throw IllegalArgumentException("passed method node instructions not contains target node")
        }

        var lastCache = node
        fun push(nodePush: AbstractInsnNode) {
            when (shift) {
                EnumHookShift.BEFORE -> insnList.insertBefore(node, nodePush)
                EnumHookShift.AFTER -> {
                    insnList.insert(lastCache, nodePush)
                    lastCache = nodePush
                }
            }
        }

        // throwable/return already on stack

        // push thisObject to stack
        var loadIndex = 0
        if (method.access and Opcodes.ACC_STATIC != 0) {
            push(InsnNode(Opcodes.ACONST_NULL)) // pass null in static methods
        } else {
            push(VarInsnNode(Opcodes.ALOAD, 0))
            loadIndex++
        }

        // parse method description to get arguments and return type
        val methodType = Type.getMethodType(method.desc)

        // create array for arguments
        push(getInsnInt(methodType.argumentTypes.size))
        push(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"))

        // store arguments into the array
        methodType.argumentTypes.forEachIndexed { index, type ->
            push(InsnNode(Opcodes.DUP)) // duplicate the array on stack to make the array won't disappear after AASTORE
            push(getInsnInt(index))
            when(type.sort) {
                Type.METHOD, Type.ARRAY -> push(VarInsnNode(getTypeLoadOpcode(type.descriptor), loadIndex))
                else -> {
                    push(VarInsnNode(getTypeLoadOpcode(type.descriptor), loadIndex))
                    push(getPrimitiveValueOf(type))
                }
            }
            loadIndex += type.size
            push(InsnNode(Opcodes.AASTORE))
        }

        // construct MethodHookParam
        push(when(type) {
            EnumPointType.COMMON -> MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam\$Companion", "raw", "(Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
            EnumPointType.RETURN -> MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam\$Companion", "withReturn", "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
            EnumPointType.THROWN -> MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam\$Companion", "withThrowable", "(Ljava/lang/Throwable;Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
        })

        // invoke hook
        push(InsnNode(Opcodes.DUP))
        push(getInsnInt(callbackId))
        push(MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookProcessor\$Companion", "hookCallback", "(Lme/yuugiri/hutil/processor/hook/MethodHookParam;I)V"))

        // TODO: process result
        push(InsnNode(Opcodes.POP))
    }

    enum class EnumPointType {
        COMMON,
        /**
         * pass the return object
         */
        RETURN,
        /**
         * pass the throwable object
         */
        THROWN
    }
}