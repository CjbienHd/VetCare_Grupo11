package com.example.vetcare_grupo11.viewmodel

import com.example.vetcare_grupo11.data.ClimaActual
import com.example.vetcare_grupo11.data.ClimaRetrofit
import com.example.vetcare_grupo11.data.RespuestaClima
import com.example.vetcare_grupo11.data.ServicioClimaApi
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
class ClimaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    lateinit var fakeApi: ServicioClimaApi

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        clearAllMocks()
        mockkObject(ClimaRetrofit)
        every { ClimaRetrofit.api } returns fakeApi
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `cargarClima carga datos correctamente`() = runTest {
        // arrange
        val respuesta = RespuestaClima(
            latitude = 1.0,
            longitude = 2.0,
            timezone = "UTC",
            current_weather = ClimaActual(
                temperature = 20.0,
                windspeed = 5.0,
                winddirection = 90.0,
                weathercode = 0,
                time = "2025-11-26T00:00"
            )
        )
        coEvery { fakeApi.obtenerClimaActual(any(), any(), any()) } returns respuesta

        val vm = ClimaViewModel()

        // act
        vm.cargarClima(10.0, 20.0)
        advanceUntilIdle()

        // assert
        val estado = vm.estado.first()
        estado.cargando shouldBe false
        estado.error shouldBe null
        estado.datosClima shouldBe respuesta
    }

    @Test
    fun `cargarClima maneja error de red`() = runTest {
        coEvery { fakeApi.obtenerClimaActual(any(), any(), any()) } throws RuntimeException("Fallo red")

        val vm = ClimaViewModel()

        vm.cargarClima(10.0, 20.0)
        advanceUntilIdle()

        val estado = vm.estado.first()
        estado.cargando shouldBe false
        estado.datosClima shouldBe null
        estado.error shouldBe "Fallo red"
    }
}