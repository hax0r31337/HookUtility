package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.processor.hook.point.IHookPoint

class HookInfo(
    val target: AbstractHookTarget,
    val callback: (MethodHookParam) -> Unit,
    val point: IHookPoint,
    val hookShift: EnumHookShift,
    /**
     * hook all points when ordinal is -1
     * @throws IllegalArgumentException when ordinal not in -1..points.size
     */
    val ordinal: Int = -1,
    /**
     * TODO: checks the arguments got modified or not, and gives the fresh arguments
     */
//    val freshArgs: Boolean = true
)