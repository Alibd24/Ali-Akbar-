package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String // Base64 encoding
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class ResponsePropertySchema(
    val type: String,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val items: ResponseSchema? = null,
    val properties: Map<String, ResponsePropertySchema>? = null,
    val required: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    // Helper functions
    suspend fun formatChemistDetails(rawText: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return rawText // return original if no key
        }

        val prompt = "Format this chemist/pharmacy raw text into a clean professional string with 'Name, Full Address'. Remove redundant conversational words or formatting marks like markdown asterisks. Return ONLY the cleaned plain text."
        
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "$prompt\n\nRaw text: $rawText")
                    )
                )
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: rawText
        } catch (e: Exception) {
            e.printStackTrace()
            rawText
        }
    }

    suspend fun scanInvoiceForProducts(base64Image: String, mimeType: String): List<FormProduct> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return emptyList()
        }

        val prompt = "Extract the list of pharmaceutical products from this image. Map the data to these fields: name (string), group (must be exactly one of: UHP, UMP, UD, NUV, S&N, or Others), batch (string, default to empty string), mfg (string in YYYY-MM format), exp (string in YYYY-MM format), pack (integer, default 1), qty (number), tp (number). If any numbers are missing, try to deduce them or set to 0."

        // Defined JSON response schema to get a strictly typed structure
        val schema = ResponseSchema(
            type = "ARRAY",
            items = ResponseSchema(
                type = "OBJECT",
                properties = mapOf(
                    "name" to ResponsePropertySchema("STRING", "The name of the product"),
                    "group" to ResponsePropertySchema("STRING", "Must be exactly UHP, UMP, UD, NUV, S&N, or Others"),
                    "batch" to ResponsePropertySchema("STRING", "Batch number"),
                    "mfg" to ResponsePropertySchema("STRING", "Mfg date in YYYY-MM"),
                    "exp" to ResponsePropertySchema("STRING", "Expiry date in YYYY-MM"),
                    "pack" to ResponsePropertySchema("INTEGER", "Pack size"),
                    "qty" to ResponsePropertySchema("NUMBER", "Quantity"),
                    "tp" to ResponsePropertySchema("NUMBER", "Trade Price")
                ),
                required = listOf("name", "group", "pack", "qty", "tp")
            )
        )

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = schema
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: return emptyList()
            
            // Parse extracted list
            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, Map::class.java)
            val adapter = moshi.adapter<List<Map<String, Any>>>(type)
            val list = adapter.fromJson(jsonText) ?: return emptyList()

            list.map { map ->
                val name = map["name"] as? String ?: "Unknown Product"
                val rawGroup = map["group"] as? String ?: "Others"
                val group = if (listOf("UHP", "UMP", "UD", "NUV", "S&N", "Others").contains(rawGroup)) rawGroup else "Others"
                val batch = map["batch"] as? String ?: ""
                val mfg = map["mfg"] as? String ?: ""
                val exp = map["exp"] as? String ?: ""
                val pack = (map["pack"] as? Number)?.toInt() ?: 1
                val qty = (map["qty"] as? Number)?.toDouble() ?: 0.0
                val tp = (map["tp"] as? Number)?.toDouble() ?: 0.0

                FormProduct(name, group, batch, mfg, exp, pack, qty, tp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
