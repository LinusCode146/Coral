package main.coral.`object`

typealias ObjectType = String

// Types
const val INTEGER_OBJ = "INTEGER"
const val BOOL_OBJ = "BOOLEAN"
const val NULL_OBJ = "NULL"

interface Obj {
    fun type(): ObjectType
    fun inspect(): String
}

class Integer(val value: Int): Obj {
    override fun type(): ObjectType {
        return INTEGER_OBJ
    }
    override fun inspect(): String =  "$value"
}
class Flag(private val value: Boolean): Obj {
    override fun type(): ObjectType {
        return BOOL_OBJ
    }
    override fun inspect(): String =  "$value"
}

class Null: Obj {
    override fun type(): ObjectType {
        return NULL_OBJ
    }
    override fun inspect(): String =  "null"
}


