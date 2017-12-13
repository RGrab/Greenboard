package run.greenboard.greenboard;
import android.os.IBinder;
import android.support.test.espresso.Root;
import android.support.test.rule.ActivityTestRule;
import android.view.WindowManager;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Babak on 11/14/16
 */

public class LoginActivityTest {
    TypeSafeMatcher<Root> matcher;

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(LoginActivity.class,true,true);

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

    public void setLoginInput(String username, String password) {
        onView(withId(R.id.username_input)).perform(typeText(username));
        onView(withId(R.id.password_input)).perform(typeText(password));
        closeSoftKeyboard();
    }

    @Test
    public void testLoginFunctionWithValidInput() {
        //input all new information for email and username
        setLoginInput("Brian","securepass");

        onView(withId(R.id.loginBtn)).perform(click());

        onView(withText("Login Success")).inRoot(matcher).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginFunctionWithInvalidUserAndPass() {
        //input all new information for email and username
        setLoginInput("u1u2u3","password123");

        onView(withId(R.id.loginBtn)).perform(click());

        onView(withText("Invalid login credentials")).inRoot(matcher).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginFunctionWithInvalidUser() {
        //input all new information for email and username
        setLoginInput("Brian","password123");

        onView(withId(R.id.loginBtn)).perform(click());

        onView(withText("Invalid login credentials")).inRoot(matcher).check(matches(isDisplayed()));
    }
}
