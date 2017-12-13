package run.greenboard.greenboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.InjectEventSecurityException;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.Root;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;
import android.widget.Toast;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegisterActivityTest {

    TypeSafeMatcher<Root> matcher;

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(RegisterActivity.class,true,true);

    //Initializes matcher to a toast matcher
    @Before
    public void setToastMatcher() {
        matcher = new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root item) {
                int type = item.getWindowLayoutParams().get().type;
                if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                    IBinder windowToken = item.getDecorView().getWindowToken();
                    IBinder appToken = item.getDecorView().getApplicationWindowToken();
                    if (windowToken == appToken) {
                        //means this window isn't contained by any other windows.
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is toast");
            }
        };
    }

    public void setRegisterInput(String email, String user) {
        onView(withId(R.id.regEmailInput)).perform(typeText(email));
        onView(withId(R.id.regUserInput)).perform(typeText(user));
        closeSoftKeyboard();
        onView(withId(R.id.regPassInput)).perform(typeText("password123"));
        closeSoftKeyboard();
        onView(withId(R.id.regPassCInput)).perform(typeText("password123"));
        closeSoftKeyboard();
    }

    @Test
    public void testRegisterFunctionalityWithNewAccountDetails() {
        int randNum = (int)(Math.random() * 10000000);
        //input all new information for email and username
        setRegisterInput("test_" + randNum + "@greenboard.com", "test_" + randNum);

        onView(withId(R.id.regBtnSubmit)).perform(click());

        onView(withText("Registered Successfully")).inRoot(matcher).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterFunctionWithExistingAccountEmail() {
        int randNum = (int)(Math.random() * 10000000);
        //input an already taken email
        setRegisterInput("test@greenboard.com","test_" + randNum);

        onView(withId(R.id.regBtnSubmit)).perform(click());


        onView(withText("Email is already registered.")).inRoot(matcher).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisterFunctionWithExistingAccountUsername() {
        int randNum = (int)(Math.random() * 10000000);
        //input an already taken username
        setRegisterInput("test_" + randNum + "@greenboard.com","Brian");

        onView(withId(R.id.regBtnSubmit)).perform(click());

        onView(withText("Username is already registered.")).inRoot(matcher).check(matches(isDisplayed()));
    }
}