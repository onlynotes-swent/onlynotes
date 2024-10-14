package com.github.onlynotesswent.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.onlynotesswent.MainActivity
import junit.framework.TestCase.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  @get:Rule val activityRule = ActivityScenarioRule(MainActivity::class.java)

  @Test
  fun testActivityLaunchesSuccessfully() {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        // Add assertions to verify the activity's state
        assertNotNull(activity)
      }
    }
  }
}
