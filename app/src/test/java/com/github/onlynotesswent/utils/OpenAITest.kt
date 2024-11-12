package com.github.onlynotesswent.utils

import java.io.IOException
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class OpenAITest {
  // Mock dependencies
  @Mock private lateinit var mockClient: OkHttpClient
  @Mock private lateinit var mockCall: Call

  // Function to provide non-null arguments for Mockito when needed
  private fun <T> any(): T = Mockito.any<T>()

  private lateinit var openAI: OpenAI

  @Before
  fun setUp() {
    // Initialize Mockito and create mock instances for the test
    MockitoAnnotations.openMocks(this)
    mockClient = mock(OkHttpClient::class.java)
    openAI = OpenAI(client = mockClient) // Inject mock OkHttpClient into OpenAI class
  }

  @Test
  fun `test successful response triggers onSuccess`() {
    // Set up mock response
    val responseBody = "{\"choices\": [{\"message\": {\"content\": \"Hello World!\"}}]}"
    val response =
        Response.Builder()
            .request(Request.Builder().url("https://api.openai.com/v1/chat/completions").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("")
            .body(responseBody.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()

    // Configure mock behavior for OkHttp client
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockCall.enqueue(any())).thenAnswer { invocation ->
      // Invoke the onResponse callback to simulate a successful response
      val callback = invocation.getArgument<okhttp3.Callback>(0)
      callback.onResponse(mockCall, response)
    }

    // Trigger request and capture the response
    var successResponse: String? = null
    openAI.sendRequest("Hello", { successResponse = it }, { fail("onFailure called") })

    // Assertions
    assert(successResponse == responseBody)
  }

  @Test
  fun `test failure response triggers onFailure`() {
    // Set up exception to simulate a network error
    val exception = IOException("Network error")

    // Configure mock behavior for OkHttp client
    `when`(mockClient.newCall(any())).thenReturn(mockCall)
    `when`(mockCall.enqueue(any())).thenAnswer { invocation ->
      // Invoke the onFailure callback to simulate a failure response
      val callback = invocation.getArgument<okhttp3.Callback>(0)
      callback.onFailure(mockCall, exception)
    }

    // Trigger request and capture the response
    var failureException: IOException? = null
    openAI.sendRequest("Hello", { fail("onSuccess called") }, { failureException = it })

    // Assertions
    assert(failureException == exception)
  }
}
