package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.util.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

interface IHookInsnPoint {

    fun injectHook(method: MethodNode, callbackId: Int, shift: EnumHookShift)
}

class HookInsnPoint(
    /**
     * AbstractInsnNodes(hook points) that matches the hook rule
     */
    val node: AbstractInsnNode,
    val type: EnumPointType = EnumPointType.COMMON) : IHookInsnPoint {

    override fun injectHook(method: MethodNode, callbackId: Int, shift: EnumHookShift) {
        val insnList = method.instructions

        if (!insnList.contains(node)) {
            throw IllegalArgumentException("passed method node instructions not contains target node")
        }

        if ((type == EnumPointType.RETURN || type == EnumPointType.THROWN) && shift == EnumHookShift.AFTER) throw IllegalArgumentException("shift mode $shift not supported for type $type")

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

        // parse method description to get arguments and return type
        val methodType = Type.getMethodType(method.desc)

        // throwable/return already on stack
        if (type == EnumPointType.RETURN && methodType.returnType.sort.let{ it != Type.OBJECT && it != Type.ARRAY && it != Type.VOID }) {
            push(getPrimitiveValueOf(methodType.returnType))
        }

        // push thisObject to stack
        var loadIndex = 0
        if (method.access and Opcodes.ACC_STATIC != 0) {
            push(InsnNode(Opcodes.ACONST_NULL)) // pass null in static methods
        } else {
            push(VarInsnNode(Opcodes.ALOAD, 0))
            loadIndex++
        }

        // create array for arguments
        push(getInsnInt(methodType.argumentTypes.size))
        push(TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"))

        // store arguments into the array
        methodType.argumentTypes.forEachIndexed { index, type ->
            push(InsnNode(Opcodes.DUP)) // duplicate the array on stack to make the array won't disappear after AASTORE
            push(getInsnInt(index))
            when(type.sort) {
                Type.OBJECT, Type.ARRAY -> push(VarInsnNode(getTypeLoadOpcode(type.descriptor), loadIndex))
                else -> {
                    push(VarInsnNode(getTypeLoadOpcode(type.descriptor), loadIndex))
                    push(getPrimitiveValueOf(type))
                }
            }
            loadIndex += type.size
            push(InsnNode(Opcodes.AASTORE))
        }

        // construct MethodHookParam
        push(when {
            type == EnumPointType.RETURN && methodType.returnType.descriptor != Type.VOID_TYPE.descriptor ->
                MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam", "withReturn", "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
            type == EnumPointType.THROWN -> MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam", "withThrowable", "(Ljava/lang/Throwable;Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
            else -> MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookParam", "raw", "(Ljava/lang/Object;[Ljava/lang/Object;)Lme/yuugiri/hutil/processor/hook/MethodHookParam;")
        })

        // invoke hook
        push(InsnNode(Opcodes.DUP))
        push(getInsnInt(callbackId))
        push(MethodInsnNode(Opcodes.INVOKESTATIC, "me/yuugiri/hutil/processor/hook/MethodHookProcessor", "hookCallback", "(Lme/yuugiri/hutil/processor/hook/MethodHookParam;I)V"))

        when(type) {
            EnumPointType.THROWN -> {
                push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/processor/hook/MethodHookParam", "getThrowable", "()Ljava/lang/Throwable;"))
                // cancel the throw process by set throwable in MethodHookParam to null
                push(InsnNode(Opcodes.DUP))
                val label1 = LabelNode()
                push(JumpInsnNode(Opcodes.IFNULL, label1))
                insnList.insert(node, label1)
                insnList.insert(label1, InsnNode(Opcodes.POP))

                // check control flow
                var hasReturn = false
                for (i in insnList.indexOf(label1) until insnList.size()) {
                    if (isReturnNode(insnList.get(i))) {
                        hasReturn = true
                        break
                    }
                }
                if (!hasReturn) {
                    // inject dummy return to prevent control flow falls to end
                    val returnDesc = methodType.returnType.descriptor
                    val lastNode = insnList.get(insnList.size() - 1)
                    // in reversed order
                    insnList.insert(lastNode, InsnNode(getTypeReturnOpcode(returnDesc)))
                    when(returnDesc) {
                        Type.VOID_TYPE.descriptor -> null
                        Type.BOOLEAN_TYPE.descriptor, Type.CHAR_TYPE.descriptor, Type.BYTE_TYPE.descriptor,
                        Type.SHORT_TYPE.descriptor, Type.INT_TYPE.descriptor -> Opcodes.ICONST_0
                        Type.FLOAT_TYPE.descriptor -> Opcodes.FCONST_0
                        Type.LONG_TYPE.descriptor -> Opcodes.LCONST_0
                        Type.DOUBLE_TYPE.descriptor -> Opcodes.DCONST_0
                        else -> Opcodes.ACONST_NULL
                    }?.let { insnList.insert(lastNode, InsnNode(it)) }
                }
            }
            EnumPointType.RETURN -> {
                if (methodType.returnType.descriptor != Type.VOID_TYPE.descriptor) {
                    push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/processor/hook/MethodHookParam", "getResult", "()Ljava/lang/Object;"))
                    castToType(methodType.returnType).forEach {
                        push(it)
                    }
                }
            }
            else -> {
                // check interrupt
                push(InsnNode(Opcodes.DUP))
                push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/processor/hook/MethodHookParam", "getResultModified", "()Z"))
                val label1 = LabelNode()
                push(JumpInsnNode(Opcodes.IFEQ, label1))
                val returnOpcode = getTypeReturnOpcode(methodType.returnType.descriptor)
                if (returnOpcode != Opcodes.RETURN) { // void type
                    push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/processor/hook/MethodHookParam", "getResult", "()Ljava/lang/Object;"))
                    castToType(methodType.returnType).forEach {
                        push(it)
                    }
                }
                push(InsnNode(returnOpcode))
                push(label1)

                // check arguments modify
//                push(InsnNode(Opcodes.DUP))
                push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/processor/hook/MethodHookParam", "getArgs", "()Lme/yuugiri/hutil/util/ModifyRecordArray;"))
                push(InsnNode(Opcodes.DUP))
                push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/util/ModifyRecordArray", "getHasModified", "()Z"))
                val label2 = LabelNode()
                push(JumpInsnNode(Opcodes.IFEQ, label2))
                // exchange values
                push(InsnNode(Opcodes.DUP))
                push(MethodInsnNode(Opcodes.INVOKEVIRTUAL, "me/yuugiri/hutil/util/ModifyRecordArray", "getArray", "()[Ljava/lang/Object;"))
                loadIndex = if (method.access and Opcodes.ACC_STATIC == 0) 1 else 0
                methodType.argumentTypes.forEachIndexed { index, type ->
                    push(InsnNode(Opcodes.DUP))
                    push(getInsnInt(index))
                    push(InsnNode(Opcodes.AALOAD))
                    castToType(type).forEach {
                        push(it)
                    }
                    push(VarInsnNode(getTypeStoreOpcode(type.descriptor), loadIndex))
                    loadIndex += type.size
                }
                push(InsnNode(Opcodes.POP))
                push(label2)
                push(InsnNode(Opcodes.POP))


                // pop the MethodHookParam that on stack
//                push(InsnNode(Opcodes.POP))
            }
        }
    }
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