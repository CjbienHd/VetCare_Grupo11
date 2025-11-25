package com.example.vetcare_grupo11.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vetcare_grupo11.R

class RecordatorioCitaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pacienteNombre = intent.getStringExtra("paciente_nombre") ?: "Paciente"
        val motivo = intent.getStringExtra("motivo") ?: "Cita veterinaria"
        val esVacuna = intent.getBooleanExtra("es_vacuna", false)

        val notificationId = System.currentTimeMillis().toInt()

        val title = if (esVacuna) "Recordatorio de Vacunación" else "Recordatorio de Cita"
        val text = if (esVacuna) "Vacuna para $pacienteNombre: $motivo" else "Cita para $pacienteNombre: $motivo"

        val builder = NotificationCompat.Builder(context, "citas_vetcare")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // <-- Icono por defecto de la app
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
}
