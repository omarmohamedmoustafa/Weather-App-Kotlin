package com.example.weatherapp.view_model_test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.repository.WeatherAppRepository
import com.example.weatherapp.ui.home.view_model.HomeViewModel
import getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: WeatherAppRepository
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchWeatherData_emitsCurrentAndForecastWeather_whenRepositoryReturnsSuccess() = runTest {
        val currentResponse = mockk<CurrentWeatherResponse>()
        val forecastResponse = mockk<WeatherResponse>()

        coEvery {
            repository.getCurrentWeather(any(), any(), any(), any(), any())
        } returns Result.success(currentResponse)

        coEvery {
            repository.getWeatherForecast(any(), any(), any(), any(), any())
        } returns Result.success(forecastResponse)

        viewModel.fetchWeatherData(30.0f, 31.0f, "dummy_api_key")
        advanceUntilIdle()

        assertThat(viewModel.currentWeather.getOrAwaitValue(), `is`(currentResponse))
        assertThat(viewModel.forecastWeather.getOrAwaitValue(), `is`(forecastResponse))
        assertThat(viewModel.errorMessage.getOrAwaitValue(), nullValue())
    }

    @Test
    fun fetchWeatherData_setsErrorMessage_whenRepositoryReturnsFailure() = runTest {
        coEvery {
            repository.getCurrentWeather(any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Failed current"))

        coEvery {
            repository.getWeatherForecast(any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Failed forecast"))

        viewModel.fetchWeatherData(30.0f, 31.0f, "dummy_api_key")
        advanceUntilIdle()

        assertThat(viewModel.errorMessage.getOrAwaitValue(), `is`("Failed to fetch weather data"))
    }

    @Test
    fun formatDateTime_returnsFormattedDateAndTime_whenValidTimestampAndOffsetGiven() {
        val timestamp = 1716892800L // May 28, 2024 00:00:00 UTC
        val timezoneOffset = 7200L

        val (date, time) = viewModel.formatDateTime(timestamp, timezoneOffset)

        assertThat(date, `is`("Tue, May 28"))
        assertThat(time, `is`("12:40 PM"))
    }

    @Test
    fun formatHourlyTime_returnsFormattedTime_whenValidTimestampAndOffsetGiven() {
        val timestamp = 1716892800L // May 28, 2024 00:00:00 UTC
        val timezoneOffset = 7200L // +2 hours => 02:00:00 local time

        val formattedTime = viewModel.formatHourlyTime(timestamp, timezoneOffset)

        assertThat(formattedTime, `is`("12 PM"))
    }

}
