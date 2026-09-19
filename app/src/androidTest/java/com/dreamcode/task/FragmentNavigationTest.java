package com.dreamcode.task;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FragmentNavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testFabNavigatesToAddNote() {
        // Click the FAB to navigate to SecondFragment (Add/Edit note)
        onView(withId(R.id.fab)).perform(click());

        // Verify that the title input is displayed in SecondFragment
        onView(withId(R.id.edit_text_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testSettingsNavigation() {
        // Open overflow menu or navigate to settings
        Espresso.openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(ViewMatchers.withText(R.string.action_settings)).perform(click());

        // Verify settings UI is displayed
        onView(withId(R.id.edit_text_pin)).check(matches(isDisplayed()));
    }
}
