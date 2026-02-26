package com.example.decompi.compiler

import com.example.decompi.compiler.models.*
import java.io.StringReader

class Compiler {
    fun compile(input: String): CompilationResult {
        return try {
            val lexer = Lexer(StringReader(input))
            val parser = parser(lexer)
            
            val symbol = parser.parse()
            val program = symbol.value as? Program
            
            CompilationResult(
                program = program,
                errors = parser.errors,
                operatorReport = parser.operatorReport,
                controlReport = parser.controlReport
            )
        } catch (e: Exception) {
            CompilationResult(
                program = null,
                errors = listOf(CompilationError("N/A", 0, 0, "Error Fatal", e.message ?: "Error desconocido durante la compilación")),
                operatorReport = emptyList(),
                controlReport = emptyList()
            )
        }
    }
}
