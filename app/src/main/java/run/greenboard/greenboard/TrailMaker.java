package run.greenboard.greenboard;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
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
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.os.Handler;
import static android.media.tv.TvContract.Programs.Genres.encode;

public class TrailMaker extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private double lat;
    private double lon;
    private boolean TimerOnOff = false;
    private TextView timerText;
    private Button startStopBtn;
    private long starttime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedtime = 0L;
    private int t = 1;
    private int hours = 0;
    private int secs = 0;
    private int mins = 0;
    private int milliseconds = 0;
    private Handler handler = new Handler();
    private Double headLat = null;
    private Double headLng = null;

    Bitmap icon;
    PolylineOptions trailLineOptions;
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_trailmmaker, container, false);
        timerText = (TextView) view.findViewById(R.id.timer);
        startStopBtn = (Button) view.findViewById(R.id.startStopBtn);
        SharedPreferences prefs = getContext().getSharedPreferences("UserData", 0);

        if (prefs.contains("profile_picture")) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            icon = BitmapFactory.decodeFile(prefs.getString("profile_picture","No name defined"),opts);
            icon = getCircleBitmap(icon);
        } else {
            icon = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.user_pin_icon);
        }

        startStopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TimerOnOff) {
                    turnOnTimer();
                } else {
                    turnOffTimer();
                    initiateSaveTrail();
                }
            }
        });

        if (googleServicesAvailable()) {
            initMap();
            initLocationRequest();
            initGoogleApi();
        }
        return view;
    }
    //*****************************
    // trail maker specific functions
    //*****************************
    private void turnOnTimer() {
        startStopBtn.setText("stop trail");
        starttime = SystemClock.uptimeMillis();
        handler.postDelayed(updateTimer, 0);
        initTrailLineOptions();
        TimerOnOff = true;
    }

    private void turnOffTimer() {
        startStopBtn.setText("start trail");
        timeSwapBuff += timeInMilliseconds;
        handler.removeCallbacks(updateTimer);
        TimerOnOff = false;
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
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    private boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(getContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(getActivity(), isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "Can Not Connect to Google Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: ADD permission check
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

    }

    private void requestLocationupdates() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            // TODO: ADD permission check
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationupdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }
    //*****************************
    // map functions
    //*****************************
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        userMarker = mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(new LatLng(lat,lon)));
    }
    //****************
    // updates map on location changed
    //****************
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        //set head location for the trail
        if(headLat == null || headLng == null){
            headLat = lat;
            headLng = lon;
        }
        goToLocation(lat,lon,50);
        if(TimerOnOff) {
            addpoint(lat, lon);
        }
    }

    private void goToLocation(double latatude, double longitude, float zoom){
        LatLng Ll=  new LatLng(latatude, longitude);
        CameraUpdate camupdate = CameraUpdateFactory.newLatLngZoom(Ll,zoom);
        mGoogleMap.animateCamera(camupdate);
        CurrentLoacation(latatude, longitude);
    }

    private void goToLocation(double latatude, double longitude){
        LatLng Ll=  new LatLng(latatude, longitude);
        CameraUpdate camupdate = CameraUpdateFactory.newLatLng(Ll);
        mGoogleMap.animateCamera(camupdate);
        CurrentLoacation(latatude, longitude);
    }

    //*****************
    // line drawing
    //*****************

    private void initTrailLineOptions(){
        trailLineOptions = new PolylineOptions()
                .color(Color.rgb(118,219,141))
                .width(20);
        addPolyLineToMap();
    }

    private void addpoint(double clat, double clon){
        trailLineOptions.add(new LatLng(clat,clon));
        mGoogleMap.addPolyline(trailLineOptions);
    }

    private void addPolyLineToMap(){
        mGoogleMap.addPolyline(trailLineOptions);
    }

    private void initiateSaveTrail() {
        String encodedTrail = encode(String.valueOf(trailLineOptions.getPoints()));
        Intent intent = new Intent(getContext(),TrailNamer.class);
        intent.putExtra("Head_Lat",headLat);
        intent.putExtra("Head_Lng",headLng);
        intent.putExtra("encoded_trail",encodedTrail);
        startActivity(intent);
    }

    private void CurrentLoacation(double clat,double clon){
        Log.w("thing","lat" + clat + " lon :" + clon );
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
        final RectF rectF = new RectF(rect);
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
