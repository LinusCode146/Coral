package main.coral.ast

import main.coral.token.Token

//Main Type Structure
interface Node {
    fun tokenLiteral(): String
    fun String(): String
    override fun toString(): String
}

interface Statement: Node {
    fun statementNode()
}
interface Expression: Node {
    fun expressionNode()
}

class Program (
    val statements: MutableList<Statement>
) : Node {
    override fun tokenLiteral(): String {
        return if (statements.isNotEmpty()) {
            statements[0].tokenLiteral()
        } else {
            ""
        }
    }

    override fun String(): String {
        val codeBuffer = StringBuilder()
        for (statement in statements) {
            codeBuffer.appendLine(statement.String())
        }

        return codeBuffer.toString()
    }
    override fun toString(): String = String()
}

class ExpressionStatement(
    private val token: Token,
): Statement {

    lateinit var expression: Expression
    override fun tokenLiteral(): String = token.literal
    override fun statementNode() {}
    override fun String(): String = expression.String()
    override fun toString(): String = String()
}

class PrefixExpression(
    private val token: Token,
    private val operator: String,
): Expression {
    lateinit var right: Expression

    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String = "($operator${right.String()})"
    override fun toString(): String = String()
}

class InfixExpression(
    private val token: Token,
    private val operator: String,
    private val left: Expression
): Expression {
    lateinit var right: Expression
    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String = "( ${left.String()} $operator ${right.String()} )"
    override fun toString(): String = String()
}

class IfExpression(
    private val token: Token,
): Expression {
    lateinit var condition: Expression
    lateinit var consequence: BlockStatement
    lateinit var alternative:  BlockStatement
    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String {
        return if(::alternative.isInitialized) {
            "if ${condition.String()} ${consequence.String()}; else ${alternative.String()};"

        }else{
            "if ${condition.String()} ${consequence.String()};"
        }
    }
    override fun toString(): String = String()
}

class CallExpression(
    private val token: Token,
): Expression {
    lateinit var function: Expression
    lateinit var arguments: List<Expression>

    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String {
        return "${function.String()}(${arguments.joinToString()})"
    }
    override fun toString(): String = String()
}

class IntegerLiteral (
    private val token: Token,
    @Suppress("unused") private val value: Int
): Expression {
    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String = token.literal
    override fun toString(): String = String()
}

class Identifier(private val token: Token, val value: String): Expression {
    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String = value
    override fun toString(): String = String()
}

class Bool (private val token: Token, @Suppress("unused") private val value: Boolean): Expression {
    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String = token.literal
    override fun toString(): String = String()
}

class FunctionLiteral (
    private val token: Token,
): Expression {
    lateinit var body: BlockStatement
    lateinit var parameters: MutableList<Identifier>

    override fun tokenLiteral(): String = token.literal
    override fun expressionNode() {}
    override fun String(): String {
        val codeBuffer = StringBuilder()
        codeBuffer.appendLine("fn")
        codeBuffer.appendLine("(")
        for (parameter in parameters) {
            codeBuffer.appendLine(parameter.toString())
        }
        codeBuffer.appendLine(")")
        codeBuffer.appendLine(body.String())
        return codeBuffer.toString()
    }

    override fun toString(): String = String()
}

class LetStatement(
    private val token: Token
) : Statement {

    lateinit var name: Identifier
    lateinit var value: Expression

    override fun tokenLiteral(): String = token.literal
    override fun statementNode() {}
    override fun String(): String = "${tokenLiteral()} ${name.value} =  $value;"
    override fun toString(): String = String()

}

class ReturnStatement(
    private val token: Token,
): Statement {
    lateinit var returnValue: Expression

    override fun tokenLiteral(): String = token.literal
    override fun statementNode() {}
    override fun String(): String = "${tokenLiteral()} ${returnValue.String()};"
    override fun toString(): String = String()

}

class BlockStatement(
    private val token: Token,
    val statements: MutableList<Statement> = mutableListOf(),
): Statement {
    override fun tokenLiteral(): String = token.literal
    override fun statementNode() {}
    override fun String(): String {
        val codeBuffer = StringBuilder()
        for (statement in statements) {
            codeBuffer.appendLine(statement.String())
        }
        return codeBuffer.toString()
    }
    override fun toString(): String = String()

}