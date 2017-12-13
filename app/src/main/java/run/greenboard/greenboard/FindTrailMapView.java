package run.greenboard.greenboard;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class FindTrailMapView extends Fragment implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    HTTPConnect httpConnect;
    View view;
    Bitmap icon;

    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private double lat;
    private double lon;
    private double reuploadLat;
    private double reuploadLng;
    private List<Double> headLatPoints;
    private List<Double> headLngPoints;
    private List<String> trailNames;
    private List<String> trailIDs;



    public FindTrailMapView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_find_trail_map_view, container, false);

        SharedPreferences prefs = getContext().getSharedPreferences("UserData", 0);
        if (prefs.contains("profile_picture")) {
          icon = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.user_pin_icon);
        } else {
            icon = BitmapFactory.decodeResource(getContext().getResources(),R.mipmap.user_pin_icon);
        }

        if (googleServicesAvailable()) {
            initializeVariables();
            initLocationRequest();
            initGoogleApi();
            initMap();
        }
        return view;
    }
    //*****************************
    // FindTrailMapView Specific Functions
    //*****************************
    private void getAndSaveHeadPoints(){
        //get trails in 1x1 lat/lon square around user
        double minLat = lat - 1;
        double minLon = lon - 1;
        double maxLat = lat + 1;
        double maxLon = lon + 1;
        String url = "http://greenboard-env.us-west-2.elasticbeanstalk.com/api.php/GetTrailInArea/?minLat=" + minLat + "&minLng=" + minLon + "&maxLat=" + maxLat + "&maxLng=" + maxLon + "&key=abc123";
        String responseBody = httpConnect.getSynchronusRequest(url);
        try {
            JSONObject jobject = new JSONObject(responseBody);
            JSONArray trailsResults;
            JSONObject currentTrail;

            jobject = jobject.getJSONObject("args");
            if (!jobject.isNull("success")) { //successful response
                jobject = new JSONObject(responseBody);
                trailsResults = jobject.getJSONArray("result");

                //store trail heads
                for (int i = 0; i < trailsResults.length(); ++i) {
                    currentTrail = trailsResults.getJSONObject(i);
                    headLatPoints.add(currentTrail.getDouble("lat"));
                    headLngPoints.add(currentTrail.getDouble("lng"));
                    trailIDs.add(currentTrail.getString("id"));
                    trailNames.add(currentTrail.getString("trailName"));
                }
                setUpPoints();
            }
        } catch (JSONException e) {
            Log.w("url :","url");
            e.printStackTrace();
        }
    }

    private void setUpPoints(){

        for(int i = 0; i < headLatPoints.size() ; i++){

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(headLatPoints.get(i) , headLngPoints.get(i)))
                    .title("Trail Name")
                    .snippet(trailIDs.get(i)));
        }

        mGoogleMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Intent intent = new Intent(getContext(), TrailProfile.class);
                        intent.putExtra("trail_id", marker.getSnippet());
                        startActivity(intent);
                        return true;
                    }
                }
        );

    }
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
            // TODO: Ask for permissions
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

            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,this);
        }
        else{
            // TODO: Ask for permissions
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO: Retry connection
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(googleApiClient.isConnected()){
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
        //change lat lon
        if(calculatedistance(lat, lon) > 5) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            getAndSaveHeadPoints();
        }
        //reupload head points
        if(calculatedistance(reuploadLat , reuploadLng) > 10) {

            reuploadLat = location.getLatitude();
            reuploadLng = location.getLongitude();
            getAndSaveHeadPoints();
            setUpPoints();
        }
        goToLocation(lat,lon,20);
    }

    public double calculatedistance(double clat, double clon){
        double earthd = 6371000;
        double lat1Rad = toRadians(lat);
        double lat2Rad = toRadians(clat);
        double deltasig = toRadians(clat - lat);
        double deltalam = toRadians(clon - lon);
        double a = sin(deltasig/2) * sin(deltasig/2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltalam/2) * sin(deltalam/2);
        double c = 2 * atan2(sqrt(a),sqrt(1-a));
        return earthd * c;
    }

    protected boolean goToLocation(double latatude, double longitude, float zoom) {
        LatLng Ll = new LatLng(latatude, longitude);
        CameraUpdate camupdate = CameraUpdateFactory.newLatLngZoom(Ll, zoom);
        mGoogleMap.animateCamera(camupdate);
        CurrentLoacation(latatude, longitude);
         //if(mGoogleMap.getCameraPosition() == );
         return true;
    }

    //used to connect to the database.
    private void initializeVariables() {
        httpConnect = new HTTPConnect();
        headLatPoints = new ArrayList<Double>();
        headLngPoints = new ArrayList<Double>();
        trailNames = new ArrayList<String>();
        trailIDs = new ArrayList<String>();
    }

    private void CurrentLoacation(double clat,double clon){
        userMarker.setPosition(new LatLng(clat, clon));
    }
}
