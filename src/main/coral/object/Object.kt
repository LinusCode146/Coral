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

interface Obj {
    fun type(): ObjectType
    fun inspect(): String
}

class ArrayList(val elements: List<Obj>): Obj {
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

class ReturnValue(val value: Obj): Obj {
    override fun type(): ObjectType {
        return RETURN_VALUE_OBJ
    }
    override fun inspect(): String = value.inspect()
}

class StringOBJ(val value: String): Obj {
    override fun type(): ObjectType = STRING_OBJ
    override fun inspect(): String = value

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



