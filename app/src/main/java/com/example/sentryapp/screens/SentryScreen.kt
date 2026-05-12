package com.example.sentryapp.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentryapp.ui.theme.SentryAppTheme

@Composable
fun SentryScreen(
    innerPadding : PaddingValues = PaddingValues(0.dp),
    motion       : Boolean       = false,
    onLedOn      : () -> Unit    = {},
    onLedOff     : () -> Unit    = {},
    onConnect    : () -> Boolean = { false },
    onDisconnect : () -> Unit    = {}
) {
    val colors = MaterialTheme.colorScheme

    var isConnected by remember { mutableStateOf(false) }
    var ledOn       by remember { mutableStateOf(false) }

    // Color del LED animado
    val ledColor by animateColorAsState(
        targetValue = if (ledOn) Color(0xFFE53935) else Color(0xFF9E9E9E),
        animationSpec = tween(300),
        label = "ledColor"
    )

    // Color del estado de movimiento
    val motionColor = if (motion) Color(0xFFE53935) else Color(0xFF43A047)
    val motionText  = if (motion) "MOVIMIENTO DETECTADO" else "Sin movimiento"

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(Color(0xFFFFC1CC)),          // fondo rosado como el mockup
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Título ────────────────────────────────────────────────────────────
        Spacer(Modifier.height(32.dp))
        Text("Sentry",
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF1A237E))
        Text("Detector de movimiento",
            fontSize = 14.sp,
            color    = Color(0xFF1A237E).copy(alpha = 0.6f))

        Spacer(Modifier.height(32.dp))

        // ── Círculo LED ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(ledColor),
            contentAlignment = Alignment.Center
        ) { }

        Text("LED rojo",
            fontSize = 13.sp,
            color    = Color(0xFF1A237E).copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(24.dp))

        // ── Tarjeta de estado de movimiento ───────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Text("Estado",
                    fontSize = 12.sp,
                    color    = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(motionText,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = motionColor)
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Botón LED ─────────────────────────────────────────────────────────
        Button(
            onClick = {
                ledOn = !ledOn
                if (ledOn) onLedOn() else onLedOff()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ledOn) Color(0xFFE53935) else Color(0xFF1A237E)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text       = if (ledOn) "DESACTIVAR" else "Activar alarma",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Estado de conexión + botón ────────────────────────────────────────
        Text(
            text  = if (isConnected) "Conectado" else "Desconectado",
            color = if (isConnected) Color(0xFF43A047) else Color.Gray,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (isConnected) {
                    onDisconnect()
                } else {
                    onConnect()
                }
                isConnected = !isConnected
            },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isConnected) Color.Red else Color(0xFF1A237E)
            )
        ) {
            Text(if (isConnected) "Desconectarse" else "Conectarse")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SentryPreview() {
    SentryAppTheme {
        SentryScreen(motion = true)
    }
}