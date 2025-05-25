package main.coral.evaluator

import main.coral.ast.*
import main.coral.`object`.*
import main.coral.`object`.Function

val TRUE = Flag(true)
val FALSE = Flag(false)
val NULL = Null()

fun eval(node: Node, env: Environment): Obj {
    return when (node) {
        is Program -> evalProgram(node, env)
        is ChainExpression -> {
            val leftExp = eval(node.left, env)
            if (isError(leftExp)) return leftExp
            evalChainedExpression(leftExp, node.right, env)
        }
        is ArrayLiteral -> {
            val elements = evalExpressions(node.elements, env)
            if(elements.size == 1 && isError(elements.first())) {
                return elements.first()
            }
            return ArrayList(elements)
        }
        is IndexExpression -> {
            val left = eval(node.left, env)
            if(isError(left)) {
                return left
            }
            val index = eval(node.index, env)
            if(isError(index)) {
                return index
            }
            return evalIndexExpression(left, index)
        }
        is ExpressionStatement -> eval(node.expression, env)
        is HashLiteral -> evalHashLiteral(node, env)
        is IntegerLiteral -> Integer(node.value)
        is Bool -> boolToBoolean(node.value)
        is StringLiteral -> StringOBJ(node.value)
        is PrefixExpression -> {
            val right = eval(node.right, env)
            if(isError(right)) {return right}

            return evalPrefixExpression(node.operator,  right)
        }
        is InfixExpression -> {
            val right = eval(node.right, env)
            if(isError(right)) {return right}

            val left = eval(node.left, env)
            if(isError(left)) {return left}

            return evalInfixExpression(node.operator, left, right)
        }
        is BlockStatement -> evalBlockStatement(node, env)
        is IfExpression -> evalIfExpression(node, env)
        is ReturnStatement -> {
            val value = eval(node.returnValue, env)
            if(isError(value)) {return value}

            return ReturnValue(value)
        }
        is LetStatement -> {
            val value = eval(node.value, env)
            if (isError(value)) {return value}

            env.set(node.name.value, value)
        }
        is Identifier -> evalIdentifier(node, env)
        is FunctionLiteral -> {
            val parameters = node.parameters
            val body = node.body
            return Function(parameters, body, env)
        }
        is CallExpression -> {
            val function = eval(node.function, env)
            if(isError(function)) {return function}

            val args = evalExpressions(node.arguments, env)
            if(args.size == 1 && isError(args[0])) {
                return args[0]
            }
            return applyFunction(function, args)
        }
        else -> NULL
    }
}
fun evalProgram(program: Program, env: Environment): Obj {
    var result: Obj = NULL
    for (statement in program.statements) {
        result = eval(statement, env)

        when (result) {
            is ReturnValue -> return result.value
            is Error -> return result
        }
    }

    return result
}

fun evalHashLiteral(node: HashLiteral, env: Environment): Obj {
    val pairs = mutableMapOf<HashKey, HashPair>()

    for ((keyNode, valNode) in node.pairs) {
        val key = eval(keyNode, env)
        if(isError(key)) {
            return NULL
        }
        val hashKey = key as Hashable
        val value = eval(valNode, env)
        if(isError(value)) {
            return NULL
        }
        val hashed = hashKey.HashKey()
        pairs[hashed] = HashPair(key, value)
    }

    return Hash(pairs)
}


// Functions
fun applyFunction(fn: Obj, args: MutableList<Obj>): Obj {
    return when (fn) {
        is Builtin -> fn.fn(args.toTypedArray())
        is Function -> {
            val extendedEnv = extendFunctionEnv(fn, args)
            val evaluated = eval(fn.body, extendedEnv)
            unwrapReturnValue(evaluated)
        }
        else -> newError("unknown function call $fn")
    }
}

fun extendFunctionEnv(fn: Function, args: List<Obj>): Environment {
    val env = newEnclosedEnvironment(fn.env)
    fn.parameters.forEachIndexed { index, param ->
        env.set(param.value, args[index])
    }
    return env
}
fun unwrapReturnValue(obj: Obj): Obj {
    return if (obj is ReturnValue) {
        obj.value
    } else {
        obj
    }
}

