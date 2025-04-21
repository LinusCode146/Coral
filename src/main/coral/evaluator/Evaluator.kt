package main.coral.evaluator

import main.coral.ast.*
import main.coral.`object`.*

val TRUE = Flag(true)
val FALSE = Flag(false)
val NULL = Null()

fun eval(node: Node): Obj? {
    return when (node) {
        is Program -> evalStatements(node.statements)
        is ExpressionStatement -> eval(node.expression)
        is IntegerLiteral -> Integer(node.value)
        is Bool -> boolToBoolean(node.value)
        is PrefixExpression -> {
            val right = eval(node.right) ?: return null
            return evalPrefixExpression(node.operator,  right)
        }
        is InfixExpression -> {
            val right = eval(node.right) ?: return null
            val left = eval(node.left) ?: return null
            return evalInfixExpression(node.operator, left, right)
        }
        is BlockStatement -> evalStatements(node.statements)
        is IfExpression -> evalIfExpression(node)
        else -> null
    }
}

fun evalIfExpression(node: IfExpression): Obj? {
    val condition = eval(node.condition)  ?: return null
    return if(isTruthy(condition)) {
        eval(node.consequence)
    }else if(node.hasAlternative()) {
        eval(node.alternative)
    }else{
        null
    }
}

fun isTruthy(obj: Obj ): Boolean {
    return when (obj) {
        TRUE -> true
        FALSE -> false
        NULL -> false
        else -> true
    }
}

fun evalStatements(statements: List<Statement>): Obj? {
     var result: Obj? = null
     for (statement in statements) {
         result = eval(statement)
     }

    return result
}

fun evalPrefixExpression(operator: String, right: Obj): Obj {
    return when (operator) {
        "!" -> evalBangPrefixOperatorExpression(right)
        "-" -> evalMinusPrefixOperatorExpression(right)
        else -> NULL
    }
}

fun evalInfixExpression(operator: String, left: Obj, right: Obj): Obj {
    return when {
        left.type() == INTEGER_OBJ && right.type() == INTEGER_OBJ -> evalIntegerInfixExpression(operator, left, right)
        operator == "==" -> boolToBoolean(left == right)
        operator == "!=" -> boolToBoolean(left != right)
        else -> NULL
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
        return NULL
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
        else -> NULL
    }
}

fun boolToBoolean(bool: Boolean) = if (bool) TRUE else FALSE