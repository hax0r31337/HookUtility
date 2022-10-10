package me.yuugiri.hutil.processor.hook.point

/**
 * used to match [IHookPoint]s that has information control
 */
interface IHookPointMatcher {

    fun matches(string: String): Boolean
}

class HookPointMatcherAll : IHookPointMatcher {

    override fun matches(string: String) = true
}

class HookPointMatcherRaw(val cond: String) : IHookPointMatcher {

    override fun matches(string: String) = string == cond
}

class HookPointMatcherRegex(val cond: Regex) : IHookPointMatcher {

    override fun matches(string: String) = cond.matches(string)
}

