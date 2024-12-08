package com.github.onlynotesswent.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ProfilePictureTakerTest {
  @Mock private lateinit var mockActivity: ComponentActivity
  @Mock private lateinit var mockOnImageSelected: (Uri?) -> Unit
  @Mock private lateinit var mockActivityResultLauncher: ActivityResultLauncher<Intent>
  private lateinit var pictureTaker: PictureTaker

  private fun <T> nullableAny(type: Class<T>): T = any(type)

  private inline fun <reified T> nullableAnyReified(): T = any(T::class.java)

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Mock the activity result launcher registration
    `when`(
            mockActivity.registerForActivityResult(
                nullableAny(ActivityResultContracts.StartActivityForResult::class.java),
                nullableAnyReified<ActivityResultCallback<ActivityResult>>()))
        .thenReturn(mockActivityResultLauncher)

    // Initialize ProfilePictureTaker with mocked components
    pictureTaker = PictureTaker(mockActivity).apply { init() }
    pictureTaker.setOnImageSelected { mockOnImageSelected }
  }

  @Test
  fun initRegistersActivityResultLauncherTest() {
    // Verify that the activity result launcher is registered
    verify(mockActivity)
        .registerForActivityResult(
            nullableAnyReified<ActivityResultContract<Intent, ActivityResult>>(),
            nullableAnyReified<ActivityResultCallback<ActivityResult>>())
  }

  @Test
  fun `pickImage should launch image picker intent`() {
    // Call pickImage to test if ImagePicker intent is created and launched
    pictureTaker.pickImage()

    // Verify that ImagePicker intent is launched
    verify(mockActivityResultLauncher).launch(nullableAny(Intent::class.java))
  }
}
