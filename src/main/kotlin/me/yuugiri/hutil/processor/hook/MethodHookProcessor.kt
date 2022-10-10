package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.tree.ClassNode
import java.util.concurrent.atomic.AtomicBoolean

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
    @JvmName("hookCallback")
    @JvmStatic
    internal fun hookCallback(param: MethodHookParam, id: Int) {
        if (id !in 0 until hookInfoList.size) return
        val info = hookInfoList[id] ?: return
        info.callback(param)
    }

    override fun selectClass(name: String) = hookInfoList.any { it?.target?.classMatches(name) ?: false }

    override fun processClass(obfuscationMap: AbstractObfuscationMap?, map: AbstractObfuscationMap.ClassObfuscationRecord, klass: ClassNode): Boolean {
        var name = map.name
        val entries = hookInfoEntry
        val selectedRecords = run {
            entries.filter { it.second?.target?.classMatches(name) ?: false }.let {
                it.ifEmpty {
                    name = map.obfuscatedName
                    entries.filter { it.second?.target?.classMatches(name) ?: false }
                }
            }
        }.also { if(it.isEmpty()) return false }.map { it to AtomicBoolean(false) }
        klass.methods.forEach { method ->
            val obf = AbstractObfuscationMap.methodObfuscationRecord(obfuscationMap, klass.name, method)
            var hooks = selectedRecords.filter { it.first.second!!.target.methodMatches(obf.name, obf.description) }
            if (hooks.isEmpty() && obf.name != method.name) {
                hooks = selectedRecords.filter { it.first.second!!.target.methodMatches(method.name, method.desc) }
            }
            hooks.forEach { entry ->
                val info = entry.first.second!!
                val points = info.point.hookPoints(obfuscationMap, klass, method)
                (if (info.ordinal != -1) {
                    if (info.ordinal >= points.size) throw IllegalArgumentException("Attempt hook point-${info.ordinal} but only ${points.size} exists")
                    listOf(points[info.ordinal])
                } else points).forEach { point ->
                    point.injectHook(method, entry.first.first, info.hookShift)
                }
                entry.second.set(true)
            }
        }
        if (selectedRecords.any { !it.second.get() }) {
            throw IllegalStateException("some hooks not applied (class=${klass.name}, hook=${selectedRecords.count { !it.second.get() }})")
        }

        return true
    }
}
