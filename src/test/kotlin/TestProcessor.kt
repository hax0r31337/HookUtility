import me.yuugiri.hutil.obfuscation.AbstractObfuscationMap
import me.yuugiri.hutil.processor.IClassProcessor
import org.objectweb.asm.tree.ClassNode

class TestProcessor : IClassProcessor {
    override fun selectClass(name: String) = true

    override fun processClass(
        obfuscationMap: AbstractObfuscationMap?,
        map: AbstractObfuscationMap.ClassObfuscationRecord,
        klass: ClassNode
    ): Boolean {

//        klass.methods.forEach {
//            println(it.name)
//            val type = Type.getMethodType(it.desc)
//            println(type.argumentTypes.size)
//            type.argumentTypes.forEach {
//                println(it)
//            }
//        }

        return false
    }
}