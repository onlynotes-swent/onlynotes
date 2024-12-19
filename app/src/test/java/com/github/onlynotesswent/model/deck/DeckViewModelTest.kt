package com.github.onlynotesswent.model.deck

import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeckViewModelTest {
  @Mock private lateinit var mockDeckRepository: DeckRepository
  private lateinit var deckViewModel: DeckViewModel

  private val testDeck: Deck =
      Deck(
          id = "1",
          name = "Deck",
          userId = "2",
          folderId = "3",
          visibility = Visibility.PUBLIC,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("4", "5")
      )

  private val testDeckFriends: Deck =
      Deck(
          id = "2",
          name = "Deck2",
          userId = "3",
          folderId = "4",
          visibility = Visibility.FRIENDS,
          lastModified = Timestamp.now(),
          flashcardIds = listOf("6", "7")
      )

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    deckViewModel = DeckViewModel(mockDeckRepository)
    `when`(mockDeckRepository.init(any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(0)
      onSuccess()
    }
    `when`(mockDeckRepository.getDecksFrom(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Deck>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testDeck))
    }
    `when`(mockDeckRepository.getDecksFromFollowingList(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Deck>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testDeckFriends))
    }
    `when`(mockDeckRepository.getDecksByFolder(any(), any(), any())).thenAnswer {
      val onSuccess: (List<Deck>) -> Unit = it.getArgument(1)
      onSuccess(listOf(testDeck))
    }
    `when`(mockDeckRepository.getDeckById(any(), any(), any())).thenAnswer {
      val onSuccess: (Deck) -> Unit = it.getArgument(1)
      onSuccess(testDeck)
    }
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }
    `when`(mockDeckRepository.deleteDeck(any(), any(), any())).thenAnswer {
      val onSuccess: () -> Unit = it.getArgument(1)
      onSuccess()
    }
  }

  @Test
  fun `init calls repository`() {
    verify(mockDeckRepository, timeout(1000)).init(any())
  }

  @Test
  fun `selectDeck updates selectedDeck`() {
    deckViewModel.selectDeck(testDeck)
    assertEquals(testDeck, deckViewModel.selectedDeck.value)
  }

  @Test
  fun `getNewUid works`() {
    val testUid = "1"
    `when`(mockDeckRepository.getNewUid()).thenReturn(testUid)
    assertEquals(testUid, deckViewModel.getNewUid())
  }

  @Test
  fun `getDecksFrom calls repository`() {
    var wasCalled = false
    deckViewModel.getDecksFrom(testDeck.userId, { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository).getDecksFrom(eq(testDeck.userId), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `getDecksFromFollowingList calls repository`() {
    var wasCalled = false
    deckViewModel.getDecksFromFollowingList(
        listOf(testDeckFriends.userId), { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository)
        .getDecksFromFollowingList(eq(listOf(testDeckFriends.userId)), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `getDecksFromFollowingList fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.getDecksFromFollowingList(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.getDecksFromFollowingList(
        listOf(testDeck.userId), { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).getDecksFromFollowingList(eq(listOf(testDeck.userId)), any(), any())
    assertEquals(testException, exceptionThrown)
  }

  @Test
  fun `getDecksFrom fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.getDecksFrom(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.getDecksFrom(
        testDeck.userId, { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).getDecksFrom(eq(testDeck.userId), any(), any())
    assertEquals(testException, exceptionThrown)
  }

  @Test
  fun `getDecksByFolder calls repository`() {
    var wasCalled = false
    deckViewModel.getDecksByFolder(
        testDeck.folderId!!, { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository).getDecksByFolder(eq(testDeck.folderId!!), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `getDecksByFolder fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.getDecksByFolder(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.getDecksByFolder(
        testDeck.folderId!!, { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).getDecksByFolder(eq(testDeck.folderId!!), any(), any())
    assertEquals(testException, exceptionThrown)
  }

  @Test
  fun `getDeckById calls repository`() {
    var wasCalled = false
    deckViewModel.getDeckById(testDeck.id, { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository).getDeckById(eq(testDeck.id), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `getDeckById fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.getDeckById(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.getDeckById(testDeck.id, { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).getDeckById(eq(testDeck.id), any(), any())
    assertEquals(testException, exceptionThrown)
  }

  @Test
  fun `updateDeck calls repository`() {
    var wasCalled = false
    deckViewModel.updateDeck(testDeck, { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository).updateDeck(eq(testDeck), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `updateDeck fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.updateDeck(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.updateDeck(testDeck, { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).updateDeck(eq(testDeck), any(), any())
    assertEquals(testException, exceptionThrown)
  }

  @Test
  fun `deleteDeck calls repository`() {
    var wasCalled = false
    deckViewModel.deleteDeck(testDeck, { wasCalled = true }, { fail("Should not fail") })
    verify(mockDeckRepository).deleteDeck(eq(testDeck), any(), any())
    assert(wasCalled)
  }

  @Test
  fun `deleteDeck fails`() {
    val testException = Exception("Test exception")
    var exceptionThrown: Exception? = null
    `when`(mockDeckRepository.deleteDeck(any(), any(), any())).thenAnswer {
      val onFailure: (Exception) -> Unit = it.getArgument(2)
      onFailure(testException)
    }
    deckViewModel.deleteDeck(testDeck, { fail("Should not succeed") }, { exceptionThrown = it })
    verify(mockDeckRepository).deleteDeck(eq(testDeck), any(), any())
    assertEquals(testException, exceptionThrown)
  }
}
