package com.example.vetcare_grupo11.viewmodel


import android.util.Log
import com.example.vetcare_grupo11.data.PatientsStore
import com.example.vetcare_grupo11.network.PatientApiService
import com.example.vetcare_grupo11.network.RetrofitInstance
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    // API de backend para pacientes
    @MockK(relaxed = true)
    lateinit var patientsApi: PatientApiService

    // Fake store en memoria
    private lateinit var fakeStore: FakePatientsStore

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // RetrofitInstance.api -> nuestro mock
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.api } returns patientsApi

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        // Inicializamos el store vacío por defecto
        fakeStore = FakePatientsStore()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        unmockkStatic(Log::class)
    }


    // loadInitialData (init)


    @Test
    fun `loadInitialData usa backend cuando este devuelve lista no vacia y guarda en store`() = runTest(dispatcher) {
        // Local cache inicial
        fakeStore.internalList = mutableListOf(
            Patient(id = "local1", nombre = "Local", especie = "Gato", tutor = "Juan")
        )

        val backendList = listOf(
            Patient(id = "b1", nombre = "Backend", especie = "Perro", tutor = "Ana")
        )
        coEvery { patientsApi.getPatients() } returns backendList

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        vm.isLoading.value shouldBe false
        vm.patients.value shouldContainExactly backendList
        fakeStore.lastSaved shouldBe backendList
    }

    @Test
    fun `loadInitialData mantiene cache local cuando backend devuelve lista vacia`() = runTest(dispatcher) {
        // Local cache con datos
        val localList = listOf(
            Patient(id = "local1", nombre = "Local", especie = "Gato", tutor = "Juan")
        )
        fakeStore.internalList = localList.toMutableList()

        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        vm.isLoading.value shouldBe false
        vm.patients.value shouldContainExactly localList
        // No debería haber sobreescritura con backend
        fakeStore.lastSaved shouldBe null
    }

    @Test
    fun `loadInitialData mantiene cache local cuando backend lanza excepcion`() = runTest(dispatcher) {
        val localList = listOf(
            Patient(id = "local1", nombre = "Local", especie = "Gato", tutor = "Juan")
        )
        fakeStore.internalList = localList.toMutableList()

        coEvery { patientsApi.getPatients() } throws RuntimeException("Network error")

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        vm.isLoading.value shouldBe false
        vm.patients.value shouldContainExactly localList
        fakeStore.lastSaved shouldBe null
    }


    // addPatient()


    @Test
    fun `addPatient agrega paciente localmente y reemplaza por backend cuando tiene exito`() = runTest(dispatcher) {
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val newPatient = Patient(
            id = null,
            nombre = "Luna",
            especie = "Perro",
            raza = "Mestizo",
            tutor = "Juan"
        )

        // El backend devuelve un paciente (ejemplo, mismo con id fijo)
        val createdFromBackend = newPatient.copy(id = "backend-id")
        coEvery { patientsApi.createPatient(any()) } returns createdFromBackend

        vm.addPatient(newPatient)
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 1
        list[0].id shouldBe "backend-id"
        list[0].nombre shouldBe "Luna"

        // El store debería haber guardado la lista final
        fakeStore.lastSaved shouldBe list
        coVerify(exactly = 1) { patientsApi.createPatient(any()) }
    }

    @Test
    fun `addPatient mantiene paciente en local cuando el backend falla`() = runTest(dispatcher) {
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val newPatient = Patient(
            id = null,
            nombre = "Rocky",
            especie = "Perro",
            raza = "Boxer",
            tutor = "Ana"
        )

        coEvery { patientsApi.createPatient(any()) } throws RuntimeException("Network error")

        vm.addPatient(newPatient)
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 1
        list[0].nombre shouldBe "Rocky"
        list[0].id.isNullOrBlank() shouldBe false

        // Como el backend falló, la última versión guardada en store es la lista local con withId
        fakeStore.lastSaved shouldBe list
        coVerify(exactly = 1) { patientsApi.createPatient(any()) }
    }


    // removePatient()


    @Test
    fun `removePatient elimina paciente cuando backend tiene exito`() = runTest(dispatcher) {
        val p1 = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        val p2 = Patient(id = "2", nombre = "Michi", especie = "Gato", tutor = "Ana")

        fakeStore.internalList = mutableListOf(p1, p2)
        coEvery { patientsApi.getPatients() } returns emptyList() // para init, pero quedará cache local

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        coEvery { patientsApi.deletePatient("1") } returns Unit

        vm.removePatient("1")
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 1
        list[0].id shouldBe "2"

        fakeStore.lastSaved shouldBe list
        coVerify(exactly = 1) { patientsApi.deletePatient("1") }
    }

    @Test
    fun `removePatient no cambia lista cuando backend falla`() = runTest(dispatcher) {
        val p1 = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        val p2 = Patient(id = "2", nombre = "Michi", especie = "Gato", tutor = "Ana")

        fakeStore.internalList = mutableListOf(p1, p2)
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        coEvery { patientsApi.deletePatient("1") } throws RuntimeException("Network error")

        vm.removePatient("1")
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 2
        list[0].id shouldBe "1"
        list[1].id shouldBe "2"

        // No debería haberse guardado una nueva lista distinta en el store
        fakeStore.lastSaved shouldBe null
        coVerify(exactly = 1) { patientsApi.deletePatient("1") }
    }


    // updatePatient()


    @Test
    fun `updatePatient actualiza paciente y mantiene cambio cuando backend tiene exito`() = runTest(dispatcher) {
        val original = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        fakeStore.internalList = mutableListOf(original)
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val updated = original.copy(
            nombre = "Luna Actualizada",
            tutor = "Juan Pérez"
        )

        coEvery { patientsApi.updatePatient("1", updated) } answers { secondArg<Patient>() }


        vm.updatePatient(updated)
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 1
        list[0].nombre shouldBe "Luna Actualizada"
        list[0].tutor shouldBe "Juan Pérez"

        // Último guardado debe ser la lista actualizada
        fakeStore.lastSaved shouldBe list
        coVerify(exactly = 1) { patientsApi.updatePatient("1", updated) }
    }

    @Test
    fun `updatePatient revierte cambios cuando backend falla`() = runTest(dispatcher) {
        val original = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        fakeStore.internalList = mutableListOf(original)
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val updated = original.copy(
            nombre = "Luna Editada",
            tutor = "Juan Editado"
        )

        coEvery { patientsApi.updatePatient("1", updated) } throws RuntimeException("Network error")

        vm.updatePatient(updated)
        advanceUntilIdle()

        val list = vm.patients.value
        list.size shouldBe 1
        list[0].nombre shouldBe "Luna"          // vuelto al original
        list[0].tutor shouldBe "Juan"

        // Store debe terminar guardando la lista original
        fakeStore.lastSaved shouldBe list
        coVerify(exactly = 1) { patientsApi.updatePatient("1", updated) }
    }


    // getPatient()


    @Test
    fun `getPatient devuelve paciente correcto por id`() = runTest(dispatcher) {
        val p1 = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        val p2 = Patient(id = "2", nombre = "Michi", especie = "Gato", tutor = "Ana")

        fakeStore.internalList = mutableListOf(p1, p2)
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val result = vm.getPatient("2")
        result?.nombre shouldBe "Michi"
    }

    @Test
    fun `getPatient devuelve null cuando id no existe`() = runTest(dispatcher) {
        val p1 = Patient(id = "1", nombre = "Luna", especie = "Perro", tutor = "Juan")
        fakeStore.internalList = mutableListOf(p1)
        coEvery { patientsApi.getPatients() } returns emptyList()

        val vm = PatientsViewModel(fakeStore)
        advanceUntilIdle()

        val result = vm.getPatient("999")
        result shouldBe null
    }


    // Fake de PatientsStore para los tests
    private class FakePatientsStore : PatientsStore {

        var internalList: MutableList<Patient> = mutableListOf()

        // Para inspeccionar cuál fue la última lista guardada
        var lastSaved: List<Patient>? = null

        override fun load(): List<Patient> = internalList.toList()

        override fun save(list: List<Patient>) {
            internalList = list.toMutableList()
            lastSaved = list
        }
    }
}
