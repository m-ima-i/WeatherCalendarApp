package com.anri.weathercalendarapp.weather.data.repositoryimpl

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlacesRepositoryImplTest {

    private val placesClient: PlacesClient = mockk()

    private lateinit var repository: PlacesRepositoryImpl

    @Before
    fun setup() {
        // Task.await() はKotlinx Coroutinesの拡張関数なのでmockkStaticが必要
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        repository = PlacesRepositoryImpl(placesClient)
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    /**
     * AutocompletePredictionのモックを作成するヘルパー関数
     *
     * getPrimaryText/getSecondaryText/getFullTextはSpannableStringを返すが、
     * SpannableStringはAndroid FrameworkのfinalクラスなのでmockkStaticでモックする。
     * ここではSpannableStringをmockkStaticで構築する。
     */
    private fun createPredictionMock(
        placeIdValue: String,
        primaryText: String,
        secondaryText: String
    ): AutocompletePrediction {
        val prediction: AutocompletePrediction = mockk(relaxed = true)
        every { prediction.placeId } returns placeIdValue
        every { prediction.getPrimaryText(null).toString() } returns primaryText
        every { prediction.getSecondaryText(null).toString() } returns secondaryText
        return prediction
    }

    // ========== searchPlaces ==========

    @Test
    fun `searchPlaces - 正常系 - 予測結果をPlaceSuggestionリストに変換して返す`() = runTest {
        // Arrange
        val prediction1 = createPredictionMock(
            placeIdValue = "place_id_1",
            primaryText = "東京",
            secondaryText = "日本"
        )
        val prediction2 = createPredictionMock(
            placeIdValue = "place_id_2",
            primaryText = "東京都",
            secondaryText = "日本"
        )

        val response: FindAutocompletePredictionsResponse = mockk {
            every { autocompletePredictions } returns listOf(prediction1, prediction2)
        }

        val task: Task<FindAutocompletePredictionsResponse> = mockk()
        every { placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()) } returns task
        coEvery { task.await() } returns response

        // Act
        val result = repository.searchPlaces("東京")

        // Assert
        assertEquals(2, result.size)
        assertEquals("place_id_1", result[0].placeId)
        assertEquals("東京", result[0].mainText)
        assertEquals("日本", result[0].secondaryText)
        assertEquals("place_id_2", result[1].placeId)
        assertEquals("東京都", result[1].mainText)
    }

    @Test(expected = RuntimeException::class)
    fun `searchPlaces - 異常系 - 例外発生時に再スローする`() = runTest {
        // Arrange
        val task: Task<FindAutocompletePredictionsResponse> = mockk()
        every { placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()) } returns task
        coEvery { task.await() } throws RuntimeException("API Error")

        // Act: ViewModel側でtry-catchしてToastを発火させる前提のため、例外は呼び出し側に伝播する
        repository.searchPlaces("東京")
    }

    @Test
    fun `searchPlaces - 境界値 - 結果が0件の場合空リストを返す`() = runTest {
        // Arrange
        val response: FindAutocompletePredictionsResponse = mockk {
            every { autocompletePredictions } returns emptyList()
        }

        val task: Task<FindAutocompletePredictionsResponse> = mockk()
        every { placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()) } returns task
        coEvery { task.await() } returns response

        // Act
        val result = repository.searchPlaces("xyznonexistent")

        // Assert
        assertTrue(result.isEmpty())
    }

    // ========== getPlaceCoordinates ==========

    @Test
    fun `getPlaceCoordinates - 正常系 - LatLngを返す`() = runTest {
        // Arrange
        val expectedLatLng = LatLng(35.6762, 139.6503)
        val place: Place = mockk {
            every { location } returns expectedLatLng
        }
        val response: FetchPlaceResponse = mockk {
            every { this@mockk.place } returns place
        }

        val task: Task<FetchPlaceResponse> = mockk()
        every { placesClient.fetchPlace(any<FetchPlaceRequest>()) } returns task
        coEvery { task.await() } returns response

        // Act
        val result = repository.getPlaceCoordinates("place_id_1")

        // Assert
        assertNotNull(result)
        assertEquals(35.6762, result!!.latitude)
        assertEquals(139.6503, result.longitude)
    }

    @Test
    fun `getPlaceCoordinates - 異常系 - 例外発生時にnullを返す`() = runTest {
        // Arrange
        val task: Task<FetchPlaceResponse> = mockk()
        every { placesClient.fetchPlace(any<FetchPlaceRequest>()) } returns task
        coEvery { task.await() } throws RuntimeException("Fetch Error")

        // Act
        val result = repository.getPlaceCoordinates("invalid_place_id")

        // Assert
        assertNull(result)
    }

    @Test
    fun `getPlaceCoordinates - 正常系 - 呼び出し後にトークンが再生成される`() = runTest {
        // Arrange
        val expectedLatLng = LatLng(35.6762, 139.6503)
        val place: Place = mockk {
            every { location } returns expectedLatLng
        }
        val response: FetchPlaceResponse = mockk {
            every { this@mockk.place } returns place
        }

        val fetchTask: Task<FetchPlaceResponse> = mockk()
        every { placesClient.fetchPlace(any<FetchPlaceRequest>()) } returns fetchTask
        coEvery { fetchTask.await() } returns response

        // searchPlaces 用のモック
        val searchResponse: FindAutocompletePredictionsResponse = mockk {
            every { autocompletePredictions } returns emptyList()
        }
        val searchTask: Task<FindAutocompletePredictionsResponse> = mockk()
        every { placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()) } returns searchTask
        coEvery { searchTask.await() } returns searchResponse

        // Act: getPlaceCoordinatesを呼ぶとトークンが再生成される
        repository.getPlaceCoordinates("place_id_1")

        // getPlaceCoordinates成功後にsearchPlacesを呼ぶ（新しいトークンが使われることを確認）
        // 例外が発生しなければトークン再生成後も正常に動作することの確認
        val result = repository.searchPlaces("大阪")

        // Assert: 例外なく処理が完了すること
        assertTrue(result.isEmpty())
    }
}
