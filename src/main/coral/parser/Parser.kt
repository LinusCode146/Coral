package main.coral.parser

import main.coral.ast.*
import main.coral.lexer.Lexer
import main.coral.token.Token
import main.coral.token.TokenType

//Registration function types
fun interface PrefixParseFn {
    fun parse(): Expression?
}
fun interface InfixParseFn {
    fun parse(): Expression?
}

// Precedence helpers
enum class PCD(val pcd: Int){
    LOWEST(0),
    EQUALS(1),
    LESSGREATER(2),
    SUM(3),
    PRODUCT(4),
    PREFIX(5),
    CALL(6),
    INDEX(7),
}
val precedences = mapOf(
    TokenType.EQ to PCD.EQUALS.pcd,
    TokenType.NEQ to PCD.EQUALS.pcd,
    TokenType.LT to PCD.LESSGREATER.pcd,
    TokenType.GT to PCD.LESSGREATER.pcd,
    TokenType.PLUS to PCD.SUM.pcd,
    TokenType.MINUS to PCD.SUM.pcd,
    TokenType.MUL to PCD.PRODUCT.pcd,
    TokenType.DIV to PCD.PRODUCT.pcd,
    TokenType.LPAREN to PCD.CALL.pcd,
    TokenType.LBRACKET to PCD.INDEX.pcd,
)


