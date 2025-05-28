package com.example.weatherapp.repository_test

import com.example.weatherapp.model.local.ILocalDataSource
import com.example.weatherapp.model.pojos.Alert
import com.example.weatherapp.model.pojos.City
import com.example.weatherapp.model.pojos.Clouds
import com.example.weatherapp.model.pojos.Coord
import com.example.weatherapp.model.pojos.CurrentClouds
import com.example.weatherapp.model.pojos.CurrentCoordinates
import com.example.weatherapp.model.pojos.CurrentSys
import com.example.weatherapp.model.pojos.CurrentWeather
import com.example.weatherapp.model.pojos.CurrentWeatherData
import com.example.weatherapp.model.pojos.CurrentWeatherResponse
import com.example.weatherapp.model.pojos.CurrentWind
import com.example.weatherapp.model.pojos.FavouriteCountry
import com.example.weatherapp.model.pojos.Sys
import com.example.weatherapp.model.pojos.Weather
import com.example.weatherapp.model.pojos.WeatherData
import com.example.weatherapp.model.pojos.WeatherDataOfHour
import com.example.weatherapp.model.pojos.WeatherResponse
import com.example.weatherapp.model.pojos.Wind
import com.example.weatherapp.model.remote.IRemoteDataSource
import com.example.weatherapp.model.repository.WeatherAppRepository
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {
    lateinit var localDataSource: ILocalDataSource
    lateinit var remoteDataSource: IRemoteDataSource
    lateinit var repo: WeatherAppRepository

    private val mockHourlyForecastResponse = WeatherResponse(
        id = 0,
        cod = "200",
        message = 0,
        numberOfHours = 1,
        list = listOf(
            WeatherData(
                unixTimeStamp = 1627040400L,
                weatherDataOfHour = WeatherDataOfHour(
                    temp = 298.55f,
                    feelsLike = 298.95f,
                    tempMin = 298.55,
                    tempMax = 298.56,
                    pressure = 1013,
                    seaLevel = 1013,
                    grndLevel = 1005,
                    humidity = 87,
                    tempKf = 0.01f
                ),
                weather = listOf(
                    Weather(
                        id = 500,
                        weatherStatusOfHour = "Rain",
                        weatherDescriptionOfHour = "light rain",
                        icon = "10d"
                    )
                ),
                clouds = Clouds(all = 75),
                wind = Wind(speed = 4.12f, deg = 240, gust = 7.2f),
                visibility = 10000,
                pop = 0.2f,
                sys = Sys(pod = "d"),
                dtTxt = "2021-07-23 15:00:00"
            )
        ),
        city = City(
            id = 2643743,
            name = "London",
            coord = Coord(lat = 51.5085f, lon = -0.1257f),
            country = "GB",
            population = 1000000,
            timezone = 3600,
            sunrise = 1627014875L,
            sunset = 1627071392L
        )
    )

    private val mockCurrentWeatherResponse = CurrentWeatherResponse(
        currentId = 0,
        coordinates = CurrentCoordinates(
            lon = -0.1257,
            lat = 51.5085
        ),
        currentWeather = listOf(
            CurrentWeather(
                id = 800,
                currentWeatherStatus = "Clear",
                currentWeatherDescription = "clear sky",
                icon = "01d"
            )
        ),
        base = "stations",
        weatherData = CurrentWeatherData(
            temp = 293.15f,
            feelsLike = 292.86f,
            tempMin = 292.15f,
            tempMax = 294.15f,
            pressure = 1012,
            humidity = 60,
            seaLevel = 1012,
            grndLevel = 1008
        ),
        visibility = 10000,
        wind = CurrentWind(
            speed = 3.6,
            deg = 200
        ),
        clouds = CurrentClouds(
            all = 0
        ),
        unixTimeStamp = 1627040400L,
        sys = CurrentSys(
            type = 1,
            id = 1414,
            countryCode = "GB",
            unixSunriseTimeStamp = 1627014875L,
            unixSunsetTimeStamp = 1627071392L
        ),
        offsetFromUTC = 3600,
        id = 2643743,
        currentLocationName = "London",
        responseCode = 200
    )

    private val mockFavouriteCountry = FavouriteCountry(
        name = "Tokyo",
        latitude = 35.6895f,
        longitude = 139.6917f,
        minTemp = 285.15f,
        maxTemp = 295.15f,
        weatherIcon = "10d",
        flagUrl = "https://flagcdn.com/w320/jp.png"
    )

    private val mockAlert = Alert(
        alertId = 1L,
        dateMillis = 1727452800000L,
        alertTriggerAt = 1727456400000L,
        alertStopAt = 1727460000000L,
        isAlarm = true,
    )

    @Before
    fun setup() {
        val weatherResponses = mutableMapOf(Pair(51.5085f, -0.1257f) to mockHourlyForecastResponse)
        val currentWeatherResponses = mutableMapOf(Pair(51.5085f, -0.1257f) to mockCurrentWeatherResponse)
        localDataSource = FakeLocalDataSource(
            weatherResponses = weatherResponses,
            currentWeatherResponses = currentWeatherResponses
        )
        remoteDataSource = FakeRemoteDataSource(
            weatherResponses = weatherResponses,
            currentWeatherResponses = currentWeatherResponses
        )
        repo = WeatherAppRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun getCurrentWeather_validCoordinates_returnsSuccess() = runTest {
        val result = repo.getCurrentWeather(
            latitude = 51.5085f,
            longitude = -0.1257f,
            apiKey = "valid_api_key"
        )
        assertThat(result.isSuccess, `is`(true))
        result.onSuccess { response ->
            assertThat(response.currentLocationName, `is`("London"))
            assertThat(response.weatherData.temp, `is`(293.15f))
        }
    }

    @Test
    fun getHourlyForecast_validCoordinates_returnsSuccess() = runTest {
        val result = repo.getWeatherForecast(
            latitude = 51.5085f,
            longitude = -0.1257f,
            apiKey = "valid_api_key"
        )
        assertThat(result.isSuccess, `is`(true))
        result.onSuccess { response ->
            assertThat(response.city.name, `is`("London"))
            assertThat(response.list[0].weatherDataOfHour.temp, `is`(298.55f))
        }
    }

    @Test
    fun saveHourlyForecastToLocal_savesCorrectly() = runTest {
        repo.saveWeatherResponse(mockHourlyForecastResponse)
        val retrieved = localDataSource.getWeatherResponse()
        assertThat(retrieved?.city?.name, `is`("London"))
    }

    @Test
    fun saveCurrentWeatherToLocal_savesCorrectly() = runTest {
        repo.saveCurrentWeatherResponse(mockCurrentWeatherResponse)
        val retrieved = localDataSource.getCurrentWeatherResponse()
        assertThat(retrieved?.currentLocationName, `is`("London"))
    }

    @Test
    fun favoriteLocationOperations_workCorrectly() = runTest {
        // Test insert
        repo.insertFavouriteCountry(mockFavouriteCountry)
        val locations = repo.getAllFavouriteCountries()
        assertThat(locations.size, `is`(1))
        assertThat(locations[0].name, `is`("Tokyo"))

        // Test delete
        repo.deleteFavouriteCountry(mockFavouriteCountry.name, mockFavouriteCountry.latitude, mockFavouriteCountry.longitude)
        val updatedLocations = repo.getAllFavouriteCountries()
        assertThat(updatedLocations.size, `is`(0))
    }

    @Test
    fun alertOperations_workCorrectly() = runTest {
        // Test save
        val alertId = repo.insertAlert(mockAlert)
        val alerts = repo.getAllAlerts()
        assertThat(alerts.size, `is`(1))
        assertThat(alerts[0].alertId, `is`(1L))

        // Test get by id
        val retrievedAlert = repo.getAlertById(alertId)
        assertThat(retrievedAlert?.dateMillis, `is`(1727452800000L))

        // Test delete
        repo.deleteAlert(alertId)
        val updatedAlerts = repo.getAllAlerts()
        assertThat(updatedAlerts.size, `is`(0))
    }
}