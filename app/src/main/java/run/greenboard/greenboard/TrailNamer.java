package run.greenboard.greenboard;

import android.content.Intent;
import android.os.Parcelable;
import android.support.test.espresso.core.deps.dagger.internal.DoubleCheckLazy;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class TrailNamer extends AppCompatActivity {

    HTTPConnect httpConnect;
    Bundle bundle;
    String encodedTrail;
    Double headLat;
    Double headLng;
    String trailName;
    EditText trailNameEditText;
    Button saveTrailBtn;
    Button cancelBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_namer);

        initializeVariables();
        trailNameEditText = (EditText)findViewById(R.id.trailNameEditText);

        //pull information from previous intent on unsaved trail
        bundle = getIntent().getExtras();
        headLat = bundle.getDouble("Head_Lat");
        headLng = bundle.getDouble("Head_Lng");
        encodedTrail = bundle.getString("encoded_trail");

        trailName = trailNameEditText.getText().toString();



        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeView();
            }
        });

        saveTrailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailName = trailNameEditText.getText().toString();
                saveTrail(encodedTrail, headLat, headLng, trailName);
                changeView();
            }
        });
    }

    private void changeView(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }
    private void initializeVariables() {
        httpConnect = new HTTPConnect();
        saveTrailBtn = (Button) findViewById(R.id.save_trail_button);
        cancelBtn = (Button) findViewById(R.id.Cancel  );
    }

    // save trail to database
    void saveTrail(String trail, Double headLat, Double headLng , String trailName) {
        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/WriteTrailToDB/?trailName=" + trailName + "&lat=" + headLat + "&lng=" + headLng + "&trailObj=" + trail + "&key=" + "abc123";
        String responseBody = httpConnect.getSynchronusRequest(url);

        try {
            JSONObject jobject = new JSONObject(responseBody);
            jobject = jobject.getJSONObject("args");

            if (!jobject.isNull("success")) { //if successful
                Toast.makeText(TrailNamer.this, "Trail Added Successfully", Toast.LENGTH_SHORT).show();
            } else { //
                Toast.makeText(TrailNamer.this, "Trail save error", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
