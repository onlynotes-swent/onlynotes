package com.github.onlynotesswent.model.user

import com.google.firebase.Timestamp
import java.util.Date
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

class UserTest {

  @Test
  fun `test user creation`() {
    val user =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)

    assertEquals("User", user.firstName)
    assertEquals("Name", user.lastName)
    assertEquals("username", user.userName)
    assertEquals("example@gmail.com", user.email)
    assertEquals("1", user.uid)
    assertEquals(0.0, user.rating)
  }

  @Test
  fun `test user equality`() {
    val timestamp = Timestamp.now()
    val user1 =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = timestamp,
            rating = 0.0)
    val user2 =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = timestamp,
            rating = 0.0)

    assertEquals(user1, user2)
  }

  @Test
  fun `test user inequality`() {
    val user1 =
        User(
            firstName = "User1",
            lastName = "Name",
            userName = "username",
            email = "example1@gmail.com",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    val user2 =
        User(
            firstName = "User2",
            lastName = "Name",
            userName = "username",
            email = "example2@gmail.com",
            uid = "2",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)

    assertNotSame(user1, user2)
  }

  @Test
  fun `test user friends creation`() {
    val friends = Friends(following = listOf("1", "2"), followers = listOf("3", "4"))

    assertEquals(listOf("1", "2"), friends.following)
    assertEquals(listOf("3", "4"), friends.followers)

    val emptyFriends = Friends()
    assertEquals(emptyList<String>(), emptyFriends.following)
    assertEquals(emptyList<String>(), emptyFriends.followers)
  }

  @Test
  fun `test user handle`() {
    var user =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "email",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    assertEquals("@username", user.userHandle())
    user =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "",
            email = "email",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    assertEquals("@unknown", user.userHandle())
  }

  @Test
  fun `test user full name`() {
    val user =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "email",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    assertEquals("User Name", user.fullName())
  }

  @Test
  fun `test user date to string`() {
    val user =
        User(
            firstName = "User",
            lastName = "Name",
            userName = "username",
            email = "email",
            uid = "1",
            dateOfJoining = Timestamp(date = Date(0)),
            rating = 0.0)
    assertEquals("1/1/1970", user.dateToString())
  }
}
