package lexer

import main.coral.lexer.Lexer
import main.coral.token.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class LexerTest {
    @Test
    fun testTokensBasic() {
        val lexer = Lexer("+=(){},;")
        lexer.reset()

        while (true) {
            val token = lexer.nextToken()
            if (token.type == TokenType.EOF) break
        }

        println(lexer.char)

        assertEquals(mutableListOf(
            Token(TokenType.PLUS, "+"),
            Token(TokenType.ASSIGN, "="),
            Token(TokenType.LPAREN, "("),
            Token(TokenType.RPAREN, ")"),
            Token(TokenType.LBRACE, "{"),
            Token(TokenType.RBRACE, "}"),
            Token(TokenType.COMMA, ","),
            Token(TokenType.SEMICOLON, ";"),
            Token(TokenType.EOF, 0.toChar().toString()),
        ), lexer.tokens)
    }

    @Test
    fun testTokenLengthBasic() {
        val lexer = Lexer("+=(){},;")

        lexer.reset()

        while (true) {
            val token = lexer.nextToken()
            if (token.type == TokenType.EOF) break
        }

        assertEquals(9, lexer.tokens.size)
    }

    @Test
    fun testVariables() {
        val lexer = Lexer("+=(){},;")

        lexer.reset()

        while (true) {
            val token = lexer.nextToken()
            if (token.type == TokenType.EOF) break
        }

        assertEquals(lexer.input.length + 1, lexer.position)
        assertEquals(lexer.input.length + 2, lexer.readPosition)
    }

    @Test
    fun testFullLexing() {
        val lexer = Lexer(
            """
                let gravity = 9829;
                let number = 5 + 5;
                let direction;
                if (gravity == 10000) {
                    direction = 'N';
                } else {
                    direction = 'E';
                    return;
                }
            """.trimIndent()
        )
        lexer.reset()

        while (true) {
            val token = lexer.nextToken()
            if (token.type == TokenType.EOF) break
        }

        println(lexer.tokens)
    }
}