package com.example.decompi.compiler

import com.example.decompi.compiler.models.*
import java.io.StringReader

class Compiler {
    fun compile(input: String): CompilationResult {
        val lexer = Lexer(StringReader(input))
        val parser = Parser(lexer)
        
        return try {
            val symbol = parser.parse()
            val program = symbol.value as? Program
            
            // Si el parser tiene errores acumulados pero terminó "bien" (ej. recuperación)
            CompilationResult(
                program = program,
                errors = parser.errors,
                operatorReport = parser.operatorReport,
                controlReport = parser.controlReport
            )
        } catch (e: Exception) {
            // Si el error fue fatal y detuvo el proceso
            val allErrors = parser.errors.toMutableList()
            if (allErrors.isEmpty()) {
                // Si no hay errores en la lista, el error ocurrió antes de que el parser pudiera registrarlo
                allErrors.add(CompilationError("N/A", 0, 0, "Error Fatal", e.message ?: "Error desconocido"))
            }
            
            CompilationResult(
                program = null,
                errors = allErrors,
                operatorReport = parser.operatorReport,
                controlReport = parser.controlReport
            )
        }
    }
}
