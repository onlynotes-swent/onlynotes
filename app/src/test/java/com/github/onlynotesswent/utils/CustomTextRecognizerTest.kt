package com.github.onlynotesswent.utils

import org.mockito.Mockito.*


/*
@RunWith(RobolectricTestRunner::class)
@Suppress("UNCHECKED_CAST")
class CustomTextRecognizerTest {

  @Mock private lateinit var mockActivity: ComponentActivity
  @Mock private lateinit var mockTextRecognizer: TextRecognizer
  @Mock private lateinit var mockActivityResultLauncher: ActivityResultLauncher<String>

  private lateinit var customTextRecognizer: CustomTextRecognizer

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Mock the registration of activity result launcher in the activity
    `when`(
            mockActivity.registerForActivityResult(
                any<ActivityResultContract<String, ActivityResult>>(),
                any<ActivityResultCallback<ActivityResult>>()))
        .thenReturn(mockActivityResultLauncher)

    customTextRecognizer = CustomTextRecognizer(mockActivity, mockTextRecognizer)
  }

  @Test
  fun initRegistersActivityResultLauncherTest() {
    customTextRecognizer.init()

    // Verify that the activity result launcher is registered
    verify(mockActivity)
        .registerForActivityResult(
            any<ActivityResultContract<String, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>())
  }

  @Test
  fun scanImageLogsErrorWhenNotInitializedTest() {
    customTextRecognizer.scanImage()

    // Get all the logs
    val logs = ShadowLog.getLogs()

    // Check for the error log that should be generated
    val errorLog =
        logs.find {
          it.type == Log.ERROR &&
              it.tag == CustomTextRecognizer.TAG &&
              it.msg == "Error: textRecognitionLauncher is not initialized"
        }
    assert(errorLog != null) { "Expected error log was not found!" }
  }

  @Test
  fun scanImageLaunchesImagePickerIntentTest() {
    customTextRecognizer.init()
    customTextRecognizer.scanImage()

    // Verify that the activity result launcher was launched with the correct MIME type
    verify(mockActivityResultLauncher).launch("image/*")
  }

  @Test
  fun scanImageHandlesActivityNotFoundExceptionTest() {
    // Simulate ActivityNotFoundException when launching the image picker
    `when`(mockActivityResultLauncher.launch("image/*"))
        .thenThrow(ActivityNotFoundException::class.java)

    mockStatic(Toast::class.java).use { toastMock ->
      val mockToast = mock(Toast::class.java)
      toastMock
          .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
          .thenReturn(mockToast)

      // Start the activity
      customTextRecognizer.init()
      customTextRecognizer.scanImage()

      // Get all the logs
      val logs = ShadowLog.getLogs()

      // Check for the error log that should be generated
      val errorLog =
          logs.find {
            it.type == Log.ERROR &&
                it.tag == CustomTextRecognizer.TAG &&
                it.msg == "Failed to launch gallery"
          }
      assert(errorLog != null) { "Expected error log was not found!" }

      // Verify that Toast.makeText() was called with the appropriate arguments
      toastMock.verify {
        Toast.makeText(eq(mockActivity), eq("Failed to launch gallery"), eq(Toast.LENGTH_LONG))
      }

      // Verify that Toast.show() was called on the returned Toast object
      verify(mockToast).show()
    }
  }
  /*
    @Test
    fun extractTextFromImageSuccessTest() {
      val mockUri = mock(Uri::class.java)
      val mockInputImage = mock(InputImage::class.java)
      val mockText = mock(Text::class.java)
      `when`(mockText.text).thenReturn("Test recognized text")

      // Create a task that simulates a successful recognition
      val successfulTask = mock(Task::class.java) as Task<Text>
      `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
        val listener = it.arguments[0] as OnSuccessListener<Text>
        listener.onSuccess(mockText)
        successfulTask
      }

      // Mock the text recognizer's processing result
      `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(successfulTask)

      // Mock the InputImage creation
      mockStatic(InputImage::class.java).use { inputImageMock ->
        inputImageMock
            .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
            .thenReturn(mockInputImage)

        // Trigger the text extraction process
        customTextRecognizer.init()
        customTextRecognizer.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), captor.capture())
        captor.value.onActivityResult(mockUri)

        // Verify that the text processing was triggered
        verify(mockTextRecognizer).process(mockInputImage)
      }
    }
  */
  @Test
  fun extractTextFromImageIOExceptionTest() {
    val mockUri = mock(Uri::class.java)

    // Mock the InputImage creation to throw an IOException
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenThrow(IOException("Test IO Exception"))

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)
        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        customTextRecognizer.init()
        customTextRecognizer.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), captor.capture())
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the error log that should be generated
        val errorLog =
            logs.find {
              it.type == Log.ERROR &&
                  it.tag == CustomTextRecognizer.TAG &&
                  it.msg == "Error reading image"
            }
        assert(errorLog != null) { "Expected error log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(
              eq(mockActivity), eq("Error reading image: Test IO Exception"), eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }

  @Test
  fun extractTextFromImageNoTextFoundTest() {
    val mockUri = mock(Uri::class.java)
    val mockInputImage = mock(InputImage::class.java)
    val mockText = mock(Text::class.java)
    `when`(mockText.text).thenReturn("")

    // Create a task that simulates a successful recognition
    val successfulTask = mock(Task::class.java) as Task<Text>
    `when`(successfulTask.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Text>
      listener.onSuccess(mockText)
      successfulTask
    }

    // Mock the text recognizer's processing result
    `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(successfulTask)

    // Mock the InputImage creation
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenReturn(mockInputImage)

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)
        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        customTextRecognizer.init()
        customTextRecognizer.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), captor.capture())
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the debug log that should be generated
        val debugLog =
            logs.find {
              it.type == Log.DEBUG &&
                  it.tag == CustomTextRecognizer.TAG &&
                  it.msg == "No text found in the image"
            }
        assert(debugLog != null) { "Expected warning log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(eq(mockActivity), eq("No text found in the image"), eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }

  @Test
  fun extractTextFromImageFailureRecognitionTest() {
    val mockUri = mock(Uri::class.java)
    val mockInputImage = mock(InputImage::class.java)

    // Create a task that simulates a failed recognition
    val failedTask = mock(Task::class.java) as Task<Text>
    `when`(failedTask.addOnSuccessListener(any())).thenReturn(failedTask) // No success listener
    `when`(failedTask.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception("Recognition failed"))
      failedTask
    }

    // Mock the text recognizer's processing result
    `when`(mockTextRecognizer.process(mockInputImage)).thenReturn(failedTask)

    // Mock the InputImage creation
    mockStatic(InputImage::class.java).use { inputImageMock ->
      inputImageMock
          .`when`<InputImage> { InputImage.fromFilePath(any(), eq(mockUri)) }
          .thenReturn(mockInputImage)

      mockStatic(Toast::class.java).use { toastMock ->
        val mockToast = mock(Toast::class.java)

        toastMock
            .`when`<Toast> { Toast.makeText(any<Context>(), any<String>(), any()) }
            .thenReturn(mockToast)

        // Trigger the text extraction process
        customTextRecognizer.init()
        customTextRecognizer.scanImage()

        // Simulate the image selection callback
        val captor =
            ArgumentCaptor.forClass(ActivityResultCallback::class.java)
                as ArgumentCaptor<ActivityResultCallback<Uri?>>
        verify(mockActivity)
            .registerForActivityResult(any<ActivityResultContracts.GetContent>(), captor.capture())
        captor.value.onActivityResult(mockUri)

        // Get all the logs
        val logs = ShadowLog.getLogs()

        // Check for the error log that should be generated
        val errorLog =
            logs.find {
              it.type == Log.ERROR &&
                  it.tag == CustomTextRecognizer.TAG &&
                  it.msg == "Text recognition failed"
            }
        assert(errorLog != null) { "Expected error log was not found!" }

        // Verify that Toast.makeText() was called with the appropriate arguments
        toastMock.verify {
          Toast.makeText(
              eq(mockActivity),
              eq("Text recognition failed: Recognition failed"),
              eq(Toast.LENGTH_LONG))
        }

        // Verify that Toast.show() was called on the returned Toast object
        verify(mockToast).show()
      }
    }
  }
}

*/
