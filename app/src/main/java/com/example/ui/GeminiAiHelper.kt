package com.example.ui

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiHelper {
    private const val TAG = "GeminiAiHelper"

    data class SuggestionResult(
        val jobTitle: String,
        val summary: String,
        val error: String? = null
    )

    suspend fun getProfileSuggestions(
        currentTitle: String,
        currentSummary: String,
        skillsList: List<String>,
        customApiKey: String? = null
    ): SuggestionResult = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) {
            customApiKey.trim()
        } else {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }
        }
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext SuggestionResult(
                "", 
                "", 
                "Gemini API কী পাওয়া যায়নি। অনুগ্রহ করে API Key সেট করুন অথবা Secrets-এ 'GEMINI_API_KEY' যুক্ত করুন।"
            )
        }

        val skillsStr = if (skillsList.isEmpty()) "None specified yet" else skillsList.joinToString(", ")
        val prompt = """
            You are a professional CV optimizer. Enhance the following resume information for a modern professional look.
            
            Current Job Title: $currentTitle
            Current Profile Summary: $currentSummary
            Skills: $skillsStr
            
            Please suggest a highly professional, trendy, and ATS-friendly version of the Job Title and Profile Summary.
            Keep the Job Title concise (e.g. 2-4 words) and the Summary elegant, motivating, and punchy (around 1-3 sentences).
            The suggestions must be written in English.
            
            You MUST respond ONLY with a raw JSON object matching the following structure (do not include markdown block ticks, backticks, or other chat text):
            {"suggestedJobTitle": "Suggested Title", "suggestedSummary": "Suggested summary text"}
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val jsonPayload = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API error: $errBody")
                    return@withContext SuggestionResult("", "", "Gemini API error (Status: ${response.code})")
                }
                
                val responseStr = response.body?.string()
                if (responseStr.isNullOrEmpty()) {
                    return@withContext SuggestionResult("", "", "ফাঁকা রেসপন্স পাওয়া গিয়েছে।")
                }

                val parentObj = JSONObject(responseStr)
                val candidates = parentObj.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val contentObj = candidates.getJSONObject(0).getJSONObject("content")
                    val parts = contentObj.getJSONArray("parts")
                    if (parts.length() > 0) {
                        var textStr = parts.getJSONObject(0).getString("text").trim()
                        
                        // Parse JSON from text
                        if (textStr.startsWith("```")) {
                            textStr = textStr.removePrefix("```json").removePrefix("```")
                            if (textStr.endsWith("```")) {
                                textStr = textStr.removeSuffix("```")
                            }
                        }
                        textStr = textStr.trim()
                        
                        val suggestionsObj = JSONObject(textStr)
                        val sugTitle = suggestionsObj.optString("suggestedJobTitle", "").trim()
                        val sugSummary = suggestionsObj.optString("suggestedSummary", "").trim()
                        
                        return@withContext SuggestionResult(sugTitle, sugSummary)
                    }
                }
                return@withContext SuggestionResult("", "", "রেসপন্স পার্স করতে ত্রুটি হয়েছে।")
            }
        } catch (e: Exception) {
            Log.e(TAG, "API call failed", e)
            return@withContext SuggestionResult("", "", "Error: ${e.localizedMessage ?: "Unknown network issue"}")
        }
    }
}
