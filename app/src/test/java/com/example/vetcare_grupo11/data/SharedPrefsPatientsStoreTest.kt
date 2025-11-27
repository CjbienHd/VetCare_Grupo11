package com.example.vetcare_grupo11.data

import android.content.Context
import android.content.SharedPreferences
import com.example.vetcare_grupo11.viewmodel.Patient
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SharedPrefsPatientsStoreTest {

    companion object {
        private const val PREFS_NAME = "datos_app"
        private const val KEY = "patients_json"
    }

    @MockK(relaxed = true)
    lateinit var context: Context

    @MockK(relaxed = true)
    lateinit var sharedPrefs: SharedPreferences

    @MockK(relaxed = true)
    lateinit var editor: SharedPreferences.Editor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Context.getSharedPreferences(...) debe devolver nuestro mock de SharedPreferences
        every { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // ---------------------------------
    // load() cuando no hay nada guardado
    // ---------------------------------

    @Test
    fun `load devuelve lista vacia si no hay valor en SharedPreferences`() {
        every { sharedPrefs.getString(KEY, "") } returns ""

        val store = SharedPrefsPatientsStore(context)

        val result = store.load()

        Assertions.assertTrue(result.isEmpty())
    }

    // ---------------------------------
    // load() con JSON valido
    // ---------------------------------

    @Test
    fun `load reconstruye la lista de pacientes cuando el JSON es valido`() {
        // Construimos un JSON igual a lo que guarda save()
        val arr = JSONArray().apply {
            put(
                JSONObject().apply {
                    put("id", "1")
                    put("nombre", "Luna")
                    put("especie", "Perro")
                    put("raza", "Boxer")
                    put("tutor", "Juan")
                }
            )
            put(
                JSONObject().apply {
                    put("id", "2")
                    put("nombre", "Michi")
                    put("especie", "Gato")
                    put("raza", "Siames")
                    put("tutor", "Ana")
                }
            )
        }

        every { sharedPrefs.getString(KEY, "") } returns arr.toString()

        val store = SharedPrefsPatientsStore(context)

        val result = store.load()

        Assertions.assertEquals(2, result.size)

        Assertions.assertEquals("1", result[0].id)
        Assertions.assertEquals("Luna", result[0].nombre)
        Assertions.assertEquals("Perro", result[0].especie)
        Assertions.assertEquals("Boxer", result[0].raza)
        Assertions.assertEquals("Juan", result[0].tutor)

        Assertions.assertEquals("2", result[1].id)
        Assertions.assertEquals("Michi", result[1].nombre)
        Assertions.assertEquals("Gato", result[1].especie)
        Assertions.assertEquals("Siames", result[1].raza)
        Assertions.assertEquals("Ana", result[1].tutor)
    }

    // ---------------------------------
    // load() con JSON corrupto
    // ---------------------------------

    @Test
    fun `load devuelve lista vacia cuando el JSON esta corrupto`() {
        every { sharedPrefs.getString(KEY, "") } returns "{ esto no es json valido ]"

        val store = SharedPrefsPatientsStore(context)

        val result = store.load()

        Assertions.assertTrue(result.isEmpty()) // por el catch(Throwable) del load()
    }

    // ---------------------------------
    // save() genera el JSON esperado
    // ---------------------------------

    @Test
    fun `save serializa la lista de pacientes a JSON y la guarda en SharedPreferences`() {
        // Capturamos el string que se pasa a putString(KEY, json)
        val savedJsonSlot = slot<String>()

        every { editor.putString(KEY, capture(savedJsonSlot)) } returns editor

        val store = SharedPrefsPatientsStore(context)

        val list = listOf(
            Patient(
                id = "1",
                nombre = "Luna",
                especie = "Perro",
                raza = "Boxer",
                tutor = "Juan"
            ),
            Patient(
                id = "2",
                nombre = "Michi",
                especie = "Gato",
                raza = null,
                tutor = "Ana"
            )
        )

        store.save(list)

        // Comprobamos el JSON que se guardó
        val json = savedJsonSlot.captured
        val arr = JSONArray(json)

        Assertions.assertEquals(2, arr.length())

        val o1 = arr.getJSONObject(0)
        Assertions.assertEquals("1", o1.getString("id"))
        Assertions.assertEquals("Luna", o1.getString("nombre"))
        Assertions.assertEquals("Perro", o1.getString("especie"))
        Assertions.assertEquals("Boxer", o1.getString("raza"))
        Assertions.assertEquals("Juan", o1.getString("tutor"))

        val o2 = arr.getJSONObject(1)
        Assertions.assertEquals("2", o2.getString("id"))
        Assertions.assertEquals("Michi", o2.getString("nombre"))
        Assertions.assertEquals("Gato", o2.getString("especie"))
        // raza puede ser null, aquí solo verificamos que exista la clave (o el valor "null")
        Assertions.assertTrue(o2.has("raza"))
        Assertions.assertEquals("Ana", o2.getString("tutor"))
    }
}