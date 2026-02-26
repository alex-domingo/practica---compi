package com.example.decompi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.decompi.ui.theme.DeCompiTheme
import com.example.decompi.compiler.Compiler
import com.example.decompi.compiler.models.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeCompiTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var inputText by remember { mutableStateOf("") }
    var compilationResult by remember { mutableStateOf<CompilationResult?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val compiler = remember { Compiler() }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                label = { Text("Código de entrada") },
                placeholder = { Text("Escribe tu pseudocódigo aquí...") }
            )
            
            Button(
                onClick = { 
                    compilationResult = compiler.compile(inputText)
                    if (compilationResult?.errors?.isNotEmpty() == true) {
                        selectedTab = 0 
                    } else {
                        selectedTab = 0 
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
            ) {
                Text("Compilar y Generar Diagrama")
            }

            compilationResult?.let { result ->
                if (result.errors.isNotEmpty()) {
                    Text("Errores encontrados:", color = Color.Red, style = MaterialTheme.typography.titleMedium)
                    ErrorTab(result.errors)
                } else {
                    Column {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Diagrama") }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Operadores") }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Estructuras") }
                        }
                        
                        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                            when (selectedTab) {
                                0 -> FlowchartView(result.program)
                                1 -> OperatorReportTab(result.operatorReport)
                                2 -> ControlReportTab(result.controlReport)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorTab(errors: List<CompilationError>) {
    LazyColumn(modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray)) {
        item {
            Row(Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp)) {
                Text("Lexema", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Lín", Modifier.weight(0.3f), fontWeight = FontWeight.Bold)
                Text("Col", Modifier.weight(0.3f), fontWeight = FontWeight.Bold)
                Text("Tipo", Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                Text("Descripción", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            }
        }
        items(errors) { error ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(error.lexeme, Modifier.weight(1f))
                Text(error.line.toString(), Modifier.weight(0.3f))
                Text(error.column.toString(), Modifier.weight(0.3f))
                Text(error.type, Modifier.weight(0.8f))
                Text(error.description, Modifier.weight(1.5f))
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun OperatorReportTab(operators: List<OperatorOccurrence>) {
    LazyColumn(modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray)) {
        item {
            Row(Modifier.fillMaxWidth().background(Color.Cyan.copy(alpha = 0.2f)).padding(8.dp)) {
                Text("Operador", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Línea", Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Columna", Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Ocurrencia", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            }
        }
        items(operators) { op ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(op.operator, Modifier.weight(1f))
                Text(op.line.toString(), Modifier.weight(0.5f))
                Text(op.column.toString(), Modifier.weight(0.5f))
                Text(op.occurrence, Modifier.weight(2f))
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun ControlReportTab(controls: List<ControlStructure>) {
    LazyColumn(modifier = Modifier.fillMaxSize().border(1.dp, Color.LightGray)) {
        item {
            Row(Modifier.fillMaxWidth().background(Color.Yellow.copy(alpha = 0.2f)).padding(8.dp)) {
                Text("Objeto", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Línea", Modifier.weight(0.5f), fontWeight = FontWeight.Bold)
                Text("Condición", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            }
        }
        items(controls) { ctrl ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(ctrl.type, Modifier.weight(1f))
                Text(ctrl.line.toString(), Modifier.weight(0.5f))
                Text(ctrl.condition, Modifier.weight(2f))
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun FlowchartView(program: Program?) {
    if (program == null) return

    val scrollState = rememberScrollState()
    
    Box(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(2000.dp)) {
            var currentY = 50f
            val centerX = size.width / 2f
            val nodeHeight = 100f
            val nodeWidth = 400f
            val spacing = 80f

            drawFlowchartNode(centerX, currentY, "INICIO", "ELIPSE", Color(0xFFC8E6C9), nodeWidth, nodeHeight)
            currentY += nodeHeight + spacing

            program.instructions.forEach { instruction ->
                drawArrow(centerX, currentY - spacing, centerX, currentY)

                when (instruction) {
                    is VarDeclaration -> {
                        drawFlowchartNode(centerX, currentY, "VAR ${instruction.name}", "RECTANGULO", Color(0xFFB3E5FC), nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is Assignment -> {
                        drawFlowchartNode(centerX, currentY, "${instruction.name} = ...", "RECTANGULO", Color(0xFFB3E5FC), nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is ShowInstruction -> {
                        drawFlowchartNode(centerX, currentY, "MOSTRAR: \"${instruction.content}\"", "PARALELOGRAMO", Color(0xFFF5F5F5), nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is ReadInstruction -> {
                        drawFlowchartNode(centerX, currentY, "LEER: ${instruction.variable}", "PARALELOGRAMO", Color(0xFFF5F5F5), nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is IfInstruction -> {
                        drawFlowchartNode(centerX, currentY, "SI (?)", "ROMBO", Color(0xFFFFF9C4), nodeWidth, nodeHeight + 50f)
                        currentY += nodeHeight + 50f + spacing
                        instruction.body.forEach { _ ->
                            drawArrow(centerX, currentY - spacing, centerX, currentY)
                            drawFlowchartNode(centerX, currentY, "Instr. Interna", "RECTANGULO", Color(0xFFE1F5FE), nodeWidth, nodeHeight)
                            currentY += nodeHeight + spacing
                        }
                    }
                    is WhileInstruction -> {
                        drawFlowchartNode(centerX, currentY, "MIENTRAS (?)", "ROMBO", Color(0xFFFFF9C4), nodeWidth, nodeHeight + 50f)
                        currentY += nodeHeight + 50f + spacing
                        instruction.body.forEach { _ ->
                            drawArrow(centerX, currentY - spacing, centerX, currentY)
                            drawFlowchartNode(centerX, currentY, "Instr. Interna", "RECTANGULO", Color(0xFFE1F5FE), nodeWidth, nodeHeight)
                            currentY += nodeHeight + spacing
                        }
                    }
                }
            }

            drawArrow(centerX, currentY - spacing, centerX, currentY)
            drawFlowchartNode(centerX, currentY, "FIN", "ELIPSE", Color(0xFFFFCDD2), nodeWidth, nodeHeight)
        }
    }
}

fun DrawScope.drawArrow(startX: Float, startY: Float, endX: Float, endY: Float) {
    drawLine(
        color = Color.Black,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 3f
    )
    val arrowSize = 15f
    val path = Path().apply {
        moveTo(endX, endY)
        lineTo(endX - arrowSize, endY - arrowSize)
        lineTo(endX + arrowSize, endY - arrowSize)
        close()
    }
    drawPath(path, Color.Black)
}

fun DrawScope.drawFlowchartNode(x: Float, y: Float, text: String, shape: String, nodeColor: Color, width: Float, height: Float) {
    val left = x - width / 2
    val top = y
    
    when (shape) {
        "ELIPSE" -> {
            drawOval(color = nodeColor, topLeft = Offset(left, top), size = Size(width, height))
            drawOval(color = Color.Black, topLeft = Offset(left, top), size = Size(width, height), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        }
        "ROMBO" -> {
            val path = Path().apply {
                moveTo(x, top)
                lineTo(x + width / 2, top + height / 2)
                lineTo(x, top + height)
                lineTo(x - width / 2, top + height / 2)
                close()
            }
            drawPath(path, nodeColor)
            drawPath(path, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        }
        "PARALELOGRAMO" -> {
            val offset = 40f
            val path = Path().apply {
                moveTo(left + offset, top)
                lineTo(left + width + offset, top)
                lineTo(left + width - offset, top + height)
                lineTo(left - offset, top + height)
                close()
            }
            drawPath(path, nodeColor)
            drawPath(path, Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        }
        else -> { 
            drawRect(color = nodeColor, topLeft = Offset(left, top), size = Size(width, height))
            drawRect(color = Color.Black, topLeft = Offset(left, top), size = Size(width, height), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
        }
    }

    drawContext.canvas.nativeCanvas.drawText(
        if (text.length > 25) text.take(22) + "..." else text,
        x,
        y + height / 2 + 10f,
        android.graphics.Paint().apply {
            this.textSize = 35f
            this.textAlign = android.graphics.Paint.Align.CENTER
            this.isFakeBoldText = true
            this.color = android.graphics.Color.BLACK
        }
    )
}
