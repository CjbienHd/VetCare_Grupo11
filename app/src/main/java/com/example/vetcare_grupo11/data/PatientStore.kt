package com.example.vetcare_grupo11.data


import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.example.vetcare_grupo11.viewmodel.Patient

/** Contrato de persistencia */
interface PatientsStore {
    fun load(): List<Patient>
    fun save(list: List<Patient>)
}

/** Implementación con SharedPreferences (mismo espíritu que RegistroScreen) */
class SharedPrefsPatientsStore(context: Context) : PatientsStore {
    private val sp = context.getSharedPreferences("datos_app", Context.MODE_PRIVATE)
    private val KEY = "patients_json"

    override fun load(): List<Patient> {
        val raw = sp.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        Patient(
                            id = o.getString("id"),
                            nombre = o.getString("nombre"),
                            especie = o.getString("especie"),
                            raza = o.getString("raza"),
                            tutor = o.getString("tutor")
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override fun save(list: List<Patient>) {
        val arr = JSONArray()
        list.forEach { p ->
            val o = JSONObject()
            o.put("id", p.id)
            o.put("nombre", p.nombre)
            o.put("especie", p.especie)
            o.put("raza", p.raza)
            o.put("tutor", p.tutor)
            arr.put(o)
        }
        sp.edit().putString(KEY, arr.toString()).apply()
    }
}
