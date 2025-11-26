package com.example.vetcare_grupo11.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.vetcare_grupo11.viewmodel.Patient

/**
 * Interfaz de persistencia
 */
interface PatientsStore {
    fun load(): List<Patient>   // carga todos los pacientes guardados
    fun save(list: List<Patient>) // guarda la lista actual
}

/**
 * Implementación concreta usando SharedPreferences.
 */
class SharedPrefsPatientsStore(context: Context) : PatientsStore {

    // Abre o crea el archivo de SharedPreferences "datos_app"
    private val sp = context.getSharedPreferences("datos_app", Context.MODE_PRIVATE)
    private val KEY = "patients_json" // clave bajo la cual guardo los pacientes

    // Función para leer desde SharedPreferences y convertir el JSON a objetos Patient
    override fun load(): List<Patient> {
        val raw = sp.getString(KEY, "") ?: ""  // obtengo el texto guardado
        if (raw.isBlank()) return emptyList()  // si no hay nada, retorno lista vacía
        return try {
            val arr = JSONArray(raw) // parseo el texto como JSONArray
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Patient(
                            id = o.getString("id"),
                            nombre = o.getString("nombre"),
                            especie = o.getString("especie"),
                            raza = o.optString("raza", null),
                            tutor = o.getString("tutor"),
                            fotoUri = o.optString("fotoUri", null)
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            // En caso de error, devuelvo lista vacía
            emptyList()
        }
    }

    // Función para guardar la lista de pacientes como JSON
    override fun save(list: List<Patient>) {
        val arr = JSONArray()
        list.forEach { p ->
            val o = JSONObject()
            o.put("id", p.id)
            o.put("nombre", p.nombre)
            o.put("especie", p.especie)
            o.put("raza", p.raza)
            o.put("tutor", p.tutor)
            o.put("fotoUri", p.fotoUri)
            arr.put(o)
        }
        sp.edit().putString(KEY, arr.toString()).apply()
    }
}
