package com.github.onlynotesswent.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.OnlyNotes
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnlyNotesTest {

  @get:Rule val activityRule = ActivityScenarioRule(OnlyNotes::class.java)

  @Test
  fun testActivityLaunchesSuccessfully() {
    ActivityScenario.launch(OnlyNotes::class.java).use { scenario ->
      scenario.onActivity { activity ->
        // Add assertions to verify the activity's state
        assertNotNull(activity)
      }
    }
  }
}
