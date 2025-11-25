
package com.example.vetcare_grupo11.data

import android.content.Context
import com.example.vetcare_grupo11.viewmodel.Appointment
import org.json.JSONArray
import org.json.JSONObject

/**
 * Interfaz de persistencia para las citas
 */
interface AppointmentsStore {
    fun load(): List<Appointment>
    fun save(list: List<Appointment>)
}

/**
 * Implementación con SharedPreferences para guardar las citas.
 */
class SharedPrefsAppointmentsStore(context: Context) : AppointmentsStore {

    private val sp = context.getSharedPreferences("datos_app", Context.MODE_PRIVATE)
    private val KEY = "appointments_json"

    override fun load(): List<Appointment> {
        val raw = sp.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Appointment(
                            id = o.getString("id"),
                            patientName = o.getString("patientName"),
                            motivo = o.getString("motivo"),
                            fechaHora = o.getString("fechaHora"),
                            fechaHoraMillis = o.getLong("fechaHoraMillis"),
                            notas = o.getString("notas"),
                            estado = o.getString("estado"),
                            esVacuna = o.getBoolean("esVacuna")
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override fun save(list: List<Appointment>) {
        val arr = JSONArray()
        list.forEach { p ->
            val o = JSONObject()
            o.put("id", p.id)
            o.put("patientName", p.patientName)
            o.put("motivo", p.motivo)
            o.put("fechaHora", p.fechaHora)
            o.put("fechaHoraMillis", p.fechaHoraMillis)
            o.put("notas", p.notas)
            o.put("estado", p.estado)
            o.put("esVacuna", p.esVacuna)
            arr.put(o)
        }
        sp.edit().putString(KEY, arr.toString()).apply()
    }
}
