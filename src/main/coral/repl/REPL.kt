package main.coral.repl

import main.coral.evaluator.eval
import main.coral.lexer.Lexer
import main.coral.`object`.Environment
import main.coral.parser.Parser
import java.io.File
import java.util.Scanner

class Repl {

    fun startFile() {
        val input = try {
            File("./script.coral").readText()
        } catch (e: Exception) {
            println("Could not read file ': ${e.message}")
            return
        }

        val lexer = Lexer(input)
        val parser = Parser(
            lexer = lexer,
            curToken = lexer.nextToken(),
            peekToken = lexer.nextToken()
        )

        val program = try {
            parser.parseProgram()
        } catch (e: Exception) {
            println("Error during parsing: ${e.message}")
            return
        }

        if (parser.errors().isNotEmpty()) {
            println("Parsing Errors:")
            parser.errors().forEach { println(" - $it") }
            return
        }

        val environment = Environment()
        val result = eval(program, environment)
        println(result.inspect())
    }

    fun start() {
        val scanner = Scanner(System.`in`)
        println("Welcome, user of this fairly wonderful language!")
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
                ":redo" -> {
                    codeBuffer.clear()
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
                        parser.parseProgram()
                    } catch (e: Exception) {
                        println("Error during parsing: ${e.message}")
                        null
                    }

                    if (parser.errors().isNotEmpty()) {
                        println("👎 Parsing Errors:")
                        parser.errors().forEach { println(" - $it") }
                    } else {
                        val environment = Environment()
                        val evaluated = eval(program!!, environment)
                        println(evaluated.inspect())
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