package fi.aalto.ming.uitestscreens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MapsActivity";
/*    private static final LatLng HELSINKI = new LatLng(60.160999, 24.944800);
    private static final LatLng NEAR_HKI =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng WEST =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.016059);
    private static final LatLng SOUTH =
            new LatLng(HELSINKI.latitude - 0.008, HELSINKI.longitude);
*/
//    private static final LatLng HELSINKI = new LatLng(60.168928, 24.937195);
    private static final LatLng HELSINKI = new LatLng(0.0, 0.0);
    private static final double LAT_RADIUS = 0.007315;
    private static final double LNG_RADIUS = 0.015789;

    private static final LatLng NEAR_HKI =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng SOUTHWEST =
            new LatLng(HELSINKI.latitude - LAT_RADIUS, HELSINKI.longitude - LNG_RADIUS);
    private static final LatLng NORTHEAST =
            new LatLng(HELSINKI.latitude + LAT_RADIUS, HELSINKI.longitude + LNG_RADIUS);

    private GoogleMap mMap;
    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();
    private UiSettings mUiSettings;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private GroundOverlay mGroundOverlay;
    ImageView legend;

    private int mCurrentEntry = 0;

    private int uiStyle = 0;

    private Vector latList;
    private Vector lngList;
    private Vector nameList;
    private int[] conf_colors;
    private int[] conf_markers;
    private int colorCount=0;
    private int markerStart = 0;
    private int markerCount;
    private int screenCount = 0;
    private boolean arraysRecycled = false;

    ArrayList venuesList;

    // the foursquare client_id and the client_secret
    final String CLIENT_ID = "I0C0HY1NYS1KL1FWKW1SUS3SLP2ZBA40PXIOBBT5HTVBPM3A";
    final String CLIENT_SECRET = "WV3QKEIECZW4GJXD1T1X5ZOUYBCADWSHJUJIZTAIICQ23QV5";

    ArrayAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        latList = new Vector();
        lngList = new Vector();
        nameList = new Vector();
        venuesList = new ArrayList();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 51);
        } else {
            fetchVenues();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: Error Code = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 51: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    fetchVenues();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void fetchVenues() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new fourquare().execute("4bf58dd8d48988d16d941735","");
        new fourquare().execute("4bf58dd8d48988d181941735","");
        new fourquare().execute("4bf58dd8d48988d165941735","last");
    }

    private class fourquare extends AsyncTask<String, Void, String> {
        String temp;

        @Override
        protected String doInBackground(String... urls) {
            // make Call to the url
            // venues/search?sw=60.158345,24.930203&ne=60.169805,24.953239&categoryId=4bf58dd8d48988d1e0931735&intent=browse&limit=60
            // temp = makeCall("https://api.foursquare.com/v2/venues/search?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&v=20130815&sw=60.158345,24.930203&ne=60.169805,24.953239&categoryId=4bf58dd8d48988d1e0931735&intent=browse&limit=60");
            // temp = makeCall("https://api.foursquare.com/v2/venues/search?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&v=20130815&sw=" + SOUTHWEST.latitude + "," + SOUTHWEST.longitude + "&ne=" + NORTHEAST.latitude + "," + NORTHEAST.longitude + "&categoryId=4bf58dd8d48988d16d941735,4bf58dd8d48988d181941735,4bf58dd8d48988d165941735&intent=browse&limit=60");
            LatLng sw = SOUTHWEST;
            LatLng ne = NORTHEAST;
            if (mLastLocation != null) {
                sw = new LatLng(mLastLocation.getLatitude() - LAT_RADIUS, mLastLocation.getLongitude() - LNG_RADIUS);
                ne = new LatLng(mLastLocation.getLatitude() + LAT_RADIUS, mLastLocation.getLongitude() + LNG_RADIUS);
            }
            temp = makeCall("https://api.foursquare.com/v2/venues/search?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&v=20130815&sw=" + sw.latitude + "," + sw.longitude + "&ne=" + ne.latitude + "," + ne.longitude + "&categoryId=" + urls[0] + "&intent=browse&limit=60");
            return urls[1];
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
        }

        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right
                // parseFoursquare venues search result
                venuesList.addAll((ArrayList) parseFoursquare(temp));
                if (result.equals("last")) {
                    legend.performClick();
                }
//                List listTitle = new ArrayList();
//                FoursquareVenue fsVenueTemp = new FoursquareVenue();
//                for (int i = 0; i < venuesList.size(); i++) {
                    // make a list of the venus that are loaded in the list.
                    // show the name, the category and the city
//                    fsVenueTemp = (FoursquareVenue) venuesList.get(i);
//                    listTitle.add(i, fsVenueTemp.getName() + ", " + fsVenueTemp.getCategory() + "" + fsVenueTemp.getCity());
//                    boolean latok = latList.add(fsVenueTemp.getLat());
//                    boolean lngok = lngList.add(fsVenueTemp.getLng());
//                    boolean nameok = nameList.add(fsVenueTemp.getName());
//            }
                // set the results to the list
                // and show them in the xml
                //myAdapter = new ArrayAdapter(AndroidFoursquare.this, R.layout.row_layout, R.id.listText, listTitle);
                //setListAdapter(myAdapter);

                // Draw the first UI screen
            }
        }
    }

    public static String makeCall(String url) {

        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";

        // instanciate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        // instanciate an HttpGet
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the responce of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // trim the whitespaces
        return replyString.trim();
    }

    private static ArrayList parseFoursquare(final String response) {
        ArrayList temp = new ArrayList();
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("response")) {
                if (jsonObject.getJSONObject("response").has("venues")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("response").getJSONArray("venues");
                    // {c: .response.venues[].categories[].name}
                    // {c: .response.venues[].location.lng}
                    // {c: .response.venues[].name}

                    for (int i = 0; i<jsonArray.length() ; i++) {
                        FoursquareVenue poi = new FoursquareVenue();
                        String latTemp = jsonArray.getJSONObject(i).getJSONObject("location").getString("lat");
                        String lngTemp = jsonArray.getJSONObject(i).getJSONObject("location").getString("lng");
                        String nameTemp = new String(jsonArray.getJSONObject(i).getString("name"));
                        String catTemp = jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("id");
                        poi.setLat(latTemp);
                        poi.setLng(lngTemp);
                        poi.setName(nameTemp);
                        poi.setCategory(catTemp);
                        temp.add(poi);
//                            poi.setCategory(jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("name"));
                    }
                }
            }

        } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList();
        }
        return temp;

        }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we add the radar overlay to the user position we've fixed.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Disable controls
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setCompassEnabled(true);

        // Move the camera
        if (mLastLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude() - 0.0001))           // Sets the center of the map to fit the UI overlay on screen
                    .zoom(14)                   // Sets the zoom to fit the UI overlay on screen
                    .bearing(180)               // Sets the orientation of the camera to south
                    .build();                   // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        // Read the information about configuring the screens from resource file
        Resources res = getResources();
