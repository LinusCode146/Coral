package main.coral.`object`

import main.coral.ast.BlockStatement
import main.coral.ast.Identifier
typealias ObjectType = String

// Types
const val INTEGER_OBJ = "INTEGER"
const val BOOL_OBJ = "BOOLEAN"
const val RETURN_VALUE_OBJ = "RETURN_VALUE_OBJECT"
const val NULL_OBJ = "NULL"
const val ERROR_OBJ = "ERROR"
const val FUNCTION_OBJ = "FUNCTION"
const val STRING_OBJ = "STRING"
const val BUILTIN_OBJ = "BUILTIN"
const val ARRAY_OBJ = "ARRAY"
const val HASH_OBJ = "HASH"

interface Obj {
    fun type(): ObjectType
    fun inspect(): String
}

interface Hashable {
    fun HashKey(): HashKey
}

data class HashPair(val key: Obj, val value: Obj)
data class HashKey(private val type: String, private val value: Int)

class Hash (val pairs: MutableMap<HashKey, HashPair>): Obj {
    override fun type(): ObjectType = HASH_OBJ
    override fun inspect(): String {
        val codeBuffer = StringBuilder()
        codeBuffer.appendLine("{")
        for ((_, value) in pairs) {
            codeBuffer.appendLine("${value.key.inspect()} : ${value.value.inspect()}")
        }
        codeBuffer.appendLine("}")
        return codeBuffer.toString()
    }
}

fun HashKey(b: Flag): HashKey {
    val value: Int = if(b.value) {
        1
    }else{
        2
    }
    return HashKey(b.type(), value)
}

fun HashKey(i: Integer): HashKey {
    return HashKey(i.type(), i.value)
}

fun hashKey(value: StringOBJ): HashKey {
    val hash = value.value.hashCode()
    return HashKey(type = value.type(), value = hash.toLong().toInt())
}


class ArrayList(val elements: MutableList<Obj>): Obj {
    override fun type(): ObjectType {
        return ARRAY_OBJ
    }
    override fun inspect(): String  {
        val codeBuffer = StringBuilder()
        codeBuffer.append("[")
        for  (element in elements) {
            codeBuffer.append("${element.inspect()}, ")
        }
        codeBuffer.append("]")
        return codeBuffer.toString()
    }

    fun len(): Integer = Integer(elements.size)
    fun add(element: Obj) {
        elements.add(element)
    }
}

class Builtin(@Suppress("unused")val fn: (Array<Obj>) -> Obj) : Obj {
    override fun type(): ObjectType {
        return BUILTIN_OBJ
    }
    override fun inspect(): String =  "builtin function"
}

class Integer(val value: Int): Obj {
    override fun type(): ObjectType {
        return INTEGER_OBJ
    }
    override fun inspect(): String =  "$value"
}
class Flag(val value: Boolean): Obj {
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

class ReturnValue(val value: Obj): Obj {
    override fun type(): ObjectType {
        return RETURN_VALUE_OBJ
    }
    override fun inspect(): String = value.inspect()
}

class StringOBJ(val value: String): Obj, Hashable {
    override fun type(): ObjectType = STRING_OBJ
    override fun inspect(): String = value

    override fun HashKey(): HashKey {
        // Simple hash based on built-in hashCode
        val hash = value.hashCode()
        return HashKey(type = type(), value = hash.toLong().toInt())
    }
    fun len(): Integer = Integer(value.length)

    fun reverse(): StringOBJ = StringOBJ(value.reversed())
}

class Error(private val message: String): Obj {
    override fun type(): ObjectType =  ERROR_OBJ
    override fun inspect(): String = "ERROR:  $message"
}

class Function(
    val parameters: List<Identifier>,
    val body: BlockStatement,
    val env: Environment
) : Obj {

    override fun type(): ObjectType {
        return FUNCTION_OBJ
    }

    override fun inspect(): String {
        val params = parameters.joinToString(", ") { it.toString() }

        return buildString {
            append("fn(")
            append(params)
            append(") {\n")
            append(body.toString())
            append("\n}")
        }
    }
}



