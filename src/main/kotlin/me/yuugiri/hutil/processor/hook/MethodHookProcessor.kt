package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.tree.ClassNode

class MethodHookProcessor : IClassProcessor {

    override fun selectClass(name: String) = hookInfoList.any { it.target.classMatches(name) }
    override fun processClass(
        obfuscationMap: AbstractObfuscationMap?,
        map: AbstractObfuscationMap.ClassObfuscationRecord,
        klass: ClassNode
    ): Boolean {
        TODO("Not yet implemented")
    }

    companion object {
        // make the map be static to receive callbacks from hooked method

        private val hookInfoMap = hashMapOf<Int, HookInfo>()
        private var hookInfoIndex = 0

        /**
         * immutable
         */
        val hookInfoList: Collection<HookInfo>
            get() = hookInfoMap.values

        fun addHookInfo(info: HookInfo) {
            hookInfoMap[hookInfoIndex++] = info
        }

        fun removeHookInfo(info: HookInfo) {
            hookInfoMap.entries.map { it }.forEach { (k, v) ->
                if (info == v) {
                    removeHookInfo(k)
                    return
                }
            }
        }

        fun removeHookInfo(key: Int) {
            hookInfoMap.remove(key)
        }

        fun hookCallback(param: MethodHookParam, id: Int) {
            val info = hookInfoMap[id] ?: return
            info.callback(param)
        }
    }
}