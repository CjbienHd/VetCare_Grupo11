package com.example.vetcare_grupo11.viewmodel

import com.example.vetcare_grupo11.data.FirebasePatientsSync
import com.example.vetcare_grupo11.data.PatientsStore
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

// Implementación falsa de PatientsStore que vive solo en la memoria.
class FakePatientsStore(
    private var stored: List<Patient> = emptyList()
) : PatientsStore {
    override fun load(): List<Patient> = stored
    override fun save(list: List<Patient>) {
        stored = list
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PatientsViewModelTest {

    // 1. Creamos un dispatcher de prueba para controlar las corrutinas
    private val testDispatcher = StandardTestDispatcher()

    // 2. Declaramos nuestras dependencias falsas (mocks)
    @MockK
    private lateinit var fakeRemote: FirebasePatientsSync

    @Before
    fun setup() {
        // Inicializamos los mocks y redirigimos el Dispatcher.Main
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // 3. Definimos el comportamiento por defecto de nuestro Firebase falso
        // No hará nada cuando se le pida subir o borrar datos
        every { fakeRemote.uploadPatient(any()) } just Runs
        every { fakeRemote.deletePatient(any()) } just Runs
        // Devolverá una lista vacía cuando se le pida descargar
        coEvery { fakeRemote.downloadPatients() } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addPatient agrega un paciente a la lista`() = runTest {
        val fakeStore = FakePatientsStore()
        // Inyectamos las dependencias falsas al crear el ViewModel
        val vm = PatientsViewModel(fakeStore, fakeRemote)
        advanceUntilIdle()

        val nuevo = Patient(nombre = "Firulais", especie = "Perro", raza = "Labrador", tutor = "Juan")

        vm.addPatient(nuevo)
        advanceUntilIdle()

        vm.patients.value.size shouldBe 5 // 4 por defecto + 1 nuevo
        vm.patients.value.last().nombre shouldBe "Firulais"
    }

    @Test
    fun `removePatient elimina al paciente por id`() = runTest {
        val p1 = Patient(nombre = "Luna", especie = "Gato", raza = "Siames", tutor = "Ana")
        val p2 = Patient(nombre = "Rocky", especie = "Perro", raza = "Beagle", tutor = "Pedro")
        val fakeStore = FakePatientsStore(listOf(p1, p2))
        val vm = PatientsViewModel(fakeStore, fakeRemote)
        advanceUntilIdle()

        vm.patients.value.size shouldBe 2

        vm.removePatient(p1.id)
        advanceUntilIdle()

        vm.patients.value.size shouldBe 1
        vm.patients.value.first().id shouldBe p2.id
    }

    @Test
    fun `updatePatient reemplaza los datos del paciente`() = runTest {
        val original = Patient(nombre = "Luna", especie = "Gato", raza = "Siames", tutor = "Ana")
        val fakeStore = FakePatientsStore(listOf(original))
        val vm = PatientsViewModel(fakeStore, fakeRemote)
        advanceUntilIdle()

        val actualizado = original.copy(nombre = "Luna Modificada")

        vm.updatePatient(actualizado)
        advanceUntilIdle()

        val result = vm.getPatient(original.id)
        result!!.nombre shouldBe "Luna Modificada"
    }

}
