package com.example.vetcare_grupo11.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

fun programarRecordatorioCita(
    context: Context,
    citaId: String,
    pacienteNombre: String,
    motivo: String,
    fechaHoraMillis: Long,
    esVacuna: Boolean,
    minutosAntes: Int = 30,
    testNow: Boolean = false
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, RecordatorioCitaReceiver::class.java).apply {
        putExtra("paciente_nombre", pacienteNombre)
        putExtra("motivo", motivo)
        putExtra("cita_id", citaId)
        putExtra("es_vacuna", esVacuna)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        citaId.hashCode(),  // un requestCode único por cita
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAtMillis = if (testNow) System.currentTimeMillis() else fechaHoraMillis - minutosAntes * 60_000L

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Si no se pueden programar alarmas exactas, se recurre a una alarma inexacta.
            // Esto es mejor que no tener notificación, aunque puede que no llegue en el momento preciso.
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