//        latList = res.obtainTypedArray(R.array.lat);
//        lngList = res.obtainTypedArray(R.array.lng);
        conf_colors = res.getIntArray(R.array.conf_colors);
        conf_markers = res.getIntArray(R.array.conf_markers);

        //Test consistency of array lengths
/*        int totalMarkers = 0;
        int totalColors = 0;

        for (int i=0; i<conf_colors.length; i++){
            totalColors = totalColors+conf_colors[i];
        }
        if (totalColors != conf_markers.length)
            Log.e(TAG,"conf_markers len mismatch");

        for (int i=0; i<conf_markers.length; i++){
            totalMarkers = totalMarkers+conf_markers[i];
        }

        if (totalMarkers != latList.size())
            Log.e(TAG,"latList len mismatch");
        if (totalMarkers != lngList.length())
            Log.e(TAG,"latList len mismatch");
*/
        // Draw the first UI screen
        legend = (ImageView)findViewById(R.id.imageView);
//        legend.performClick();
    }

    public void drawMarkers(View view) {

//        if (screenCount<conf_colors.length) { // For each screen
            mMap.clear();
//            int markersPerScreen = 0;

            // Depending on the UI style, set the map type and indicator for user's location
            switch (uiStyle) {
                case 0: case 1: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    break;
                }
                case 2: case 3: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Your location")
//                            .zIndex(1.0f)
                            .icon(BitmapDescriptorFactory.defaultMarker(150f)));
                    break;
                }
                case 4: case 5: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Your location")
//                            .zIndex(1.0f)
                            .icon(BitmapDescriptorFactory.defaultMarker(150f)));
                    break;
                }
            }

            Resources res = getResources();
            // Set the legend based on UI style
            switch (uiStyle) {
                case 0: case 3: case 4: { // Use a legend with dots
        /*            switch (conf_colors[screenCount]) {
                          case 2: {
                              legend.setImageDrawable(res.getDrawable(R.drawable.legend2dots, null));
                              break;
                          }
                        case 3: {
                   */         legend.setImageDrawable(res.getDrawable(R.drawable.legend3dots, null));
                  /*          break;
                        }
                        case 4: {
                            legend.setImageDrawable(res.getDrawable(R.drawable.legend4dots, null));
                            break;
                        }
                    }
              */      break;
                }
                case 1: case 2: case 5: { // Use a legend with icons
                /*    switch (conf_colors[screenCount]) {
                        case 2: {
                            legend.setImageDrawable(res.getDrawable(R.drawable.legend2icons, null));
                            break;
                        }
                        case 3: {
                 */           legend.setImageDrawable(res.getDrawable(R.drawable.legend3icons, null));
                 /*           break;
                        }
                        case 4: {
                            legend.setImageDrawable(res.getDrawable(R.drawable.legend4icons, null));
                            break;
                        }
                    }
                   */ break;
                }
            }
            // Count the number of markers to be drawn for this screen
