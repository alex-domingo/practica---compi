package com.example.decompi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
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
                label = { Text("Código de entrada") }
            )
            
            Button(
                onClick = { 
                    compilationResult = compiler.compile(inputText)
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Compilar")
            }

            compilationResult?.let { result ->
                val hasErrors = result.errors.isNotEmpty()
                
                if (hasErrors) {
                    ErrorTab(result.errors)
                } else {
                    Column {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Diagrama") }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Operadores") }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Estructuras") }
                        }
                        
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

@Composable
fun ErrorTab(errors: List<CompilationError>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Lexema", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("Línea", Modifier.weight(0.5f), style = MaterialTheme.typography.titleSmall)
                Text("Col", Modifier.weight(0.5f), style = MaterialTheme.typography.titleSmall)
                Text("Tipo", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("Descripción", Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
            }
            HorizontalDivider()
        }
        items(errors) { error ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(error.lexeme, Modifier.weight(1f))
                Text(error.line.toString(), Modifier.weight(0.5f))
                Text(error.column.toString(), Modifier.weight(0.5f))
                Text(error.type, Modifier.weight(1f))
                Text(error.description, Modifier.weight(2f))
            }
        }
    }
}

@Composable
fun OperatorReportTab(operators: List<OperatorOccurrence>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Operador", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("Línea", Modifier.weight(0.5f), style = MaterialTheme.typography.titleSmall)
                Text("Columna", Modifier.weight(0.5f), style = MaterialTheme.typography.titleSmall)
                Text("Ocurrencia", Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
            }
            HorizontalDivider()
        }
        items(operators) { op ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(op.operator, Modifier.weight(1f))
                Text(op.line.toString(), Modifier.weight(0.5f))
                Text(op.column.toString(), Modifier.weight(0.5f))
                Text(op.occurrence, Modifier.weight(2f))
            }
        }
    }
}

@Composable
fun ControlReportTab(controls: List<ControlStructure>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text("Objeto", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("Línea", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                Text("Condición", Modifier.weight(2f), style = MaterialTheme.typography.titleSmall)
            }
            HorizontalDivider()
        }
        items(controls) { ctrl ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                Text(ctrl.type, Modifier.weight(1f))
                Text(ctrl.line.toString(), Modifier.weight(1f))
                Text(ctrl.condition, Modifier.weight(2f))
            }
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
        Canvas(modifier = Modifier.fillMaxWidth().height(1000.dp)) {
            var currentY = 50f
            val centerX = size.width / 2f
            val nodeHeight = 100f
            val nodeWidth = 300f
            val spacing = 60f

            // Inicio
            drawNode(centerX, currentY, "INICIO", Color.Green, nodeWidth, nodeHeight)
            currentY += nodeHeight + spacing

            program.instructions.forEach { instruction ->
                when (instruction) {
                    is VarDeclaration -> {
                        drawNode(centerX, currentY, "VAR ${instruction.name}", Color.Cyan, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is Assignment -> {
                        drawNode(centerX, currentY, "${instruction.name} = ...", Color.Cyan, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is ShowInstruction -> {
                        drawNode(centerX, currentY, "MOSTRAR: ${instruction.content}", Color.LightGray, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is ReadInstruction -> {
                        drawNode(centerX, currentY, "LEER: ${instruction.variable}", Color.LightGray, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is IfInstruction -> {
                        drawNode(centerX, currentY, "SI (?)", Color.Yellow, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                    is WhileInstruction -> {
                        drawNode(centerX, currentY, "MIENTRAS (?)", Color.Yellow, nodeWidth, nodeHeight)
                        currentY += nodeHeight + spacing
                    }
                }
                // Arrow
                drawLine(
                    color = Color.Black,
                    start = Offset(centerX, currentY - spacing),
                    end = Offset(centerX, currentY - spacing + 20f),
                    strokeWidth = 5f
                )
            }

            // Fin
            drawNode(centerX, currentY, "FIN", Color.Red, nodeWidth, nodeHeight)
        }
    }
}

fun DrawScope.drawNode(x: Float, y: Float, text: String, color: Color, width: Float, height: Float) {
    drawRect(
        color = color,
        topLeft = Offset(x - width / 2, y),
        size = Size(width, height)
    )
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y + height / 2 + 10f,
        android.graphics.Paint().apply {
            textSize = 40f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
    )
}
