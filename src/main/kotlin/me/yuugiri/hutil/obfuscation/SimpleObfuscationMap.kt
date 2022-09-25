package me.yuugiri.hutil.obfuscation

/**
 * a simple implementation of [AbstractObfuscationMap] that loads manually
 */
open class SimpleObfuscationMap : AbstractObfuscationMap() {

    val classRecords = mutableMapOf<String, ClassObfuscationRecord>()
    val fieldRecords = mutableMapOf<String, FieldObfuscationRecord>()
    val methodRecords = mutableMapOf<String, MethodObfuscationRecord>()

    fun addClassRecord(record: ClassObfuscationRecord) {
        classRecords[record.identifier] = record
    }

    fun addFieldRecord(record: FieldObfuscationRecord) {
        fieldRecords[record.identifier] = record
    }

    fun addMethodRecord(record: MethodObfuscationRecord) {
        methodRecords[record.identifier] = record
    }

    override fun mapClass(name: String) = classRecords[name]

    override fun mapField(owner: String, name: String) = fieldRecords["$owner/$name"]

    override fun mapMethod(owner: String, name: String, desc: String) = methodRecords["$owner/$name$desc"]
}