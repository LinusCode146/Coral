package main.coral.repl

import main.coral.lexer.Lexer
import main.coral.parser.Parser

import java.util.Scanner

class Repl {
    fun start() {
        val scanner = Scanner(System.`in`)
        println("Seien Sie gegrüßt verehrter Nutzer dieser wunderbaren neuen Sprache!")
        println("Coral REPL - enter code. Type ':run' to evaluate, or ':exit' to quit.")

        val codeBuffer = StringBuilder()

        while (true) {
            print(">> ")
            val line = scanner.nextLine()

            when (line.trim()) {
                ":exit" -> {
                    println("Goodbye!")
                    break
                }

                ":run" -> {
                    val input = codeBuffer.toString()

                    val lexer = Lexer(input)
                    val parser = Parser(
                        lexer = lexer,
                        curToken = lexer.nextToken(),
                        peekToken = lexer.nextToken()
                    )

                    val program = try {
                        val parsedProgram = parser.parseProgram()

                        parsedProgram
                    } catch (e: Exception) {
                        println("Error during parsing: ${e.message}")
                        null
                    }

                    if (parser.errors().isNotEmpty()) {
                        println("👎 Parsing Errors:")
                        parser.errors().forEach { println(" - $it") }
                    } else {
                        println("✅ Parsed AST Output:")
                        println(program?.String() ?: "(empty)")
                    }

                    codeBuffer.clear()
                }

                else -> {
                    codeBuffer.appendLine(line)
                }
            }
        }
    }
}
