package com.github.onlynotesswent.model.scanner

import android.content.IntentSender
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import com.github.onlynotesswent.MainActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScannerTest {

  @Mock
  private lateinit var mockMainActivity: MainActivity
  @Mock
  private lateinit var mockDocScanner: GmsDocumentScanner
  @Mock
  private lateinit var mockTaskIntentSender: Task<IntentSender>
  @Mock
  private lateinit var mockIntentSender: IntentSender
  @Mock
  private lateinit var mockActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

  private lateinit var scanner: Scanner

  @Before
  fun setUp() {
      MockitoAnnotations.openMocks(this)

    Mockito.`when`(
        mockMainActivity.registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>()
        )
    )
        .thenReturn(mockActivityResultLauncher)
    scanner = Scanner(mockMainActivity, mockDocScanner)
  }

  @Test
  fun initTest() {
    // Test if scanner is correctly initialized, and if the activity result launcher is registered
    scanner.init()
    verify(mockMainActivity)
        .registerForActivityResult(
            any<ActivityResultContract<IntentSenderRequest, ActivityResult>>(),
            any<ActivityResultCallback<ActivityResult>>()
        )
  }

  @Test
  fun scanSuccessTest() {
    scanner.init()

    // Mock the creation of the IntentSender Task and simulate a successful result
    Mockito.`when`(mockDocScanner.getStartScanIntent(mockMainActivity)).thenReturn(mockTaskIntentSender)
    Mockito.`when`(mockTaskIntentSender.addOnSuccessListener(any())).thenAnswer {
      (it.arguments[0] as OnSuccessListener<IntentSender>).onSuccess(mockIntentSender)
      mockTaskIntentSender
    }

    scanner.scan()

    verify(mockDocScanner).getStartScanIntent(mockMainActivity)
    verify(mockTaskIntentSender).addOnSuccessListener(any())
    verify(mockActivityResultLauncher).launch(any())
  }

  @Test
  fun scanFailTest() {
    scanner.init()

    Mockito.`when`(mockDocScanner.getStartScanIntent(mockMainActivity)).thenReturn(mockTaskIntentSender)
    Mockito.`when`(mockTaskIntentSender.addOnSuccessListener(any())).thenReturn(mockTaskIntentSender)

    scanner.scan()
    verify(mockDocScanner).getStartScanIntent(mockMainActivity)
    verify(mockTaskIntentSender).addOnFailureListener(any())
  }
}