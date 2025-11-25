package com.example.vetcare_grupo11.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SeccionRegistroClinicoVisual(
    modifier: Modifier = Modifier
) {
    var fotoTomada by remember { mutableStateOf<Bitmap?>(null) }
    var imagenGaleria by remember { mutableStateOf<Uri?>(null) }

    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            fotoTomada = bitmap
        }
    }

    val lanzadorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imagenGaleria = uri
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(text = "Registro clínico visual")

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { lanzadorCamara.launch(null) }) {
                    Text("Tomar foto clínica")
                }

                Button(onClick = { lanzadorGaleria.launch("image/*") }) {
                    Text("Adjuntar desde galería")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                fotoTomada != null -> {
                    Text(text = "Última foto tomada:")
                    Image(
                        bitmap = fotoTomada!!.asImageBitmap(),
                        contentDescription = "Foto clínica",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                imagenGaleria != null -> {
                    Text(text = "Última imagen adjuntada:")
                    AsyncImage(
                        model = imagenGaleria,
                        contentDescription = "Imagen clínica",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Text(
                        text = "Aún no se ha registrado ninguna imagen clínica para este paciente."
                    )
                }
            }
        }
    }
}