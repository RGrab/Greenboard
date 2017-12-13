package run.greenboard.greenboard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    Button regBtn;
    EditText emailInput;
    EditText userInput;
    EditText passInput;
    EditText confPassInput;
    Toast toast;
    HTTPConnect httpConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //init variables
        initializeVariables();
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String registerInputFeedback = verifyRegisterInput();
                if (registerInputIsSuccess(registerInputFeedback)) {
                    String responseBody = httpConnect.getSynchronusRequest(registerInputFeedback);
                    try {
                        JSONObject  jobject = new JSONObject(responseBody);
                        jobject = jobject.getJSONObject("args");

                        //if success message is null, either email or username is already taken
                        if (jobject.isNull("success")) {
                            if (!jobject.isNull("email")) {
                                Log.d("JSONARRAY - Email", jobject.get("email").toString());
                                showToast(jobject.get("email").toString());
                            }
                            if (!jobject.isNull("username")) {
                                Log.d("JSONARRAY - Username", jobject.get("username").toString());
                                showToast(jobject.get("username").toString());
                            }
                        } else {
                            showToast(jobject.get("success").toString());

                            Intent intent = new Intent(RegisterActivity.this.getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast(registerInputFeedback);
                }




            }
        });
    }

    //Return URL if register input is good to go
    private String verifyRegisterInput() {
        String password = passInput.getText().toString();
        String confirmPassword = confPassInput.getText().toString();
        String email = emailInput.getText().toString();
        String username = userInput.getText().toString();

        if (email.isEmpty() || !email.contains("@")) return "Please enter a valid email";
        if (username.isEmpty()) return "Enter a username";
        if (password.length() < 8) return "Password requires 8 characters";
        if (!password.equals(confirmPassword)) return "Passwords don't match!";

        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/RegisterUser/?username=" + username + "&password=" + password + "&email=" + email;
        return url;
    }

    private boolean registerInputIsSuccess(String feedback) {
        return feedback.contains("http");
    }

    void initializeVariables()
    {
        emailInput = (EditText) findViewById(R.id.regEmailInput);
        userInput = (EditText) findViewById(R.id.regUserInput);
        passInput = (EditText) findViewById(R.id.regPassInput);
        confPassInput = (EditText) findViewById(R.id.regPassCInput);
        regBtn = (Button) findViewById(R.id.regBtnSubmit);
        httpConnect = new HTTPConnect();
    }


    void showToast(String message) {
        toast = Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}