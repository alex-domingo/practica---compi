package com.example.decompi.compiler.models

data class OperatorOccurrence(
    val operator: String,
    val line: Int,
    val column: Int,
    val occurrence: String
)

data class ControlStructure(
    val type: String,
    val line: Int,
    val condition: String
)

data class CompilationError(
    val lexeme: String,
    val line: Int,
    val column: Int,
    val type: String,
    val description: String
)

data class CompilationResult(
    val program: Program?,
    val errors: List<CompilationError>,
    val operatorReport: List<OperatorOccurrence>,
    val controlReport: List<ControlStructure>
)
