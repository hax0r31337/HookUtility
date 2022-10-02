package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.tree.ClassNode

object MethodHookProcessor : IClassProcessor {

    private val hookInfoList = ArrayList<HookInfo?>()
    private var hookInfoIndex = 0

    private val hookInfoEntry: List<Pair<Int, HookInfo?>>
        get() = hookInfoList.mapIndexed { index, hookInfo -> index to hookInfo }

    fun addHookInfo(info: HookInfo) {
        hookInfoList.add(hookInfoIndex++, info)
    }

    fun removeHookInfo(info: HookInfo) {
        if (hookInfoList.contains(info)) {
            hookInfoList.add(hookInfoList.indexOf(info), null)
        }
    }

    fun removeHookInfo(key: Int) {
        hookInfoList.add(key, null)
    }

    /**
     * used to receive hooks, DO NOT CALL THIS METHOD MANUALLY
     */
    @JvmStatic
    fun hookCallback(param: MethodHookParam, id: Int) {
        if (id !in 0 until hookInfoList.size) return
        val info = hookInfoList[id] ?: return
        info.callback(param)
    }

    override fun selectClass(name: String) = hookInfoList.any { it?.target?.classMatches(name) ?: false }

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var name = map.name
        val entries = hookInfoEntry
        var selectedRecords = entries.filter { it.second?.target?.classMatches(name) ?: false }.toMutableList()
        if (selectedRecords.isEmpty()) {
            name = map.obfuscatedName
            selectedRecords = entries.filter { it.second?.target?.classMatches(name) ?: false }.toMutableList()
            if (selectedRecords.isEmpty()) return false
        }
        klass.methods.forEach { method ->
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var hooks = selectedRecords.filter { it.second!!.target.methodMatches(obf.name, obf.description) }
            if (hooks.isEmpty() && obf.name != method.name) {
                hooks = selectedRecords.filter { it.second!!.target.methodMatches(method.name, method.desc) }
            }
            hooks.forEach { entry ->
                val info = entry.second!!
                info.point.hookPoints(obfuscationMap, klass, method).forEach { point ->
                    point.injectHook(method, entry.first, info.hookShift)
                }
                selectedRecords.remove(entry)
            }
        }
        if (selectedRecords.isNotEmpty()) {
            throw IllegalStateException("some hooks not applied (class=${klass.name}, hook=${selectedRecords.size})")
        }

        return true
    }
}