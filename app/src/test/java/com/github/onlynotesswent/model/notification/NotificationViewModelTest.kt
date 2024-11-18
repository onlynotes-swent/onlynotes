package com.github.onlynotesswent.model.notification

import com.github.onlynotesswent.model.user.UserRepositoryFirestore
import com.github.onlynotesswent.model.user.UserViewModel
import com.google.firebase.Timestamp
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq


class NotificationViewModelTest {
    @Mock
    private lateinit var mockRepositoryFirestore:  NotificationRepositoryFirestore
    private lateinit var notificationViewModel: NotificationViewModel

    val testNotification = Notification(
        id = "1",
        title = "testTitle",
        body = "testBody",
        senderId = "1",
        receiverId = "2",
        timestamp = Timestamp.now(),
        read = false
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        notificationViewModel = NotificationViewModel(mockRepositoryFirestore)


    }


    @Test
    fun `test getNotificationById`() {


        notificationViewModel.getNotificationByReceiverId(
            "1",
            onSuccess = {   },
            onFailure = {
                fail()
            }
        )

        verify(mockRepositoryFirestore).getNotificationByReceiverId(
            any(), any(), any()
        )
    }

    @Test
    fun `test addNotification`() {
        notificationViewModel.addNotification(
            testNotification,
            onSuccess = {   },
            onFailure = {
                fail()
            }
        )

        verify(mockRepositoryFirestore).addNotification(
            eq(testNotification), any(), any()
        )
    }

    @Test
    fun `test deleteNotification`() {
        notificationViewModel.deleteNotification(
            "1",
            onSuccess = {   },
            onFailure = {
                fail()
            }
        )

        verify(mockRepositoryFirestore).deleteNotification(
            eq(testNotification.id), any(), any()
        )
    }

    @Test
    fun `test updateNotification`() {
        notificationViewModel.updateNotification(
            testNotification,
            onSuccess = {   },
            onFailure = {
                fail()
            }
        )

        verify(mockRepositoryFirestore).updateNotification(
            eq(testNotification), any(), any()
        )
    }



}
