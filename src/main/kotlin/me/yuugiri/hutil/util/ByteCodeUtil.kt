package me.yuugiri.hutil.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

/**
 * get matched [AbstractInsnNode] for [num]
 */
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

/**
 * check if [insn] is a return point
 */
fun isReturnNode(insn: AbstractInsnNode): Boolean {
    return if (insn is InsnNode) {
        val opcode = insn.opcode
        opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
                opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN
    } else {
        false
    }
}

/**
 * @throws IllegalArgumentException when [type] not a primitive type
 */
fun getPrimitiveValueOf(type: Type): AbstractInsnNode {
    val objectClassName = when(type.sort) {
        Type.BOOLEAN -> "java/lang/Boolean"
        Type.CHAR -> "java/lang/Character"
        Type.BYTE -> "java/lang/Byte"
        Type.SHORT -> "java/lang/Short"
        Type.INT -> "java/lang/Integer"
        Type.FLOAT -> "java/lang/Float"
        Type.LONG -> "java/lang/Long"
        Type.DOUBLE -> "java/lang/Double"
        else -> throw IllegalArgumentException("given type not a primitive type")
    }

    return MethodInsnNode(Opcodes.INVOKESTATIC, objectClassName, "valueOf", "(${type.descriptor})L$objectClassName;")
}

fun getTypeLoadOpcode(typeDescriptor: String): Int {
    return when(typeDescriptor) {
        Type.BOOLEAN_TYPE.descriptor, Type.CHAR_TYPE.descriptor, Type.BYTE_TYPE.descriptor,
            Type.SHORT_TYPE.descriptor, Type.INT_TYPE.descriptor -> Opcodes.ILOAD
        Type.FLOAT_TYPE.descriptor -> Opcodes.FLOAD
        Type.LONG_TYPE.descriptor -> Opcodes.LLOAD
        Type.DOUBLE_TYPE.descriptor -> Opcodes.DLOAD
        else -> Opcodes.ALOAD
    }
}