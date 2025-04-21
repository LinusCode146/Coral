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
        is ExpressionStatement -> eval(node.expression, env)
        is IntegerLiteral -> Integer(node.value)
        is Bool -> boolToBoolean(node.value)
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


// Functions
fun applyFunction(fn: Obj, args: MutableList<Obj>): Obj {
    val function = fn as  Function
    val extendedEnv = extendFunctionEnv(function, args)
    val evaluated = eval(function.body, extendedEnv)
    return unwrapReturnValue(evaluated)
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
        operator == "==" -> boolToBoolean(left == right)
        operator == "!=" -> boolToBoolean(left != right)
        left.type() != right.type() -> newError("missmatch ${left.type()} $operator ${right.type()}")
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

// Helpers / Statements / Identifiers
fun evalIdentifier(node: Identifier, env: Environment): Obj {
    val (value, ok) = env.get(node.value)
    if(!ok) {
        return newError("Identifier not found: ${node.value}")
    }
    return value
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