// Expressions
fun evalExpressions(exps: List<Expression>, env: Environment): MutableList<Obj> {
    val result = mutableListOf<Obj>()

    for (exp in exps) {
        val evaluated = eval(exp, env)

        if(isError(evaluated)) {
            return mutableListOf(evaluated)
        }
        result.add(evaluated)
    }
    return result
}
fun evalIfExpression(node: IfExpression, env: Environment): Obj {
    val condition = eval(node.condition, env)
    if(isError(condition)) {return condition}

    return if(isTruthy(condition)) {
        eval(node.consequence, env)
    }else if(node.hasAlternative()) {
        eval(node.alternative, env)
    }else{
        NULL
    }
}
fun evalPrefixExpression(operator: String, right: Obj): Obj {
    return when (operator) {
        "!" -> evalBangPrefixOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> newError("unknown operator $operator ${right.type()}")
    }
}
fun evalInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    return when {
        left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ -> evalIntegerInfixExpression(operator, left, right)
        left.type() == STRING_OBJ && right.type() == STRING_OBJ -> evalStringInfixExpression(operator, left, right)
        operator == "==" -> boolToBoolean(left == right)
        operator == "!=" -> boolToBoolean(left != right)
        left.type() != right.type() -> newError("mismatch ${left.type()} $operator ${right.type()}")
        else -> newError("unknown operator ${left.type()} $operator ${right.type()}")
    }
}
fun evalBangPrefixOperatorExpression(right: Obj): Obj {
    return when (right) {
        TRUE -> FALSE
        FALSE-> TRUE
        NULL -> TRUE
        else -> FALSE
    }
}
fun evalMinusPrefixOperatorExpression(right: Obj): Obj {
    if(right.type() != INTEGER_OBJ) {
        return newError("Unknown operator ${right.type()}")
    }
    val value = (right as  Integer).value
    return Integer(-value)
}
fun evalIntegerInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    val leftNum = (left as Integer).value
    val rightNum = (right as Integer).value
    return when (operator) {
        "+" -> Integer(leftNum + rightNum)
        "-" -> Integer(leftNum - rightNum)
        "/" -> Integer(leftNum / rightNum)
        "*" -> Integer(leftNum * rightNum)
        "<" -> boolToBoolean(leftNum < rightNum)
        ">" -> boolToBoolean(leftNum > rightNum)
        "==" -> boolToBoolean(leftNum == rightNum)
        "!=" -> boolToBoolean(leftNum != rightNum)
        else -> newError("unknown operator $operator ${left.type()} $operator ${right.type()}")
    }
}
fun evalStringInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    if(operator != "+") {
        return newError("unknown operator ${right.type()} $operator ${left.type()}")
    }

    val leftVal = (left as StringOBJ).value
    val rightVal = (right as StringOBJ).value
    return StringOBJ(leftVal + rightVal)
}
fun evalIndexExpression(left: Obj, index: Obj): Obj {
    return when {
        left.type() == ARRAY_OBJ && index.type() == INTEGER_OBJ -> evalArrayIndexExpression(left, index)
        left.type() == HASH_OBJ -> evalHashIndexExpression(left, index)
        else -> newError("index operator not supported ${left.type()}")
    }
}

fun evalHashIndexExpression(hash: Obj, index: Obj): Obj {
    val hashObject = hash as Hash
    val key = index as Hashable
    val pair = hashObject.pairs[key.HashKey()]
    return pair?.value ?: NULL
}

