package main.coral.token

data class Token(val type: TokenType, val literal: String)

enum class TokenType(@Suppress("unused") val literal: String) {
    ILLEGAL("ILLEGAL"),
    EOF("EOF"),

    // Identifiers + literals
    IDENT("IDENT"),
    INT("INT"),

    // Operators
    ASSIGN("="),
    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    BANG("!"),
    LT("<"),
    GT(">"),
    EQ("=="),
    NEQ("!="),

    // Delimiters
    COMMA(","),
    SEMICOLON(";"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE(")"),

    // Keywords
    FUNCTION("FUNCTION"),
    LET("LET"),
    TRUE("TRUE"),
    FALSE("FALSE"),
    IF("IF"),
    ELSE("ELSE"),
    RETURN("RETURN");

}

val keywords = mapOf(
    "fn" to TokenType.FUNCTION,
    "let" to TokenType.LET,
    "if" to TokenType.IF,
    "else" to TokenType.ELSE,
    "true" to TokenType.TRUE,
    "false" to TokenType.FALSE,
    "return" to TokenType.RETURN
)


fun lookUpIndent(ident: String): TokenType = keywords.getOrDefault(ident, TokenType.IDENT)


