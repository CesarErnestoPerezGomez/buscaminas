package com.example.buscaminas

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscaminas.ui.theme.BuscaminasTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BuscaminasTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Buscaminas") }
                        )
                    }
                ) { innerPadding ->
                    JuegoBuscaminas(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        mensaje = { texto -> mostrarToast(texto) }
                    )
                }
            }
        }
    }

    fun mostrarToast(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun JuegoBuscaminas(
    modifier: Modifier = Modifier,
    mensaje: (String) -> Unit
) {
    val columnas = 7
    val filas = 11
    val metaGanadora = 10

    val botonesTapados = remember {
        List(filas * columnas) { mutableStateOf(true) }
    }

    val minas = remember {
        List(filas * columnas) { mutableStateOf(generarMina()) }
    }

    val contadorSeguros = remember {
        mutableStateOf(0)
    }

    val mostrarPerdiste = remember {
        mutableStateOf(false)
    }

    val mostrarGanaste = remember {
        mutableStateOf(false)
    }

    fun reiniciarJuego() {
        for (i in botonesTapados.indices) {
            botonesTapados[i].value = true
            minas[i].value = generarMina()
        }
        contadorSeguros.value = 0
        mostrarPerdiste.value = false
        mostrarGanaste.value = false
    }

    if (mostrarPerdiste.value) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Perdiste") },
            text = { Text("Seleccionaste una mina.") },
            confirmButton = {
                TextButton(onClick = { reiniciarJuego() }) {
                    Text("Reiniciar")
                }
            }
        )
    }

    if (mostrarGanaste.value) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Ganaste") },
            text = { Text("Destapaste 10 botones seguros seguidos.") },
            confirmButton = {
                TextButton(onClick = { reiniciarJuego() }) {
                    Text("Jugar otra vez")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .background(Color(0xFFF4F6FB))
            .padding(12.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3EAFB)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = "Progreso",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF25345C)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Casillas seguras seguidas: ${contadorSeguros.value} / $metaGanadora",
                    fontSize = 16.sp,
                    color = Color(0xFF374B7A)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { reiniciarJuego() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C6FA8),
                        contentColor = Color.White
                    )
                ) {
                    Text("Reiniciar juego")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        for (fila in 0 until filas) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (columna in 0 until columnas) {
                    val indice = fila * columnas + columna
                    val tapado = botonesTapados[indice].value
                    val esMina = minas[indice].value
                    val minasAlrededor = contarMinasContiguas(indice, columnas, filas, minas)

                    val textoBoton = if (tapado) {
                        "X"
                    } else {
                        if (esMina) {
                            "💣"
                        } else {
                            if (minasAlrededor == 0) "" else minasAlrededor.toString()
                        }
                    }

                    val colorBoton = if (tapado) {
                        Color(0xFF5A6C9E)
                    } else {
                        if (esMina) {
                            Color(0xFFD9534F)
                        } else {
                            Color(0xFFDCE6FF)
                        }
                    }

                    Button(
                        onClick = {
                            botonesTapados[indice].value = false

                            if (esMina) {
                                contadorSeguros.value = 0
                                mostrarPerdiste.value = true
                            } else {
                                contadorSeguros.value++

                                if (minasAlrededor > 0) {
                                    mensaje("Minas cercanas: $minasAlrededor")
                                } else {
                                    mensaje("Casilla libre")
                                }

                                if (contadorSeguros.value == metaGanadora) {
                                    mostrarGanaste.value = true
                                }
                            }
                        },
                        enabled = tapado,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorBoton,
                            contentColor = Color.White,
                            disabledContainerColor = colorBoton,
                            disabledContentColor = if (esMina) Color.White else Color(0xFF1C2A4A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Text(
                            text = textoBoton,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun generarMina(): Boolean {
    val numero = Random.nextInt(10)
    return numero > 7
}

fun contarMinasContiguas(
    indice: Int,
    columnas: Int,
    filas: Int,
    minas: List<MutableState<Boolean>>
): Int {
    val fila = indice / columnas
    val columna = indice % columnas
    var contador = 0

    for (i in fila - 1..fila + 1) {
        for (j in columna - 1..columna + 1) {
            if (i >= 0 && i < filas && j >= 0 && j < columnas) {
                val nuevoIndice = i * columnas + j

                if (nuevoIndice != indice && minas[nuevoIndice].value) {
                    contador++
                }
            }
        }
    }

    return contador
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewBuscaminas() {
    BuscaminasTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Buscaminas") }
                )
            }
        ) { innerPadding ->
            JuegoBuscaminas(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                mensaje = {}
            )
        }
    }
}