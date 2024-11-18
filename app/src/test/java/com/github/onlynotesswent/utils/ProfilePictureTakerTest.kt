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
/*
class ProfilePictureTakerTest {
  @Mock private lateinit var mockActivity: ComponentActivity
  @Mock private lateinit var mockOnImageSelected: (Uri?) -> Unit
  @Mock private lateinit var mockActivityResultLauncher: ActivityResultLauncher<Intent>
  private lateinit var profilePictureTaker: ProfilePictureTaker

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)

    // Mock the activity result launcher registration
    `when`(
            mockActivity.registerForActivityResult(
                any(ActivityResultContracts.StartActivityForResult::class.java),
                any<ActivityResultCallback<ActivityResult>>()))
        .thenReturn(mockActivityResultLauncher)

    // Initialize ProfilePictureTaker with mocked components
    profilePictureTaker = ProfilePictureTaker(mockActivity).apply { init() }
    profilePictureTaker.setOnImageSelected { mockOnImageSelected }
  }

  @Test
  fun initRegistersActivityResultLauncherTest() {
    // Verify that the activity result launcher is registered
    verify(mockActivity)
        .registerForActivityResult(
            any<ActivityResultContract<Intent, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>())
  }

  @Test
  fun `pickImage should launch image picker intent`() {
    // Call pickImage to test if ImagePicker intent is created and launched
    profilePictureTaker.pickImage()

    // Verify that ImagePicker intent is launched
    verify(mockActivityResultLauncher).launch(any(Intent::class.java))
  }
}
*/