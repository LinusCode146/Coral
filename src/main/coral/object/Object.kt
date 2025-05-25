package main.coral.`object`

import main.coral.ast.BlockStatement
import main.coral.ast.Identifier
import main.coral.evaluator.NULL
import main.coral.evaluator.boolToBoolean

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

class Hash(val pairs: MutableMap<HashKey, HashPair>) : Obj {
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

    // Check if a key exists in the hash
    fun containsKey(key: Obj): Obj {
        if (key !is Hashable) return boolToBoolean(false)
        return boolToBoolean( pairs.containsKey(key.HashKey()))
    }

    fun add(key: Obj, value: Obj): Obj {
        if (key !is Hashable) {
            return Error("Unhashable type: ${key.type()}")
        }
        if(key in keys().elements) return Error("Hash can only contain unique keys, key already exists.")
        val hashKey = key.HashKey()
        pairs[hashKey] = HashPair(key, value)
        return value
    }

    // Remove a key-value pair by key
    fun remove(key: Obj): Obj {
        if (key !is Hashable) return Error("Unhashable type: ${key.type()}")
        val hashKey = key.HashKey()
        val removed = pairs.remove(hashKey)
        return removed?.value ?: Null()
    }

    // Return list of keys
    fun keys(): ArrayList {
        val result = mutableListOf<Obj>()
        for ((_, pair) in pairs) {
            result.add(pair.key)
        }
        return ArrayList(result)
    }

    // Return list of values
    fun values(): ArrayList {
        val result = mutableListOf<Obj>()
        for ((_, pair) in pairs) {
            result.add(pair.value)
        }
        return ArrayList(result)
    }

    // Clear all entries
    fun clear() {
        pairs.clear()
    }

    // Return the number of elements in the hash
    fun size(): Integer {
        return Integer(pairs.size)
    }

    // Check if the hash is empty
    fun isEmpty(): Flag {
        return boolToBoolean(pairs.isEmpty())
    }
    // Check if the hash is empty
    fun isNotEmpty(): Flag {
        return boolToBoolean(!pairs.isEmpty())
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

    fun extend(other: ArrayList) {
        elements.addAll(other.elements)
    }

    fun append(element: Obj) {
        elements.add(element)
    }

    fun reverse() {
        elements.reverse()
    }

    fun toReversed(): ArrayList {
        val reversedElements = elements.asReversed().toMutableList()
        return ArrayList(reversedElements)
    }

    fun pop(): Obj {
        val el =  elements[elements.size - 1]
        elements.removeAt(elements.size - 1)
        return el
    }

    fun filter(predicate: Obj): Obj {
        if (predicate !is Function && predicate !is Builtin) {
            return Error("Argument to 'filter' must be a function")
        }

        val resultElements = mutableListOf<Obj>()

        for (element in elements) {
            val args = listOf(element)
            val result = when (predicate) {
                is Builtin -> predicate.fn(args.toTypedArray())
                is Function -> {
                    val extendedEnv = newEnclosedEnvironment(predicate.env)
                    predicate.parameters.forEachIndexed { index, param ->
                        extendedEnv.set(param.value, args[index])
                    }
                    val evaluated = main.coral.evaluator.eval(predicate.body, extendedEnv)
                    main.coral.evaluator.unwrapReturnValue(evaluated)
                }
                else -> Null()
            }

            if (main.coral.evaluator.isError(result)) return result
            if (main.coral.evaluator.isTruthy(result)) {
                resultElements.add(element)
            }
        }

        return ArrayList(resultElements)
    }

    fun map(transform: Obj): Obj {
        if (transform !is Function && transform !is Builtin) {
            return Error("Argument to 'map' must be a function")
        }

        val resultElements = mutableListOf<Obj>()

        for (element in elements) {
            val args = listOf(element)
            val result = when (transform) {
                is Builtin -> transform.fn(args.toTypedArray())
                is Function -> {
                    val extendedEnv = newEnclosedEnvironment(transform.env)
                    transform.parameters.forEachIndexed { index, param ->
                        extendedEnv.set(param.value, args[index])
                    }
                    val evaluated = main.coral.evaluator.eval(transform.body, extendedEnv)
                    main.coral.evaluator.unwrapReturnValue(evaluated)
                }
                else -> Null()
            }

            if (main.coral.evaluator.isError(result)) return result
            resultElements.add(result)
        }

        return ArrayList(resultElements)
    }

    fun reduce(reducer: Obj, initial: Obj): Obj {
        if (reducer !is Function && reducer !is Builtin) {
            return Error("Argument to 'reduce' must be a function")
        }

        var accumulator = initial

        for (element in elements) {
            val args = listOf(accumulator, element)
            val result = when (reducer) {
                is Builtin -> reducer.fn(args.toTypedArray())
                is Function -> {
                    val extendedEnv = newEnclosedEnvironment(reducer.env)
                    reducer.parameters.forEachIndexed { index, param ->
                        extendedEnv.set(param.value, args[index])
                    }
                    val evaluated = main.coral.evaluator.eval(reducer.body, extendedEnv)
                    main.coral.evaluator.unwrapReturnValue(evaluated)
                }
                else -> Null()
            }

            if (main.coral.evaluator.isError(result)) return result
            accumulator = result
        }

        return accumulator
    }

    fun isEmpty(): Obj = boolToBoolean(elements.isEmpty())

    fun isNotEmpty(): Obj = boolToBoolean(!elements.isEmpty())

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

    fun reversed(): StringOBJ = StringOBJ(value.reversed())
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



