package me.yuugiri.hutil.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode

fun getInsnInt(num: Int) = when (num) {
    -1 -> InsnNode(Opcodes.ICONST_M1)
    0 -> InsnNode(Opcodes.ICONST_0)
    1 -> InsnNode(Opcodes.ICONST_1)
    2 -> InsnNode(Opcodes.ICONST_2)
    3 -> InsnNode(Opcodes.ICONST_3)
    4 -> InsnNode(Opcodes.ICONST_4)
    5 -> InsnNode(Opcodes.ICONST_5)
    else -> {
        if (num in Byte.MIN_VALUE..Byte.MAX_VALUE) {
            IntInsnNode(Opcodes.BIPUSH, num)
        } else if (num in Short.MIN_VALUE..Short.MAX_VALUE) {
            IntInsnNode(Opcodes.SIPUSH, num)
        } else {
            LdcInsnNode(num)
        }
    }
}

fun isReturnNode(insn: AbstractInsnNode): Boolean {
    return if (insn is InsnNode) {
        val opcode = insn.opcode
        opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
                opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN
    } else {
        false
    }
}