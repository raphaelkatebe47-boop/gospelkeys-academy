package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Moshi Serializable Data Classes for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// --- Retrofit Endpoint Definition ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Practice Coach Manager class ---

class PracticeCoachManager {

    suspend fun getCoachResponse(
        userMessage: String,
        userSkill: String,
        chatHistory: List<Pair<String, Boolean>> // Pair of (Text, IsUser)
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Hi there! I am Coach Barnabas, your Gospel Piano Instructor. To unlock the full power of real-time AI critique, personalized progression plans, and custom passing chord suggestions, please enter your API key in the **Secrets Panel** (GEMINI_API_KEY) in Google AI Studio first! \n\n" +
                    "To help you right now while offline, let's look at the Tennessee/Gospel 1-4 walk-ups: \n" +
                    "In C Major, the basic walk-up is: C -> C/E -> F. Spiced up with Gospel Tritones: Play C in Bass, then Bb-E in Right Hand, progressing to F in Bass, then A-D-F in Right Hand!"
        }

        val systemPrompt = "You are Coach Barnabas, an expert, enthusiastic black-church of-the-soul Gospel Piano Professor. " +
                "You are teaching a student whose current level is: $userSkill. " +
                "Teach gospel and contemporary christian music piano chops: voicings, tritone progressions (like 7-3-6, 2-5-1, 1-4 walkups), Nashville numbers, slide grace notes, and dominant minor-9ths. " +
                "Keep responses inspiring, supportive, and easy to understand using text piano chord graphics (e.g., C/E: LH C, RH G-B-E). " +
                "Always write with highly readable spacing. Be concise and keep responses under 250 words."

        // Convert the history to the Gemini API format
        val previousTurns = chatHistory.map { (text, isUser) ->
            GeminiContent(
                parts = listOf(GeminiPart(text = text))
            )
        }

        // Add the current user query as the final content element
        val contents = previousTurns.toMutableList().apply {
            add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userMessage))
                )
            )
        }

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Coach Barnabas is adjusting his piano soundboard. Please try telling me that again!"
        } catch (e: Exception) {
            "Error from Barnabas: ${e.message ?: "Lost network connection."}\n\nMake sure your Internet is running and your GEMINI_API_KEY is configured in the Secrets Panel!"
        }
    }
}
