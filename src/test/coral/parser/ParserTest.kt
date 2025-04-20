package parser


import main.coral.lexer.Lexer
import main.coral.parser.Parser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ParserTest {
    private fun parseInput(input: String): String {
        val lexer = Lexer(input)
        lexer.reset()
        val parser = Parser(lexer, lexer.nextToken(), lexer.nextToken())
        val program = parser.parseProgram()
        return program.String().trim()
    }

    @Test
    fun testLetStatement() {
        val input = "let x = 5;"
        val output = parseInput(input)
        assertEquals("let x =  5;", output)
    }

    @Test
    fun testReturnStatement() {
        val input = "return 10;"
        val output = parseInput(input)
        assertEquals("return 10;", output)
    }

    @Test
    fun testInfixExpression() {
        val input = "5 + 10;"
        val output = parseInput(input)
        assertEquals("( 5 + 10 )", output)
    }

    @Test
    fun testPrefixExpression() {
        val input = "-5;"
        val output = parseInput(input)
        assertEquals("(-5)", output)
    }

    @Test
    fun testBooleanLiteral() {
        val input = "true;"
        val output = parseInput(input)
        assertEquals("true", output)
    }

    @Test
    fun testGroupedExpression() {
        val input = "(5 + 2) * 3;"
        val output = parseInput(input)
        assertEquals("( ( 5 + 2 ) * 3 )", output)
    }

    @Test
    fun testFunctionLiteral() {
        val input = "fn(x, y) { x + y; }"
        val output = parseInput(input)
        assertEquals("""
            fn
            (
            x
            y
            )
            ( x + y )
        """.trimIndent(), output)
    }

    @Test
    fun testCallExpression() {
        val input = "add(1, 2 * 3, 4 + 5);"
        val output = parseInput(input)
        assertEquals("add(1, ( 2 * 3 ), ( 4 + 5 ))", output)
    }
}