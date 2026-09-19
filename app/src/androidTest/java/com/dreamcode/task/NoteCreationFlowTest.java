package com.dreamcode.task;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NoteCreationFlowTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCreateNoteAndVerifyInList() throws InterruptedException {
        String testTitle = "Test E2E Note " + System.currentTimeMillis();
        String testContent = "This is a test note created via automated UI testing.";

        // Tap FAB to open creation screen
        onView(withId(R.id.fab)).perform(click());

        // Type title and content
        onView(withId(R.id.edit_text_title)).perform(typeText(testTitle));
        closeSoftKeyboard();

        onView(withId(R.id.edit_text_content)).perform(typeText(testContent));
        closeSoftKeyboard();

        // Tap Save button
        onView(withId(R.id.button_save)).perform(click());

        // Give database and UI a moment to update and verify the note appears in list
        Thread.sleep(500);
        onView(withText(testTitle)).check(matches(isDisplayed()));
    }
}
