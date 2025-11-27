package com.example.vetcare_grupo11.viewmodel

import android.content.Context
import com.example.vetcare_grupo11.network.RetrofitInstance
import com.example.vetcare_grupo11.notifications.programarRecordatorioCita
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
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
import android.util.Log
import io.mockk.unmockkStatic


@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var appointmentsApi: com.example.vetcare_grupo11.network.AppointmentApiService

    private lateinit var context: Context

    @BeforeEach
    fun setUp() {
        // Dispatcher de pruebas para viewModelScope (Main)
        Dispatchers.setMain(dispatcher)

        // Inicializar MockK anotaciones
        MockKAnnotations.init(this, relaxUnitFun = true)

        // Mockear el singleton RetrofitInstance para devolver nuestro API mock
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.appointmentsApi } returns appointmentsApi

        // Mockear la función top-level de notificaciones
        mockkStatic(::programarRecordatorioCita)
        every {
            programarRecordatorioCita(
                context = any(),
                citaId = any(),
                pacienteNombre = any(),
                motivo = any(),
                fechaHoraMillis = any(),
                esVacuna = any(),
                testNow = any()
            )
        } returns Unit

        // Mock de Context para addAppointment
        context = mockk(relaxed = true)

        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        unmockkStatic(Log::class)
    }


    @Test
    fun `loadFromBackend carga citas desde el backend y cambia isLoading`() = runTest(dispatcher) {
        // Arrange
        val backendList = listOf(
            Appointment(
                id = "1",
                patientName = "Firulais",
                motivo = "Control",
                fechaHora = "2025-11-26 10:00",
                fechaHoraMillis = 1_700_000_000_000L,
                notas = "Todo bien",
                estado = "Programada",
                esVacuna = false
            )
        )
        coEvery { appointmentsApi.getAppointments() } returns backendList

        // Act
        val vm = AppointmentsViewModel()
        advanceUntilIdle() // Ejecutar las corutinas de init -> loadFromBackend

        // Assert
        vm.isLoading.value shouldBe false
        vm.appointments.value.size shouldBe 1
        vm.appointments.value[0].patientName shouldBe "Firulais"
    }

    @Test
    fun `loadFromBackend maneja excepcion y deja lista vacia`() = runTest(dispatcher) {
        // Arrange
        coEvery { appointmentsApi.getAppointments() } throws RuntimeException("Network error")

        // Act
        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        // Assert
        vm.isLoading.value shouldBe false
        vm.appointments.value.size shouldBe 0
    }

    // -----------------------------
    // addAppointment()
    // -----------------------------

    @Test
    fun `addAppointment crea cita via API la agrega a la lista y programa notificacion`() = runTest(dispatcher) {
        // Arrange
        // Para que el init no falle: backend devuelve lista vacía
        coEvery { appointmentsApi.getAppointments() } returns emptyList()

        // Cuando se cree la cita, el backend devuelve la misma (eco)
        coEvery { appointmentsApi.createAppointment(any()) } answers { firstArg<Appointment>() }

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        val nuevaCita = Appointment(
            id = null, // sin id, el ViewModel debe generar un UUID
            patientName = "Luna",
            motivo = "Vacuna",
            fechaHora = "2025-11-27 09:00",
            fechaHoraMillis = 1_700_000_100_000L,
            notas = "Primera dosis",
            estado = "Programada",
            esVacuna = true
        )

        // Act
        vm.addAppointment(nuevaCita, context)
        advanceUntilIdle()

        // Assert
        val lista = vm.appointments.value
        lista.size shouldBe 1
        lista[0].patientName shouldBe "Luna"
        lista[0].id.isNullOrBlank() shouldBe false // se generó un id

        // Verificar que se llamó al API
        coVerify(exactly = 1) { appointmentsApi.createAppointment(any()) }

        // Verificar que se programó el recordatorio
        io.mockk.verify(exactly = 1) {
            programarRecordatorioCita(
                context = context,
                citaId = lista[0].id!!,
                pacienteNombre = "Luna",
                motivo = "Vacuna",
                fechaHoraMillis = 1_700_000_100_000L,
                esVacuna = true,
                testNow = true
            )
        }
    }

    @Test
    fun `addAppointment agrega cita localmente cuando API lanza error`() = runTest(dispatcher) {
        // Arrange
        coEvery { appointmentsApi.getAppointments() } returns emptyList()
        coEvery { appointmentsApi.createAppointment(any()) } throws RuntimeException("Network error")

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        val nuevaCita = Appointment(
            id = null,
            patientName = "Rocky",
            motivo = "Control",
            fechaHora = "2025-11-28 11:00",
            fechaHoraMillis = 1_700_000_200_000L,
            notas = "",
            estado = "Programada",
            esVacuna = false
        )

        // Act
        vm.addAppointment(nuevaCita, context)
        advanceUntilIdle()

        // Assert
        val lista = vm.appointments.value
        lista.size shouldBe 1
        lista[0].patientName shouldBe "Rocky"
        lista[0].id.isNullOrBlank() shouldBe false

        // API se llamó pero lanzó excepción
        coVerify(exactly = 1) { appointmentsApi.createAppointment(any()) }
    }

    // -----------------------------
    // updateAppointment()
    // -----------------------------

    @Test
    fun `updateAppointment actualiza la cita en la lista y llama al API`() = runTest(dispatcher) {
        // Arrange
        val original = Appointment(
            id = "abc",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        coEvery { appointmentsApi.getAppointments() } returns listOf(original)
        coEvery { appointmentsApi.updateAppointment("abc", any()) } answers { secondArg<Appointment>() }

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        val actualizado = original.copy(
            motivo = "Control anual",
            notas = "Revisión completa"
        )

        // Act
        vm.updateAppointment(actualizado)
        advanceUntilIdle()

        // Assert
        val lista = vm.appointments.value
        lista.size shouldBe 1
        lista[0].motivo shouldBe "Control anual"
        lista[0].notas shouldBe "Revisión completa"

        coVerify(exactly = 1) { appointmentsApi.updateAppointment("abc", actualizado) }
    }

    @Test
    fun `updateAppointment revierte la lista si el API falla`() = runTest(dispatcher) {
        // Arrange
        val original = Appointment(
            id = "abc",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        coEvery { appointmentsApi.getAppointments() } returns listOf(original)
        coEvery { appointmentsApi.updateAppointment("abc", any()) } throws RuntimeException("Network error")

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        val actualizado = original.copy(
            motivo = "Control anual",
            notas = "Revisión completa"
        )

        // Act
        vm.updateAppointment(actualizado)
        advanceUntilIdle()

        // Assert: debe volver al estado original
        val lista = vm.appointments.value
        lista.size shouldBe 1
        lista[0].motivo shouldBe "Control"
        lista[0].notas shouldBe ""

        coVerify(exactly = 1) { appointmentsApi.updateAppointment("abc", actualizado) }
    }

    // -----------------------------
    // removeAppointment()
    // -----------------------------

    @Test
    fun `removeAppointment elimina la cita y llama al API`() = runTest(dispatcher) {
        // Arrange
        val a1 = Appointment(
            id = "1",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        val a2 = Appointment(
            id = "2",
            patientName = "Rocky",
            motivo = "Vacuna",
            fechaHora = "2025-11-27 11:00",
            fechaHoraMillis = 1_700_000_100_000L
        )

        coEvery { appointmentsApi.getAppointments() } returns listOf(a1, a2)
        coEvery { appointmentsApi.deleteAppointment("1") } returns Unit

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        // Act
        vm.removeAppointment("1")
        advanceUntilIdle()

        // Assert
        val lista = vm.appointments.value
        lista.size shouldBe 1
        lista[0].id shouldBe "2"

        coVerify(exactly = 1) { appointmentsApi.deleteAppointment("1") }
    }

    @Test
    fun `removeAppointment revierte la lista si API falla`() = runTest(dispatcher) {
        // Arrange
        val a1 = Appointment(
            id = "1",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        val a2 = Appointment(
            id = "2",
            patientName = "Rocky",
            motivo = "Vacuna",
            fechaHora = "2025-11-27 11:00",
            fechaHoraMillis = 1_700_000_100_000L
        )

        coEvery { appointmentsApi.getAppointments() } returns listOf(a1, a2)
        coEvery { appointmentsApi.deleteAppointment("1") } throws RuntimeException("Network error")

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        // Act
        vm.removeAppointment("1")
        advanceUntilIdle()

        // Assert: debe volverse a la lista original
        val lista = vm.appointments.value
        lista.size shouldBe 2
        lista.first { it.id == "1" }.patientName shouldBe "Luna"
        lista.first { it.id == "2" }.patientName shouldBe "Rocky"

        coVerify(exactly = 1) { appointmentsApi.deleteAppointment("1") }
    }

    // -----------------------------
    // getAppointment()
    // -----------------------------

    @Test
    fun `getAppointment devuelve la cita correcta por id`() = runTest(dispatcher) {
        // Arrange
        val a1 = Appointment(
            id = "1",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        val a2 = Appointment(
            id = "2",
            patientName = "Rocky",
            motivo = "Vacuna",
            fechaHora = "2025-11-27 11:00",
            fechaHoraMillis = 1_700_000_100_000L
        )

        coEvery { appointmentsApi.getAppointments() } returns listOf(a1, a2)

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        // Act
        val result = vm.getAppointment("2")

        // Assert
        result?.patientName shouldBe "Rocky"
    }

    @Test
    fun `getAppointment devuelve null si no existe el id`() = runTest(dispatcher) {
        // Arrange
        val a1 = Appointment(
            id = "1",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1_700_000_000_000L
        )
        coEvery { appointmentsApi.getAppointments() } returns listOf(a1)

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        // Act
        val result = vm.getAppointment("999")

        // Assert
        result shouldBe null
    }
}

