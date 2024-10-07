package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp
import org.junit.Test
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame

class UserTest {

    @Test
    fun `test user creation`() {
        val user = User(
            name = "User",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0
        )

        assertEquals("User", user.name)
        assertEquals("example@gmail.com", user.email)
        assertEquals("1", user.uid)
        assertEquals(0.0, user.rating)
    }

    @Test
    fun `test user equality`() {
        val timestamp = Timestamp.now()
        val user1 = User(
            name = "User",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = timestamp,
            rating = 0.0
        )
        val user2 = User(
            name = "User",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = timestamp,
            rating = 0.0
        )

        assertEquals(user1, user2)
    }

    @Test
    fun `test user inequality`() {
        val user1 = User(
            name = "User1",
            email = "example1@gmail.com",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0
        )
        val user2 = User(
            name = "User2",
            email = "example2@gmail.com",
            uid = "2",
            dateOfJoining = Timestamp.now(),
            rating = 0.0
        )

        assertNotSame(user1, user2)
    }
}