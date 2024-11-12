package com.github.onlynotesswent.utils

import com.github.onlynotesswent.BuildConfig
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * A class that sends requests to the OpenAI API to generate text based on a given prompt.
 *
 * @param client the OkHttpClient instance to use for sending requests (default: OkHttpClient()),
 *   used for testing
 */
class OpenAI(private val client: OkHttpClient = OkHttpClient()) {
  private val apiKey: String = BuildConfig.OPEN_AI_API_KEY
  private val endpoint = "https://api.openai.com/v1/chat/completions"

  init {
    // Check if the API key is provided
    require(apiKey.isNotBlank()) { "API key is missing. Please provide a valid OpenAI API key." }
  }

  /**
   * Sends a POST request to the OpenAI API to generate text based on the given prompt.
   *
   * @param prompt the prompt to generate text from
   * @param onSuccess the callback function to invoke on a successful response
   * @param onFailure the callback function to invoke on a failed response
   * @param model the model to use for generating text (default: "gpt-3.5-turbo")
   */
  fun sendRequest(
      prompt: String,
      onSuccess: (String) -> Unit,
      onFailure: (IOException) -> Unit,
      model: String = "gpt-3.5-turbo",
  ) {
    // Create the JSON body using Gson
    val messageObject =
        JsonObject().apply {
          addProperty("role", "user")
          addProperty("content", prompt)
        }

    val messagesArray = JsonArray().apply { add(messageObject) }

    val jsonBody =
        JsonObject().apply {
          addProperty("model", model)
          add("messages", messagesArray)
          addProperty("temperature", 0.2)
        }

    // Convert the JSON body to a string
    val requestBody = jsonBody.toString()

    // Build the request
    val request =
        Request.Builder()
            .url(endpoint)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

    client
        .newCall(request)
        .enqueue(
            object : okhttp3.Callback {
              override fun onFailure(call: okhttp3.Call, e: IOException) {
                onFailure(e) // Invoke the failure callback
              }

              override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val body = response.body?.string().orEmpty()
                onSuccess(body) // Invoke the success callback
              }
            })
  }
}
