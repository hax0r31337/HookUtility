package me.yuugiri.hutil.obfuscation

import java.io.BufferedReader

/**
 * obfuscation map that load from searge formatted files
 */
class SeargeObfuscationMap(mapping: BufferedReader) : SimpleObfuscationMap() {

    init {
        mapping.readLines().forEach {
            val args = it.split(" ")
            when(args[0]) {
                "CL:" -> {
                    addClassRecord(ClassObfuscationRecord(args[1], args[2]))
                }
                "FD:" -> {
                    val name = args[1]
                    val srg = args[2]
                    addFieldRecord(FieldObfuscationRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1)))
                }
                "MD:" -> {
                    val name = args[1]
                    val srg = args[3]
                    addMethodRecord(MethodObfuscationRecord(name.substring(0, name.lastIndexOf('/')), srg.substring(0, srg.lastIndexOf('/')),
                        name.substring(name.lastIndexOf('/')+1), srg.substring(srg.lastIndexOf('/')+1), args[2], args[4]))
                }
            }
        }
    }
}