package me.yuugiri.hutil.processor.hook

import me.yuugiri.hutil.processor.hook.point.IHookPoint

class HookInfo(val point: IHookPoint, val hookShift: EnumHookShift, val ordinal: Int = 1) {
}