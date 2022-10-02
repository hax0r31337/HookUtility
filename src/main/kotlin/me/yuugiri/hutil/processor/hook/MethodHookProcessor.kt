package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.tree.ClassNode

object MethodHookProcessor : IClassProcessor {

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

    /**
     * used to receive hooks, DO NOT CALL THIS METHOD MANUALLY
     */
    @JvmStatic
    fun hookCallback(param: MethodHookParam, id: Int) {
        val info = hookInfoMap[id] ?: return
        info.callback(param)
    }

    override fun selectClass(name: String) = hookInfoList.any { it.target.classMatches(name) }

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var name = map.name
        var selectedRecords = hookInfoMap.entries.filter { it.value.target.classMatches(name) }
        if (selectedRecords.isEmpty()) {
            name = map.obfuscatedName
            selectedRecords = hookInfoMap.entries.filter { it.value.target.classMatches(name) }
            if (selectedRecords.isEmpty()) return false
        }
        klass.methods.forEach { method ->
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var hooks = selectedRecords.filter { it.value.target.methodMatches(obf.name, obf.description) }
            if (hooks.isEmpty() && obf.name != method.name) {
                hooks = selectedRecords.filter { it.value.target.methodMatches(method.name, method.desc) }
            }
            hooks.forEach { hookInfo ->
                hookInfo.value.point.hookPoints(obfuscationMap, klass, method).forEach { point ->
                    point.injectHook(method, hookInfo.key, hookInfo.value.hookShift)
                }
            }
        }

        return true
    }
}