package com.example.decompi.compiler.models

sealed class Node

data class Program(
    val instructions: List<Instruction>,
    val configurations: List<ConfigInstruction>
) : Node()

sealed class Instruction : Node()
data class VarDeclaration(val name: String, val value: Expression?) : Instruction()
data class Assignment(val name: String, val value: Expression) : Instruction()
data class IfInstruction(val condition: Condition, val body: List<Instruction>) : Instruction()
data class WhileInstruction(val condition: Condition, val body: List<Instruction>) : Instruction()
data class ShowInstruction(val content: String) : Instruction()
data class ReadInstruction(val variable: String) : Instruction()

sealed class Expression : Node()
data class NumberLiteral(val value: Double) : Expression()
data class Identifier(val name: String) : Expression()
data class BinaryExpression(val left: Expression, val operator: String, val right: Expression) : Expression()

sealed class Condition : Node()
data class RelationalCondition(val left: Expression, val operator: String, val right: Expression) : Condition()
data class LogicalCondition(val left: Condition, val operator: String, val right: Condition) : Condition()
data class NotCondition(val condition: Condition) : Condition()

sealed class ConfigInstruction : Node()
data class DefaultConfig(val index: Int) : ConfigInstruction()
data class ColorConfig(val type: String, val value: ColorValue, val index: Int) : ConfigInstruction()
data class FigureConfig(val type: String, val figure: String, val index: Int) : ConfigInstruction()
data class FontConfig(val type: String, val font: String, val index: Int) : ConfigInstruction()
data class FontSizeConfig(val type: String, val size: Expression, val index: Int) : ConfigInstruction()

sealed class ColorValue
data class RgbColor(val r: Int, val g: Int, val b: Int) : ColorValue()
data class HexColor(val hex: String) : ColorValue()
