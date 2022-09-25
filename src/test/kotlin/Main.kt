import me.yuugiri.hutil.HookUtility
import me.yuugiri.hutil.obfuscation.SeargeObfuscationMap
import me.yuugiri.hutil.processor.AccessProcessor
import java.io.BufferedReader
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider

fun main(args: Array<String>) {
    val testFolder = File("./run")
    packJar(testFolder, "original")

    compileTestClass(testFolder)

    val hookUtility = HookUtility()

    // test obfuscation map
    hookUtility.obfuscationMap = SeargeObfuscationMap(BufferedReader("""
CL: test/DynaCompile TheClass
FD: test/DynaCompile/a TheClass/field_1
MD: test/DynaCompile/a ()Ljava/lang/String; TheClass/methodReturnsString ()Ljava/lang/String;
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

    listFiles(testFolder).forEach {
        println("Patching ${it.absolutePath}")
        it.writeBytes(hookUtility.dealWithClassData(it.readBytes()))
    }
    packJar(testFolder, "patched")
}

private fun compileTestClass(folder: File) {
    val sourceFile = File(folder, "test/DynaCompile.java").apply { parentFile.mkdirs() }
    sourceFile.writeBytes(Dummy::class.java.classLoader.getResourceAsStream("compile.java").readBytes())

    val compiler = ToolProvider.getSystemJavaCompiler()
    compiler.run(null, null, null, sourceFile.path)
}