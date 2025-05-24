package main.coral.lexer

import main.coral.token.*


class Lexer(val input: String) {
    var position: Int = 0
    var readPosition: Int = 0
    var char: Char = 0.toChar()
    val tokens = mutableListOf<Token>()

    init {
        readChar()
    }

    fun reset() {
        position = 0
        readPosition = 0
        tokens.clear()
        readChar()
    }

    private fun readChar() {
        char = if (readPosition >= input.length) 0.toChar() else input[readPosition]
        position = readPosition
        readPosition++
    }

    private fun peekChar(): Char {
        return if (readPosition >= input.length) {
            0.toChar()
        } else {
            input[readPosition]
        }
    }

    fun nextToken(): Token {
        skipWhitespace()

        val token = when (char) {
            '"' -> {
                val literal = readString()
                newToken(TokenType.STRING, literal)
            }
            '=' -> {
                if(peekChar() == '=') {
                    readChar()
                    newToken(TokenType.EQ, "==")
                } else {
                    newToken(TokenType.ASSIGN, char)
                }
            }
            ';' -> newToken(TokenType.SEMICOLON, char)
            '.' -> newToken(TokenType.DOT, char)
            '(' -> newToken(TokenType.LPAREN, char)
            ')' -> newToken(TokenType.RPAREN, char)
            ',' -> newToken(TokenType.COMMA, char)
            '+' -> newToken(TokenType.PLUS, char)
            '{' -> newToken(TokenType.LBRACE, char)
            '}' -> newToken(TokenType.RBRACE, char)
            '-' -> newToken(TokenType.MINUS, char)
            '*' -> newToken(TokenType.MUL, char)
            '/' -> newToken(TokenType.DIV, char)
            '<' -> newToken(TokenType.LT, char)
            '>' -> newToken(TokenType.GT, char)
            '[' -> newToken(TokenType.LBRACKET, char)
            ']' -> newToken(TokenType.RBRACKET, char)
            ':' -> newToken(TokenType.COLON, char)
            '!' -> {
                if(peekChar() == '=') {
                    readChar()
                    newToken(TokenType.NEQ, "!=")
                } else {
                    newToken(TokenType.BANG, char)
                }
            }
            0.toChar() -> newToken(TokenType.EOF, 0.toChar())
            else -> {
                when {
                    char.isLetter() || char == '_' -> {
                        val literal = readIdentifier()
                        return newToken(lookUpIndent(literal), literal)
                    }
                    char.isDigit() -> {
                        val literal = readNumber()
                        return newToken(TokenType.INT, literal)
                    }
                    else -> newToken(TokenType.ILLEGAL, char)
                }
            }
        }

        readChar()
        return token
    }

    private fun newToken(type: TokenType, literal: Char): Token {
        val token = Token(type, literal.toString())
        tokens.add(token)
        return token
    }
    private fun newToken(type: TokenType, literal: String): Token {
        val token = Token(type, literal)
        tokens.add(token)
        return token
    }

    private fun readString(): String {
        val currentPos = position + 1
        while(true) {
            readChar()
            if(char == '"' || char.code == 0) {
                break
            }
        }
        return input.substring(currentPos, position)
    }
    private fun readIdentifier(): String {
        val start = position
        while (char.isLetter() || char == '_') {
            readChar()
        }
        return input.substring(start, position)
    }

    private fun readNumber(): String {
        val start = position
        while (char.isDigit()) {
            readChar()
        }
        return input.substring(start, position)
    }

    private fun skipWhitespace() {
        while (char == ' ' || char == '\t' || char == '\r' || char == '\n') {
            readChar()
        }
    }
}

