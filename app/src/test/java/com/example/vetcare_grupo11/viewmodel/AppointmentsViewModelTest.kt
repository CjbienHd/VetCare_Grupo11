package com.example.vetcare_grupo11.viewmodel

import com.example.vetcare_grupo11.network.AppointmentApiService
import com.example.vetcare_grupo11.network.RetrofitInstance
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var fakeApi: AppointmentApiService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        clearAllMocks()
        mockkObject(RetrofitInstance)
        every { RetrofitInstance.appointmentsApi } returns fakeApi
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `loadFromBackend carga citas y actualiza estado`() = runTest {
        val backendList = listOf(
            Appointment(
                id = "1",
                patientName = "Luna",
                motivo = "Control",
                fechaHora = "2025-11-26 10:00",
                fechaHoraMillis = 1234L,
                notas = "",
                estado = "Programada",
                esVacuna = false
            ),
            Appointment(
                id = "2",
                patientName = "Max",
                motivo = "Vacuna",
                fechaHora = "2025-11-27 09:00",
                fechaHoraMillis = 5678L,
                notas = "",
                estado = "Programada",
                esVacuna = true
            )
        )
        coEvery { fakeApi.getAppointments() } returns backendList

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        vm.isLoading.first() shouldBe false
        vm.appointments.first() shouldBe backendList
    }

    @Test
    fun `loadFromBackend maneja error sin crashear`() = runTest {
        coEvery { fakeApi.getAppointments() } throws RuntimeException("Fallo backend")

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        vm.isLoading.first() shouldBe false
        vm.appointments.first().size shouldBe 0
    }

    @Test
    fun `deleteAppointment elimina cita de la lista cuando existe`() = runTest {
        val cita = Appointment(
            id = "123",
            patientName = "Luna",
            motivo = "Control",
            fechaHora = "2025-11-26 10:00",
            fechaHoraMillis = 1234L,
            notas = "",
            estado = "Programada",
            esVacuna = false
        )
        coEvery { fakeApi.getAppointments() } returns listOf(cita)
        coEvery { fakeApi.deleteAppointment("123") } returns Unit

        val vm = AppointmentsViewModel()
        advanceUntilIdle()

        vm.appointments.first().size shouldBe 1

        vm.deleteAppointment("123")
        advanceUntilIdle()

        vm.appointments.first().size shouldBe 0
        vm.getAppointment("123") shouldBe null
    }
}