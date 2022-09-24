package me.yuugiri.hutil.obfuscation

/**
 * a simple implementation of [ObfuscationMap] that loads manually
 */
open class SimpleObfuscationMap : ObfuscationMap() {

    protected val classRecords = mutableMapOf<String, ObfuscationMap.ClassObfuscationRecord>()
    protected val fieldRecords = mutableMapOf<String, ObfuscationMap.FieldObfuscationRecord>()
    protected val methodRecords = mutableMapOf<String, ObfuscationMap.MethodObfuscationRecord>()

    fun addClassRecord(record: ObfuscationMap.ClassObfuscationRecord) {
        classRecords[record.identifier] = record
    }

    fun addFieldRecord(record: ObfuscationMap.FieldObfuscationRecord) {
        fieldRecords[record.identifier] = record
    }

    fun addMethodRecord(record: ObfuscationMap.MethodObfuscationRecord) {
        methodRecords[record.identifier] = record
    }

    override fun mapClass(name: String) = classRecords[name]

    override fun mapField(owner: String, name: String) = fieldRecords["$owner/$name"]

    override fun mapMethod(owner: String, name: String, desc: String) = methodRecords["$owner/$name$desc"]
}