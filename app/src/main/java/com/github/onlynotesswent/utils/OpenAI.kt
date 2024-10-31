package com.github.onlynotesswent.utils

import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** A class that sends requests to the OpenAI API to generate text based on a given prompt. */
class OpenAI {
  private val apiKey: String =
      System.getenv("OPEN_AI_API_KEY") ?: throw IllegalStateException("API Key is missing")

  /**
   * Sends a POST request to the OpenAI API to generate text based on the given prompt.
   *
   * @param prompt the prompt to generate text from
   * @param model the model to use for generating text (default: "text-davinci-003")
   * @return the generated text
   */
  fun sendRequest(prompt: String, model: String = "text-davinci-003"): String {
    // Create a connection to the OpenAI API
    val url = URL("https://api.openai.com/v1/completions")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Authorization", "Bearer $apiKey")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val requestBody =
        """
            {
                "model": "$model",
                "prompt": "$prompt",
                "max_tokens": 50
            }
        """
            .trimIndent()

    // Send the request
    connection.outputStream.use { os ->
      os.write(requestBody.toByteArray())
      os.flush()
    }

    // Read the response
    return if (connection.responseCode == HttpURLConnection.HTTP_OK) {
      BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
        val response = reader.readText()
        val json = JsonParser.parseString(response).asJsonObject
        json.get("choices").asJsonArray[0].asJsonObject.get("text").asString.trim()
      }
    } else {
      BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
        "Error: ${connection.responseCode} ${reader.readText()}"
      }
    }
  }
}
