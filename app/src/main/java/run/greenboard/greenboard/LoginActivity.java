package run.greenboard.greenboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    EditText usernameInput, passInput;
    Button registerBtn;
    String fbEmail, fbUserId, fbName;
    Button loginBtn;
    HTTPConnect httpConnect;
    Toast toast;
    int writeStoragePermissionResult;
    int writeMapPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        initializeVariables();

        //If they push register button
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchActivities(LoginActivity.this, RegisterActivity.class, null);
            }
        });

        SharedPreferences prefs = getSharedPreferences("UserData", 0);

        if (isLoggedInWithGreenBoard(prefs)) {
            switchActivities(LoginActivity.this, MainActivity.class, null);
        } else if (isLoggedInWithFacebook(AccessToken.getCurrentAccessToken())) {
            switchActivities(LoginActivity.this, MainActivity.class, null);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passInput.getText().toString();
                if (isValidLogin(username, password)) {
                    processLoginWithGreenBoard(username, password);
                } else {
                    showToast("Invalid login credentials");
                }
            }
        });
        /*
         * Facebook Login
         */

        final LoginButton fbLoginBtn = (LoginButton) findViewById(R.id.facebookLoginBtn);

        fbLoginBtn.setReadPermissions("email");
        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                        Log.d("yyyyy","onCompleted was called");
                        //needed to store email and user id
                        fbEmail = user.optString("email");
                        fbUserId = user.optString("id");
                        fbName = user.optString("name");

                        SharedPreferences prefs = getSharedPreferences("UserData", 0);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("profile_picture");
                        editor.apply();
                        if (!isRegisteredWithFacebook(fbUserId)) {
                            processFacebookData(fbUserId, fbEmail, fbName);
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email");
                request.setParameters(parameters);
                Log.d("yyyyy","executeAsync is about to be called");
                request.executeAsync();
                Log.d("yyyyy","executeAsync was called");

                switchActivities(LoginActivity.this, MainActivity.class, null);
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK LOGIN", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FACEBOOK LOGIN", "facebook:onError", error);
            }
        });
    }

    /*
     * Enable callbackManager to retrieve data from Facebook login button
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    void processLoginWithGreenBoard(String username, String password) {
        //Check database for this info before logging in here
        /////
        //Use refactored HTTPConnect class to connect to our server via that URL.
        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/Login/?username=" + username + "&password=" + password;
        String responseBody = httpConnect.getSynchronusRequest(url);
        try {
            JSONObject  jobject = new JSONObject(responseBody);
            jobject = jobject.getJSONObject("args");

            //if success message is null, either email or username is already taken
            if (jobject.isNull("success")) {
                if (!jobject.isNull("error")) {
                    showToast("Invalid login credentials");
                }
            } else {
                showToast("Login Success");

                jobject = new JSONObject(responseBody);
                jobject = jobject.getJSONObject("result");

                SharedPreferences prefs = getSharedPreferences("UserData", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("login_username", jobject.get("username").toString());
                editor.putString("login_api_key", jobject.get("api_key").toString());
                editor.remove("profile_picture");
                editor.putString("api_key", jobject.getString("api_key"));
                editor.putString("username", username);
                editor.apply();

                switchActivities(LoginActivity.this, MainActivity.class, null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    boolean isLoggedInWithFacebook(AccessToken token) {
        return token != null;
    }

    boolean isLoggedInWithGreenBoard(SharedPreferences preferences) {
        return preferences.contains("api_key");
    }

    //function to show toasts by only needing to pass through string message
    void showToast(String message) {
        toast = Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    //switches activities
    void switchActivities(Context firstActivity, Class endingActivity, HashMap parameters)
    {
        Intent intent = new Intent(firstActivity.getApplicationContext(), endingActivity);
        startActivity(intent);
        finish();
    }

    void initializeVariables (){
        //variables to get initiated onCreate()
        httpConnect = new HTTPConnect();
        registerBtn = (Button)findViewById(R.id.registerBtn); //register button
        usernameInput = (EditText) findViewById(R.id.username_input);//email input for regular login
        passInput = (EditText) findViewById(R.id.password_input); //password input for regular login
        loginBtn = (Button) findViewById(R.id.loginBtn);
        callbackManager = CallbackManager.Factory.create();
        writeStoragePermissionResult = 0;
        writeMapPermissions = 0;
    }

    boolean isValidLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty() || password.length() < 8)
            return false;
        return true;
    }

    void processFacebookData(String userId, String email, String name) {
        name = getFirstName(name);

        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/RegisterUserWithFB/?username=" + name + "&email=" + email + "&fbid=" + userId;
        String responseBody = httpConnect.getSynchronusRequest(url);

        try {
            JSONObject  jobject = new JSONObject(responseBody);
            jobject = jobject.getJSONObject("args");

            if (jobject.isNull("success")) {
                showToast("Facebook Register Failed");
            } else {
                if (isRegisteredWithFacebook(userId)) {
                    showToast("Facebook Login Successful");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void getFacebookProfilePicture(String id) {
        URL img_value = null;
        try {

            //get image from facebook graph api
            img_value = new URL("https://graph.facebook.com/" + id + "/picture?type=large");
            Bitmap profileIcon = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());

            //store profile icon onto sd card
            Log.d("profile picture", "in progress1");
            //save profile picture to sd card for trail maker usage
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            Log.d("profile picture", sdCardDirectory.toString());
            File image = new File(sdCardDirectory, "greenboard_profile_pin.JPEG");
            FileOutputStream outStream = new FileOutputStream(image);
            profileIcon.compress(Bitmap.CompressFormat.JPEG, 25, outStream);
            Log.d("profile picture", "in progress4");
            outStream.flush();
            Log.d("profile picture", "in progress5");
            outStream.close();

            //store location of image in sd card
            SharedPreferences prefs = getSharedPreferences("UserData", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("profile_picture", image.toString());
            editor.apply();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getFirstName(String name) {
        int i;
        for (i = 0; i < name.length(); ++i) {
            if (name.charAt(i) == ' ') break;
        }
        return name.substring(0, i);
    }

    boolean isRegisteredWithFacebook(String userId) {
        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/LoginWithFB/?fbid=" + userId;
        String responseBody = httpConnect.getSynchronusRequest(url);
        try {
            JSONObject jobject = new JSONObject(responseBody);
            jobject = jobject.getJSONObject("args");

            if (jobject.isNull("error")) {
                jobject = new JSONObject(responseBody);
                jobject = jobject.getJSONObject("result");

                SharedPreferences prefs = getSharedPreferences("UserData", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("api_key", jobject.getString("api_key"));
                editor.putString("username", jobject.getString("username"));
                editor.apply();

                getFacebookProfilePicture(userId);

                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