fun evalChainedExpression(obj: Obj, right: Expression, env: Environment): Obj {
    return when (right) {
        is CallExpression -> {
            val args = evalExpressions(right.arguments, env)
            if (args.size == 1 && isError(args[0])) return args[0]
            evalChainedFunction(obj, right.function, args)
        }
        is IndexExpression -> {
            val index = eval(right.index, env)
            if (isError(index)) return index
            evalIndexExpression(obj, index)
        }
        else -> newError("Invalid chain target: ${right::class.simpleName}")
    }
}
fun evalChainedFunction(receiver: Obj, methodExpr: Expression, args: MutableList<Obj>): Obj {
    if (methodExpr !is Identifier) {
        return newError("Invalid method call: expected identifier")
    }

    val methodName = methodExpr.value

    return when (receiver) {
        is Hash -> {
            when (methodName) {
                "containsKey" -> {
                    if(args.size != 1) return newError("containsKey expects one argument")
                    receiver.containsKey(args[0])
                }
                "remove" -> {
                    if(args.size != 1) return newError("remove expects one argument")
                    receiver.remove(args[0])
                }
                "add" -> {
                    if(args.size != 2) return newError("add expects two argument")
                    receiver.add(args[0], args[1])
                }
                "keys" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.keys()
                }
                "values" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.values()
                }
                "clear" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.clear()
                }
                "len" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.size()
                }
                "isEmpty" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.isEmpty()
                }
                "isNotEmpty" -> {
                    if(args.isNotEmpty()) return newError("keys expects no arguments")
                    receiver.isNotEmpty()
                }

                else -> newError("Unknown method '$methodName' for Hash")
            }
        }
        is ArrayList -> {
            when (methodName) {
                "pop" -> {
                    if (args.isNotEmpty()) return newError("len() expects no arguments")
                    receiver.pop()
                }
                "len" -> {
                    if (args.isNotEmpty()) return newError("len() expects no arguments")
                    receiver.len()
                }
                "append" -> {
                    if (args.size != 1) return newError("add() expects one argument")
                    receiver.append(args[0])
                    NULL
                }
                "filter" -> {
                    if (args.size != 1) return newError("filter() expects one argument!")
                    receiver.filter(args[0])
                }
                "map" -> {
                    if (args.size != 1) return newError("map() expects one argument!")
                    receiver.map(args[0])
                }
                "reduce" -> {
                    if (args.size != 2) return newError("reduce() expects one argument")
                    receiver.reduce(args[0], args[1])
                }
                "reverse" -> {
                    if(args.isNotEmpty()) return newError("reversed() expects one argument")
                    receiver.reverse()
                    NULL
                }
                "extend" -> {
                    if(args.size != 1) return newError("extend() expects one argument")
                    receiver.extend(args[0] as ArrayList)
                    NULL
                }
                else -> newError("Unknown method '$methodName' for ArrayList")
            }
        }

        // Example for StringOBJ
        is StringOBJ -> {
            when (methodName) {
                "len" -> {
                    if (args.isNotEmpty()) return newError("len() expects no arguments")
                    receiver.len()
                }
                "reversed" -> {
                    if(args.isNotEmpty()) return newError("reverse() expects no arguments")
                    receiver.reversed()
                }
                else -> newError("Unknown method '$methodName' for String")
            }
        }

        else -> newError("Type '${receiver.type()}' does not support method '$methodName'")
    } as Obj
}


fun evalArrayIndexExpression(array: Obj, index: Obj): Obj {
    val arrayObject = array as ArrayList
    val idx = (index as Integer).value
    val max = arrayObject.elements.size - 1
    if (idx < 0 || idx > max) {
        return NULL
    }
    return arrayObject.elements[idx]
}


// Helpers / Statements / Identifiers
fun evalIdentifier(node: Identifier, env: Environment): Obj {
    val (value, ok) = env.get(node.value)
    if (ok) {
        return value
    }
    builtins[node.value]?.let { return it }

    return newError("Identifier not found: ${node.value}")
}
fun isTruthy(obj: Obj ): Boolean {
    return when (obj) {
        TRUE -> true
        FALSE -> false
        NULL -> false
        else -> true
    }
}
fun evalBlockStatement(block:  BlockStatement, env: Environment): Obj {
    var result: Obj = NULL
    for (statement in block.statements) {
        result = eval(statement, env)

        if (result != NULL && result.type() in listOf(RETURN_VALUE_OBJ, ERROR_OBJ)) {
            return result
        }
    }
    return result
}
fun boolToBoolean(bool: Boolean) = if (bool) TRUE else FALSE
fun newError(format: String, vararg args: Any): Error {
    return Error(String.format(format, *args))
}
fun isError(obj: Obj): Boolean {
    if (obj != NULL) {
        return obj.type() == ERROR_OBJ
    }
    return false
}