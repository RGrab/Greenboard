package run.greenboard.greenboard;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import static android.media.tv.TvContract.Programs.Genres.decode;

public class TrailProfile extends AppCompatActivity implements OnMapReadyCallback {

    private String trailId;
    private String encodedTrail;
    private String[] trailPath;
    private List<LatLng> trailPoints;
    private String trailName;
    private HTTPConnect httpConnect;

    private Bundle bundle;

    private Button takeTrailBtn;
    private Button goBack;
    private TextView trailNameText;

    private PolylineOptions trailLine;
    private GoogleMap mGoogleMap;
    private Marker headMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_profile);

        httpConnect = new HTTPConnect();

        bundle = getIntent().getExtras();
        trailId = bundle.getString("trail_id");

        goBack =  (Button) findViewById(R.id.goBackBtn);
        takeTrailBtn =  (Button) findViewById(R.id.takeTrailBtn);
        trailNameText = (TextView) findViewById(R.id.trailName);
        trailPoints = new ArrayList<LatLng>();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        takeTrailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putStringArray("Path", trailPath);
                bundle.putString("trail_namegf", trailName);
                bundle.putString("trail_id", trailId);
                Intent intent = new Intent(getBaseContext(), GoOnSelectedTrail.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
            });
        getTrailInfo(trailId);

        initMap();
    }

    private void getTrailInfo(String id) {
        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/GetTrailById/?id=" + id + "&key=abc123";
        String responseBody = httpConnect.getSynchronusRequest(url);

        try {
            JSONObject jobject = new JSONObject(responseBody);
            JSONObject jsonTrail;
            jobject = jobject.getJSONObject("args");
            if (!jobject.isNull("success")) {
                jobject = new JSONObject(responseBody);
                jsonTrail = jobject.getJSONObject("result");
                trailName = jsonTrail.getString("trailName");
                encodedTrail = jsonTrail.getString("trailObj");
            }

            Log.w("trail Name", trailName);
            trailNameText.setText(trailName);

            //decode trail object
            trailPath = decode(encodedTrail);
            double lat = 0;
            double lng = 0;
            for (int i = 0; i < trailPath.length; ++i) {
                if (i == 0) {
                    lat = Double.parseDouble(trailPath[i].substring(11,trailPath[i].length()));
                } else if (i % 2 == 0) {
                    lat = Double.parseDouble(trailPath[i].substring(10,trailPath[i].length()));
                } else if (i+1 == trailPath.length) {
                    lng = Double.parseDouble(trailPath[i].substring(0,trailPath[i].length()-2));
                } else {
                    lng = Double.parseDouble(trailPath[i].substring(0,trailPath[i].length()-1));
                }

                if (i % 2 == 1 && i != 0) {
                    trailPoints.add(new LatLng(lat, lng));
                }
            }
            for (int i = 0; i < trailPoints.size(); ++i) {
                Log.d("dddddd",trailPoints.get(i)+"");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initMap() {

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.trailProfileFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        headMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(trailPoints.get(0)));
        goToLocation();
        displayTrail();

    }

    private void goToLocation() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(trailPoints.get(0),15);
        mGoogleMap.animateCamera(cameraUpdate);
    }

    void displayTrail(){
       PolylineOptions trailLineOptions = new PolylineOptions()
                .color(Color.rgb(118,219,141))
                .width(20);


        for(int i = 0 ; i < trailPoints.size(); i++){
            trailLineOptions.add(trailPoints.get(i));
        }

        mGoogleMap.addPolyline(trailLineOptions);
    }
}
