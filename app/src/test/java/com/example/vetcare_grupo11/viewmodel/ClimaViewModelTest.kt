package com.example.vetcare_grupo11.viewmodel

import com.example.vetcare_grupo11.data.ClimaRetrofit
import com.example.vetcare_grupo11.data.RespuestaClima
import com.example.vetcare_grupo11.data.ServicioClimaApi
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class ClimaViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @MockK
    lateinit var apiMock: ServicioClimaApi

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        MockKAnnotations.init(this, relaxUnitFun = true)

        // ClimaRetrofit.api -> mock
        mockkObject(ClimaRetrofit)
        // siempre que el código use ClimaRetrofit.api, devolvemos apiMock
        io.mockk.every { ClimaRetrofit.api } returns apiMock
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        unmockkObject(ClimaRetrofit)
    }


    // cargarClimaClinica - éxito


    @Test
    fun `cargarClimaClinica actualiza estado con datos y sin error`() = runTest(dispatcher) {
        // Arrange
        val respuestaFake = mockk<RespuestaClima>(relaxed = true)

        coEvery { apiMock.obtenerClimaActual(-33.0245, -71.5518) } returns respuestaFake

        val vm = ClimaViewModel()

        // Act
        vm.cargarClimaClinica() // usa los valores por defecto
        advanceUntilIdle()

        // Assert
        val estado = vm.estado.value
        estado.cargando shouldBe false
        estado.datosClima shouldBe respuestaFake
        estado.error shouldBe null

        coVerify(exactly = 1) { apiMock.obtenerClimaActual(-33.0245, -71.5518) }
    }


    // cargarClimaClinica - error


    @Test
    fun `cargarClimaClinica guarda mensaje de error cuando el backend falla`() = runTest(dispatcher) {
        // Arrange
        coEvery { apiMock.obtenerClimaActual(any(), any()) } throws RuntimeException("Network error")

        val vm = ClimaViewModel()

        // Act
        vm.cargarClimaClinica(latitud = 1.0, longitud = 2.0)
        advanceUntilIdle()

        // Assert
        val estado = vm.estado.value
        estado.cargando shouldBe false
        estado.datosClima shouldBe null
        estado.error shouldBe "Network error"
    }


    // evitar llamadas repetidas


    @Test
    fun `cargarClimaClinica no vuelve a llamar API si ya hay datos`() = runTest(dispatcher) {
        // Arrange
        val respuestaFake = mockk<RespuestaClima>(relaxed = true)
        coEvery { apiMock.obtenerClimaActual(any(), any()) } returns respuestaFake

        val vm = ClimaViewModel()

        // Primera llamada: carga datos
        vm.cargarClimaClinica()
        advanceUntilIdle()

        // Segunda llamada: como ya hay datosClima, debe retornar sin llamar API
        vm.cargarClimaClinica()
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { apiMock.obtenerClimaActual(any(), any()) }
        vm.estado.value.datosClima shouldBe respuestaFake
    }

    @Test
    fun `cargarClimaClinica no llama API si ya esta cargando`() = runTest(dispatcher) {
        // Arrange
        val respuestaFake = mockk<RespuestaClima>(relaxed = true)
        coEvery { apiMock.obtenerClimaActual(any(), any()) } returns respuestaFake

        val vm = ClimaViewModel()

        // Primera llamada: marca cargando=true y lanza la corrutina, pero NO avanzamos el dispatcher aún
        vm.cargarClimaClinica()

        // Segunda llamada inmediata: como cargando=true, debe salir sin llamar de nuevo al API
        vm.cargarClimaClinica()

        // Ahora dejamos que termine la primera llamada
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { apiMock.obtenerClimaActual(any(), any()) }
        vm.estado.value.datosClima shouldBe respuestaFake
    }
}


