package com.github.onlynotesswent.ui.navigation



import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.github.onlynotesswent.model.note.ImplementationNoteRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq



class TestingTests2 {
    private lateinit var noteRepository: ImplementationNoteRepository
    private lateinit var navigationDestination: NavDestination
    private lateinit var navHostController: NavHostController
    private lateinit var navigationActions: NavigationActions

    @Before
    fun setUp() {
        noteRepository = mock(ImplementationNoteRepository::class.java)
        navigationDestination = mock(NavDestination::class.java)
        navHostController = mock(NavHostController::class.java)
        navigationActions = NavigationActions(navHostController)
    }

    @Test
    fun navigateToCallsController() {
        navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
        verify(navHostController).navigate(eq(Route.OVERVIEW), any<NavOptionsBuilder.() -> Unit>())

        navigationActions.navigateTo(Screen.AUTH)
        verify(navHostController).navigate(Screen.AUTH)
    }
}