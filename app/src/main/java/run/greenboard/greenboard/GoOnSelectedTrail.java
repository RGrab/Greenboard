package run.greenboard.greenboard;
import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class GoOnSelectedTrail extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private double lat;
    private double lon;
    private boolean athead = false;
    private PolylineOptions completedTrailOptions = new PolylineOptions()
            .color(Color.rgb(255,0,0))
            .width(30);
    private TextView timerText;
    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int hours = 0;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    private Handler handler = new Handler();
    private int pointcounter;
    private double reuploadLat;
    private double reuploadLng;
    private double nextPointLat;
    private double nextPointLon;
    private String[] trailPath;
    private List<LatLng> trailPoints;
    TextView HeadWarning;
    Bitmap icon;


    private Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_on_selected_trail);
        bundle = getIntent().getExtras();

        bundle = getIntent().getExtras();
        if (bundle.getStringArray("Path") != null && bundle.getString("trail_id") != null) {
            trailPath = new String[bundle.getStringArray("Path").length];
            trailPath = bundle.getStringArray("Path").clone();
            trailPoints = new ArrayList<LatLng>();

            timerText = (TextView) findViewById(R.id.timer);
            SharedPreferences prefs = getSharedPreferences("UserData", 0);

            if (prefs.contains("profile_picture")) {
                //File profilePic = new File(prefs.getString("profile_picture","No name defined"));
                icon = BitmapFactory.decodeFile(prefs.getString("profile_picture", "No name defined"));
                icon = getCircleBitmap(icon);
            } else {
                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.user_pin_icon);
            }

            if (googleServicesAvailable()) {
                initMap();
                initLocationRequest();
                initGoogleApi();
            }
            decodePath();
            HeadWarning = (TextView) findViewById(R.id.trailHeadWarning);
            pointcounter =  trailPoints.size();
        }
    }

    private void decodePath() {
        double lat = 0;
        double lng = 0;
        for (int i = 0; i < trailPath.length; ++i) {
            if (i == 0) {
                lat = Double.parseDouble(trailPath[i].substring(11, trailPath[i].length()));
            } else if (i % 2 == 0) {
                lat = Double.parseDouble(trailPath[i].substring(10, trailPath[i].length()));
            } else if (i + 1 == trailPath.length) {
                lng = Double.parseDouble(trailPath[i].substring(0, trailPath[i].length() - 2));
            } else {
                lng = Double.parseDouble(trailPath[i].substring(0, trailPath[i].length() - 1));
            }

            if (i % 2 == 1 && i != 0) {
                trailPoints.add(new LatLng(lat, lng));
            }
        }
        for (int i = 0; i < trailPoints.size(); ++i) {
            Log.d("dddddd", trailPoints.get(i) + "");
        }
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

    public double calculateDistance(double clat, double clon){
        double earthd = 6371000;
        double lat1Rad = toRadians(lat);
        double lat2Rad = toRadians(clat);
        double deltasig = toRadians(clat - lat);
        double deltalam = toRadians(clon - lon);
        double a = sin(deltasig/2) * sin(deltasig/2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltalam/2) * sin(deltalam/2);
        double c = 2 * atan2(sqrt(a),sqrt(1-a));
        return earthd * c;
    }

    //*****************************
    // trail maker specific functions
    //*****************************

    private void turnOnTimer() {
        starttime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);
    }

    private void turnOffTimer() {
        timeSwapBuff += timeInMilliseconds;
        handler.removeCallbacks(updateTimer);
    }

    public Runnable updateTimer = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            updatedtime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedtime / 1000);
            hours = mins / 60;
            mins = mins % 60;
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            timerText.setText("" + hours + ":" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            handler.postDelayed(this, 0);
        }

    };
    //*****************************
    // gps functions
    //*****************************
    private void initLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
    }

    private void initGoogleApi() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    private boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can Not Connect to Google Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Add permissions
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mLastLocation != null) {
            lat = mLastLocation.getLatitude();
            lon = mLastLocation.getLongitude();
        }
        requestLocationupdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO Add connection failed
    }

    private void requestLocationupdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            // TODO: Add permissions
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationupdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    //*****************************
    // map functions
    //*****************************

    void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        userMarker = mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(new LatLng(lat,lon)));
        displayTrail();
    }
    //****************
    // updates map on location changed
    //****************
    @Override
    public void onLocationChanged(Location location) {
        if(calculateDistance(lat, lon) > 5) {
            lat = location.getLatitude();
            lon = location.getLongitude();

            checktraillocation();
      }
        if(calculateDistance(reuploadLat , reuploadLng) > 10) {

            reuploadLat = location.getLatitude();
            reuploadLng = location.getLongitude();

        }
        goToLocation(lat,lon,20);
    }

    private void checktraillocation() {

        if(!athead){
            if(calculateDistance(trailPoints.get(0).latitude,trailPoints.get(0).longitude) < 10){
                athead = true;
                HeadWarning.setVisibility(View.INVISIBLE);
                turnOnTimer();

                completedTrailOptions.add(trailPoints.get(0));
                mGoogleMap.addPolyline(completedTrailOptions);
                pointcounter--;
                nextPointLat = trailPoints.get(trailPoints.size() - pointcounter).latitude;
                nextPointLon = trailPoints.get(trailPoints.size() - pointcounter).longitude;
            }
        }else if (pointcounter > 1 && calculateDistance(nextPointLat,nextPointLon) < 5){//more points
            completedTrailOptions.add(new LatLng(nextPointLat,nextPointLon ));
            mGoogleMap.addPolyline(completedTrailOptions);
            pointcounter--;
            nextPointLat = trailPoints.get(trailPoints.size() - pointcounter).latitude;
            nextPointLon = trailPoints.get(trailPoints.size() - pointcounter ).longitude;
            checktraillocation();
        }else if(pointcounter > 1 &&calculateDistance(nextPointLat,nextPointLon) >= 5){//trail end
            return;
        }else{
            turnOffTimer();
        }
    }

    void goToLocation(double latatude, double longitude, float zoom){
        LatLng Ll=  new LatLng(latatude, longitude);
        CameraUpdate camupdate = CameraUpdateFactory.newLatLngZoom(Ll,zoom);
        mGoogleMap.animateCamera(camupdate);
        CurrentLoacation(latatude, longitude);
    }

    //will be needed at some point.
    private void goToLocation(double latatude, double longitude){
        LatLng Ll=  new LatLng(latatude, longitude);
        CameraUpdate camupdate = CameraUpdateFactory.newLatLng(Ll);
        mGoogleMap.animateCamera(camupdate);
        CurrentLoacation(latatude, longitude);
    }

    //*****************
    // line drawing
    //*****************

    private void CurrentLoacation(double clat,double clon){
        userMarker.setPosition(new LatLng(clat, clon));
    }

    //Convert bitmap to a circular bitmap
    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        final float roundPxX = bitmap.getWidth()/2;
        final float roundPxY = bitmap.getHeight()/2;
        canvas.drawCircle(roundPxX, roundPxY, bitmap.getHeight()/2, new Paint());
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        bitmap.recycle();
        return output;
    }
}
