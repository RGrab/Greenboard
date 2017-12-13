package run.greenboard.greenboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import android.content.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.facebook.AccessToken;

import junit.framework.Assert;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * Local Login Acitivty test class - Created by Babak Chehraz on 11/22/2016
 */
public class LoginActivityUnitTest {

    LoginActivity loginActivity;
    String username;
    String password;

    @Before
    public void init() {
        loginActivity = new LoginActivity();
    }

    @Test
    public void testIsLoggedInWithFaceboookFunctionWithNullAccessToken() {
        AccessToken accessToken = null;

        assertThat(false,is(equalTo(loginActivity.isLoggedInWithFacebook(accessToken))));
    }

    @Test
    public void testIsLoggedInWithFacebookFuncWithAccessTokenNotNull() { //access token not null
        AccessToken accessToken = new AccessToken("12345", "0", "12345", null, null, null, null, null);

        assertThat(true,is(equalTo(loginActivity.isLoggedInWithFacebook(accessToken))));
    }

    @Test
    public void testIfLoginValidWithVariousInput() {
        username = "test";
        password = "fail?";

        assertThat(false,is(equalTo(loginActivity.isValidLogin(username, password))));
    }

    @Test
    public void checkValidLoginWithGoodCredentials() {
        username = "helloimbob";
        password = "nicetomeetyoubob";

        assertThat(true,is(equalTo(loginActivity.isValidLogin(username, password))));
    }

    @Test
    public void checkValidLoginWithNoUsername() {
        username = "";
        password = "longpassword";

        assertThat(false,is(equalTo(loginActivity.isValidLogin(username, password))));
    }

    @Test
    public void checkValidLoginWithNoPassword() {
        username = "validusername";
        password = "";

        assertThat(false,is(equalTo(loginActivity.isValidLogin(username, password))));
    }

    @Test
    public void checkIfGetFirstNameWorks() {
        String name = "Bobby Brown";

        assertThat("Bobby", is(equalTo(loginActivity.getFirstName(name))));
    }

    @Test
    public void checkIfGetFirstNameWorksWithMiddleName() {
        String name = "John Smith Jones";

        assertThat("John", is(equalTo(loginActivity.getFirstName(name))));
    }

    @Test
    public void isLoggedInWithGreenBoardTest1() {
        SharedPreferences prefs = mock(SharedPreferences.class);

        when(prefs.contains("api_key")).thenReturn(true);

        assertThat(true, is(equalTo(loginActivity.isLoggedInWithGreenBoard(prefs))));
    }

    @Test
    public void isLoggedInWithGreenBoardTest2() {
        SharedPreferences prefs = mock(SharedPreferences.class);

        when(prefs.contains("api_key")).thenReturn(false);

        assertThat(false, is(equalTo(loginActivity.isLoggedInWithGreenBoard(prefs))));
    }
}