class Parser (
    private val lexer: Lexer,
    private var curToken: Token,
    private var peekToken: Token,
    private val errors: MutableList<String> = mutableListOf(),
    private val prefixParseFns: MutableMap<TokenType, PrefixParseFn> = mutableMapOf(),
    private val infixParseFns: MutableMap<TokenType, InfixParseFn> = mutableMapOf(),
    private var currentLeft: Expression? = null
) {

    init {
        registerPrefix(TokenType.IDENT) { parseIdentifier() }
        registerPrefix(TokenType.INT) { parseIntegerLiteral() }
        registerPrefix(TokenType.MINUS) { parsePrefixExpression() }
        registerPrefix(TokenType.BANG) { parsePrefixExpression() }
        registerPrefix(TokenType.TRUE) { parseBool() }
        registerPrefix(TokenType.FALSE) { parseBool() }
        registerPrefix(TokenType.LPAREN) { parseGroupedExpressions() }
        registerPrefix(TokenType.IF) { parseIfExpression() }
        registerPrefix(TokenType.FUNCTION) { parseFunctionLiteral() }
        registerPrefix(TokenType.STRING) { parseStringLiteral() }
        registerPrefix(TokenType.LBRACKET) { parseArrayLiteral() }

        registerInfix(TokenType.EQ) { parseInfixExpression() }
        registerInfix(TokenType.NEQ) { parseInfixExpression() }
        registerInfix(TokenType.LT) { parseInfixExpression() }
        registerInfix(TokenType.GT) { parseInfixExpression() }
        registerInfix(TokenType.PLUS) { parseInfixExpression() }
        registerInfix(TokenType.MINUS) { parseInfixExpression() }
        registerInfix(TokenType.MUL) { parseInfixExpression() }
        registerInfix(TokenType.DIV) { parseInfixExpression() }
        registerInfix(TokenType.LPAREN) { parseCallExpressionFromLeft() }
        registerInfix(TokenType.LBRACKET) { parseIndexExpression() }
    }

    //Main function
    fun parseProgram(): Program {
        val program = Program()

        while (curToken.type != TokenType.EOF) {
            val stmt = parseStatement()
            if (stmt is Statement) {
                program.statements.add(stmt)
            }
            nextToken()
        }

        return program
    }

    // Basic helper/error & control flow functions
    private fun nextToken() {
        curToken = peekToken
        peekToken = lexer.nextToken()
    }
    fun errors(): MutableList<String> = errors
    private fun peekError(t: TokenType) = errors.add("Expected next token to be ${t}, but got : ${peekToken.type}")
    private fun noPrefixParseFnError(t: TokenType) = errors.add("No PrefixParseFn for $t found.")
    private fun curTokenIs(t: TokenType): Boolean = curToken.type == t
    private fun peekTokenIs(t: TokenType): Boolean = peekToken.type == t
    private fun expectPeek(t: TokenType): Boolean {
        if (peekTokenIs(t)) {
            nextToken()
            return true
        }else {
            peekError(t)
            return false
        }
    }
    private fun peekPrecedence() = precedences.getOrElse(peekToken.type) { PCD.LOWEST.pcd }
    private fun curPrecedence() = precedences.getOrElse(curToken.type) { PCD.LOWEST.pcd }

    // Expression parsing methods
    private fun parseExpression(precedence: Int): Expression? {
        val prefix = prefixParseFns[curToken.type]
        if (prefix == null) {
            noPrefixParseFnError(curToken.type)
            return null
        }

        var leftExpression = prefix.parse()

        while (!peekTokenIs(TokenType.SEMICOLON) && precedence < peekPrecedence()) {
            val left = leftExpression ?: break

            val infix = infixParseFns[peekToken.type] ?: break

            // Let the parsing function capture `left`
            leftExpression = withLeft(left) {
                nextToken()
                infix.parse()
            }
        }

        return leftExpression
    }
    private fun parseGroupedExpressions(): Expression? {
        nextToken()

        val expression = parseExpression(PCD.LOWEST.pcd) ?: return null

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        return expression
    }
    private fun parseIfExpression(): Expression? {
        val expression = IfExpression(curToken)

        if(!expectPeek(TokenType.LPAREN)) {
            return null
        }

        nextToken()
        expression.condition = parseExpression(PCD.LOWEST.pcd) ?: return null

        if (!expectPeek(TokenType.RPAREN)) {
            return null
        }

        if(!expectPeek(TokenType.LBRACE)) {
            return null
        }

        expression.consequence = parseBlockStatement()

        if(peekTokenIs(TokenType.ELSE)) {
            nextToken()
            if(!expectPeek(TokenType.LBRACE)) {
                return null
            }
            expression.alternative = parseBlockStatement()
        }

        return expression
    }
    private fun parsePrefixExpression(): Expression? {
        val expression = PrefixExpression(curToken, curToken.literal)

        nextToken()
        expression.right = parseExpression(PCD.PREFIX.pcd) ?: return null
        return expression
    }
    private fun parseInfixExpression(): Expression? {
        val left = currentLeft ?: return null

        val expression = InfixExpression(curToken, curToken.literal, left)

        val precedence = curPrecedence()
        nextToken()
        expression.right = parseExpression(precedence) ?: return null
        return expression
    }
    private fun parseCallExpression(function: Expression): Expression? {
        val expression = CallExpression(curToken)
        expression.function = function
        expression.arguments = parseExpressionList(TokenType.RPAREN) ?: return null
        return expression
    }
    private fun parseCallExpressionFromLeft(): Expression? {
        val function = currentLeft ?: return null
        return parseCallExpression(function)
    }
    private fun parseExpressionList(end: TokenType): List<Expression>? {
        val args = mutableListOf<Expression>()

        if (peekTokenIs(end)) {
            nextToken()
            return args
        }

        nextToken()
        args.add(parseExpression(PCD.LOWEST.pcd) ?: return null)

        while (peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            args.add(parseExpression(PCD.LOWEST.pcd) ?: return null)
        }

        if (!expectPeek(end)) {
            return null
        }

        return args
    }

    private fun parseIndexExpression(): Expression? {
        val left = currentLeft ?: return null

        val exp = IndexExpression(curToken)
        exp.left = left

        nextToken()
        exp.index = parseExpression(PCD.LOWEST.pcd) ?: return null

        if (!expectPeek(TokenType.RBRACKET)) {
            return null
        }

        return exp
    }

    // Statement parsing methods
    private fun parseStatement(): Statement? {
        return when  (curToken.type) {
            TokenType.LET -> parseLetStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> parseExpressionStatement()
        }
    }
    private fun parseBlockStatement(): BlockStatement {
        val block = BlockStatement(curToken)
        nextToken()

        while (!curTokenIs(TokenType.RBRACE) && !curTokenIs(TokenType.EOF)) {
            val stmt = parseStatement()
            if (stmt is Statement) {
                block.statements.add(stmt)
            }
            nextToken()
        }
        return block
    }
    private fun parseExpressionStatement(): ExpressionStatement? {
        val stmt = ExpressionStatement(curToken)

        stmt.expression = parseExpression(PCD.LOWEST.pcd) ?: return null

        if(peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }
        return stmt
    }
    private fun parseLetStatement(): Statement? {
        val stmt = LetStatement(curToken)

        if (!expectPeek(TokenType.IDENT)) {
            return null
        }

        stmt.name = Identifier(curToken, curToken.literal)

        if (!expectPeek(TokenType.ASSIGN)) {
            return null
        }
        nextToken()

        stmt.value = parseExpression(PCD.LOWEST.pcd) ?: return null

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return stmt
    }
    private fun parseReturnStatement(): Statement? {
        val stmt = ReturnStatement(curToken)

        nextToken()

        stmt.returnValue = parseExpression(PCD.LOWEST.pcd)  ?: return null

        if (peekTokenIs(TokenType.SEMICOLON)) {
            nextToken()
        }

        return stmt
    }
    private fun parseBool(): Expression = Bool(curToken, curTokenIs(TokenType.TRUE))
    private fun parseIdentifier(): Identifier = Identifier(curToken, curToken.literal)
    private fun parseFunctionLiteral(): Expression? {
        val lit = FunctionLiteral(curToken)

        if (!expectPeek(TokenType.LPAREN)) {
            return null
        }

        lit.parameters = parseFunctionParameters() ?: return null

        if(!expectPeek(TokenType.LBRACE)) {
            return null
        }

        lit.body = parseBlockStatement()
        return lit
    }
    private fun parseIntegerLiteral(): Expression? {
        val value = curToken.literal.toLongOrNull()
        if (value == null) {
            val msg = "could not parse \"${curToken.literal}\" as integer"
            errors.add(msg)
            return null
        }

        return IntegerLiteral(curToken, value.toInt())
    }
    private fun parseFunctionParameters(): MutableList<Identifier>? {
        val identifiers = mutableListOf<Identifier>()

        if(peekTokenIs(TokenType.RPAREN)) {
            return identifiers
        }

        nextToken()
        identifiers.add(Identifier(curToken, curToken.literal))

        while(peekTokenIs(TokenType.COMMA)) {
            nextToken()
            nextToken()
            identifiers.add(Identifier(curToken, curToken.literal))
        }

        if(!expectPeek(TokenType.RPAREN)) {
            return null
        }

        return identifiers
    }
    private fun parseStringLiteral(): Expression = StringLiteral(curToken, curToken.literal)
    private fun parseArrayLiteral():  Expression? {
        val array = ArrayLiteral(curToken)

        array.elements = parseExpressionList(TokenType.RBRACKET) ?: return null
        return array
    }

    // Registration functions
    private fun registerPrefix(t: TokenType, fn: PrefixParseFn) {
        prefixParseFns[t] = fn
    }
    private fun registerInfix(t: TokenType, fn: InfixParseFn) {
        infixParseFns[t] = fn
    }
    private fun <T> withLeft(left: Expression, block: () -> T): T {
        val prev = this.currentLeft
        this.currentLeft = left
        return try {
            block()
        } finally {
            this.currentLeft = prev
        }
    }
}