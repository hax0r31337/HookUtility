import me.yuugiri.hutil.HookUtility
import me.yuugiri.hutil.obfuscation.SeargeObfuscationMap
import me.yuugiri.hutil.processor.AccessProcessor
import me.yuugiri.hutil.processor.hook.EnumHookShift
import me.yuugiri.hutil.processor.hook.HookInfo
import me.yuugiri.hutil.processor.hook.HookTargetImpl
import me.yuugiri.hutil.processor.hook.MethodHookProcessor
import me.yuugiri.hutil.processor.hook.point.HookPointEnter
import me.yuugiri.hutil.processor.hook.point.HookPointExit
import me.yuugiri.hutil.processor.hook.point.HookPointTail
import me.yuugiri.hutil.processor.hook.point.HookPointThrow
import java.io.BufferedReader
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider

fun main(args: Array<String>) {
    val testFolder = File("./run")

    compileTestClass(testFolder)
    packJar(testFolder, "original")

    val hookUtility = HookUtility()

    // test obfuscation map
    hookUtility.obfuscationMap = SeargeObfuscationMap(BufferedReader("""
CL: test/DynaCompile TheClass
FD: test/DynaCompile/a TheClass/field_1
MD: test/DynaCompile/a (I)Ljava/lang/String; TheClass/methodReturnsString (I)Ljava/lang/String;
MD: test/DynaCompile/b ()I TheClass/methodReturnsInt ()I
    """.trimIndent().reader()))

    // test AccessProcessor
    hookUtility.processorList.add(AccessProcessor.fromFMLAccessTransformer(BufferedReader("""
# test comment
public-f TheClass main()V # only with class obfuscate
public+f TheClass method_0()Ljava/lang/String; # with obfuscate and mapping
protected-f TheClass methodReturnsInt()I # with obfuscate but without mapping

public-f TheClass field_1 # also can transform fields
    """.trimIndent().reader()), mapOf("method_0" to "methodReturnsString")))

    // custom processor
    hookUtility.processorList.add(TestProcessor())

    MethodHookProcessor.addHookInfo(HookInfo(HookTargetImpl("TheClass", "methodReturnsString"), HookPointExit(), EnumHookShift.BEFORE) {
        println("HOOK: ${it.result}")
        it.result = "modified-a"
    })
    MethodHookProcessor.addHookInfo(HookInfo(HookTargetImpl("TheClass", "test"), HookPointEnter(), EnumHookShift.BEFORE) {
        println("HOOK")
    })
    MethodHookProcessor.addHookInfo(HookInfo(HookTargetImpl("TheClass", "test"), HookPointExit(), EnumHookShift.BEFORE) {
        println("HOOK1")
    })
    MethodHookProcessor.addHookInfo(HookInfo(HookTargetImpl("TheClass", "throwed"), HookPointThrow(), EnumHookShift.BEFORE) {
        println("HOOK: ${it.throwable}")
        it.throwable = null
    })
    hookUtility.processorList.add(MethodHookProcessor)

    listFiles(testFolder).forEach {
        println("Patching ${it.absolutePath}")
        it.writeBytes(hookUtility.dealWithClassData(it.readBytes()))
    }
    packJar(testFolder, "patched")

    loadAndInvokeClass(testFolder, "test.DynaCompile")
}

private fun loadAndInvokeClass(folder: File, className: String) {
    val cl = URLClassLoader(arrayOf(folder.toURI().toURL()))
    val klass = cl.loadClass(className)

    val method = klass.getDeclaredMethod("throwed", Int::class.javaPrimitiveType).apply { isAccessible = true }
    println(method.invoke(null, -1))
}

private fun compileTestClass(folder: File) {
    val sourceFile = File(folder, "test/DynaCompile.java").apply { parentFile.mkdirs() }
    sourceFile.writeBytes(Dummy::class.java.classLoader.getResourceAsStream("compile.java").readBytes())

    val compiler = ToolProvider.getSystemJavaCompiler()
    compiler.run(null, null, null, sourceFile.path)
}