/*            for (int i = colorCount; i < (colorCount + conf_colors[screenCount]); i++) {
                markersPerScreen = markersPerScreen + conf_markers[i];
            }
*/

            for (int i=0; i<venuesList.size(); i++) {
                FoursquareVenue fsVenueTemp = new FoursquareVenue();
                fsVenueTemp = (FoursquareVenue) venuesList.get(i);
                float lat = fsVenueTemp.getLat();
                float lng = fsVenueTemp.getLng();
                String title = fsVenueTemp.getName();
                String category = fsVenueTemp.getCategory();
                switch (uiStyle) {
                    case 0:
                    case 3:
                    case 4: { // Use dots as markers
                        if (category.equals("4bf58dd8d48988d16d941735")) { //Cafe
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_food_25)));
                        } else if (category.equals("4bf58dd8d48988d181941735")) { //Museum
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_museum_25)));
                        } else if (category.equals("4bf58dd8d48988d165941735")) { //Scenic Lookout
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoors_25)));
                        }
                        break;
                    }
                    case 1:
                    case 2:
                    case 5: { // Use dots as markers
                        if (category.equals("4bf58dd8d48988d16d941735")) { //Cafe
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_food_25)));
                        } else if (category.equals("4bf58dd8d48988d181941735")) { //Museum
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_museum_25)));
                        } else if (category.equals("4bf58dd8d48988d165941735")) { //Scenic Lookout
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_outdoors_25)));
                        }
                        break;
                    }

                }
            }
/*
            markersPerScreen = latList.size();
            for (markerCount = markerStart; markerCount < markerStart + markersPerScreen; markerCount++) { // For each marker in this screen

                // Scale marker locations to the area covered by radar/map
//                float lat = (float) (HELSINKI.latitude - LAT_RADIUS + (2 * LAT_RADIUS * latList.getFloat(markerCount, 1)));
//                float lng = (float) (HELSINKI.longitude - LNG_RADIUS + (2 * LNG_RADIUS * lngList.getFloat(markerCount, 1)));
                float lat = (float) latList.get(markerCount);
                float lng = (float) lngList.get(markerCount);
                String title = (String) nameList.get(markerCount);
                switch (uiStyle) {

                    case 0: case 3: case 4: { // Use dots as markers
                        // Draw markers with the appropriate color i.e. check from conf_color if there was 2nd and 3rd color for this screen and from conf_markers how many markers to print with each color
                        if (markerCount < (markerStart + conf_markers[colorCount])) // If should use the first color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_food_25)));
                        else if (conf_colors[screenCount] > 1 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1]) // If should use the second color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_museum_25)));
                        else if (conf_colors[screenCount] > 2 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2]) // If should use the third color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoors_25)));
                        else if (conf_colors[screenCount] > 3 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2] + conf_markers[colorCount + 3]) // If should use the fourth color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_nightlife_25)));
                    break;
                    }

                    case 1: case 2: case 5: { // Use icons as markers
                        // Draw markers with the appropriate color i.e. check from conf_color if there was 2nd and 3rd color for this screen and from conf_markers how many markers to print with each color
                        if (markerCount < (markerStart + conf_markers[colorCount])) // If should use the first color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_food_25)));
                        else if (conf_colors[screenCount] > 1 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1]) // If should use the second color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_museum_25)));
                        else if (conf_colors[screenCount] > 2 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2]) // If should use the third color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_outdoors_25)));
                        else if (conf_colors[screenCount] > 3 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2] + conf_markers[colorCount + 3]) // If should use the fourth color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_nightlife_25)));
                    break;
                    }
                }

            }
            markerStart = markerStart + markersPerScreen;
            colorCount = colorCount + conf_colors[screenCount];
            screenCount++;
        } else if (uiStyle < 6) {
            // Switch to new UI style and start over for next screen
            uiStyle++;
            markerStart = 0;
            colorCount = 0;
            screenCount = 0;
        } else if (!arraysRecycled) { // If drawn all the screens for all UI styles, recycle the resources
//            latList.recycle();
//            lngList.recycle();
            arraysRecycled = true;
        }
*/

/*        mMap.addMarker(new MarkerOptions().position(WEST).title("West"));
        mMap.addMarker(new MarkerOptions().position(SOUTH).title("South"));
        mMap.addMarker(new MarkerOptions().position(NORTH).title("North"));
        mMap.addMarker(new MarkerOptions().position(NORTHEAST).title("Northeast"));
        mMap.addMarker(new MarkerOptions().position(SOUTHWEST).title("Southwest"));
*/
        // If UI style requires, adds a radar UI or black square overlay at Punavuori, Helsinki
        switch (uiStyle) {
            case 0: case 1: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 1969f, 1969f)
                        .bearing(180));
            break;
            }
            case 4: case 5: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.black_sq));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 1969f, 1969f)
                        .bearing(180));
                break;
            }

        // Draw radar shape on top of the map
            /*            case 2: case 3: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq_black_transp));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(HELSINKI, 1969f, 1969f)
                        .bearing(180)
                        .transparency(0.5f)
                );
                break;
            } */
        }
        uiStyle++;
//        if (screenCount == 0)
//           legend.performClick();
    }

}
