package fi.aalto.ming.uitestscreens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.Marker;
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
import java.util.LinkedList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsActivity";

    private String[] mDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    private String[] venue_ids;
    private LinkedList selected_ids;
    private String[] old_building_names;
    private TypedArray old_building_lat;
    private TypedArray old_building_lng;

    private Toolbar myToolbar;

    private static final LatLng HELSINKI = new LatLng(60.168928, 24.937195);
    private static final double LAT_RADIUS = 0.007315;
    private static final double LNG_RADIUS = 0.015789;

//    private static final LatLng NEAR_HKI =
//            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng SOUTHWEST =
            new LatLng(HELSINKI.latitude - LAT_RADIUS, HELSINKI.longitude - LNG_RADIUS);
    private static final LatLng NORTHEAST =
            new LatLng(HELSINKI.latitude + LAT_RADIUS, HELSINKI.longitude + LNG_RADIUS);

    private GoogleMap mMap;
    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();
    private UiSettings mUiSettings;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private SensorManager mSensorManager;
    private float[] mRotationMatrix = new float[16];
    private float mDeclination;

    private GroundOverlay mGroundOverlay;
    ImageView legend;

    private int mCurrentEntry = 0;

    private int uiStyle = 0;
    ArrayList venuesList;

    // the foursquare client_id and the client_secret
    final String CLIENT_ID = "I0C0HY1NYS1KL1FWKW1SUS3SLP2ZBA40PXIOBBT5HTVBPM3A";
    final String CLIENT_SECRET = "WV3QKEIECZW4GJXD1T1X5ZOUYBCADWSHJUJIZTAIICQ23QV5";

    ArrayAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        // Instantiate the legend
        legend = (ImageView)findViewById(R.id.imageView);

        selected_ids = new LinkedList();
        Resources res = getResources();
        mDrawerItems = res.getStringArray(R.array.venue_types);
        venue_ids = res.getStringArray(R.array.venue_ids);
        old_building_names = res.getStringArray(R.array.old_building_names);
        old_building_lat = res.obtainTypedArray(R.array.old_building_lat);
        old_building_lng = res.obtainTypedArray(R.array.old_building_lng);

//        selected_ids.add("47");
        for (int i=0; i<venue_ids.length; i++)
            selected_ids.add(venue_ids[i]);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.drawer);

        if (myToolbar != null) {
            myToolbar.setTitle("Navigation Drawer");
            setSupportActionBar(myToolbar);
        }

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_multi_item, mDrawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                myToolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description */
                R.string.navigation_drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            fetchVenues();
        }

            /** Called when a drawer has settled in a completely open state. */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            selected_ids.clear();
            mDrawerList.clearChoices();
            for (int i = 0; i < mDrawerList.getCount(); i++)
                mDrawerList.setItemChecked(i, false);
            venuesList.clear();
        }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_STATUS_ACCURACY_LOW);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
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
        // Get the location of the device
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        float[] distance = new float[1];
        if (selected_ids.size() > 0) {
            for (int i = 0; i < selected_ids.size() - 1; i++) {
                String id = (String) selected_ids.get(i);
                if (id.equals("47")) {
                    for (int j=0; j<old_building_names.length; j++) {
                        Location.distanceBetween((double) old_building_lat.getFloat(j, 0.0f), (double) old_building_lng.getFloat(j, 0.0f), mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);
                        if (distance[0]<800)
                            venuesList.add(new FoursquareVenue(old_building_names[j], id, old_building_lat.getFloat(j, 0.0f), old_building_lng.getFloat(j, 0.0f)));
                    }
                } else
                    new fourquare().execute(id, "");
            }
            if (selected_ids.get(selected_ids.size() - 1).equals("47")) {
                for (int j = 0; j < old_building_names.length; j++) {
                    Location.distanceBetween((double) old_building_lat.getFloat(j, 0.0f), (double) old_building_lng.getFloat(j, 0.0f), mLastLocation.getLatitude(), mLastLocation.getLongitude(), distance);
                    if (distance[0] < 900)
                        venuesList.add(new FoursquareVenue(old_building_names[j], "47", old_building_lat.getFloat(j, 0.0f), old_building_lng.getFloat(j, 0.0f)));
                }
                if (selected_ids.size() == 1) { // If no other category is selected, we can draw the map already
                    if (uiStyle > 0)
                        uiStyle--;
                    legend.performClick();
                }
            } else
                new fourquare().execute((String) selected_ids.get(selected_ids.size() - 1), "last"); // This is the last venue id, so make the Foursquare call AND draw the map
        }
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
                    if (uiStyle > 0)
                        uiStyle--;
                    legend.performClick();
                }
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
                        String nameTemp = jsonArray.getJSONObject(i).getString("name");
                        String catTemp = jsonArray.getJSONObject(i).getJSONArray("categories").getJSONObject(0).getString("id");
                        String idTemp = jsonArray.getJSONObject(i).getString("id");
                        poi.setLat(latTemp);
                        poi.setLng(lngTemp);
                        poi.setName(nameTemp);
                        poi.setCategory(catTemp);
                        poi.setId(idTemp);
                        temp.add(poi);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        mMap.setOnInfoWindowClickListener(this);

        // Disable controls
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setCompassEnabled(false);
    }

    public void drawMarkers(View view) {
        mMap.clear();

        // Move the camera
        if (mLastLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude() - 0.0001))           // Sets the center of the map to fit the UI overlay on screen
                    .zoom(14)                   // Sets the zoom to fit the UI overlay on screen
//                    .bearing(0)               // Sets the orientation of the camera to north
                    .build();                   // Creates a CameraPosition from the builder
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

            // Depending on the UI style, set the map type and indicator for user's location
            switch (uiStyle) {
                case 0: case 1: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    mUiSettings.setScrollGesturesEnabled(false);
                    mUiSettings.setZoomGesturesEnabled(false);
                    mUiSettings.setRotateGesturesEnabled(false);
                    break;
                }
                case 2: case 3: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mUiSettings.setScrollGesturesEnabled(true);
                    mUiSettings.setZoomGesturesEnabled(true);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Your location")
                            .icon(BitmapDescriptorFactory.defaultMarker(150f)));
                    break;
                }
                case 4: case 5: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    mUiSettings.setScrollGesturesEnabled(false);
                    mUiSettings.setZoomGesturesEnabled(false);
                    mUiSettings.setRotateGesturesEnabled(false);
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Your location")
                            .icon(BitmapDescriptorFactory.defaultMarker(150f)));
                    break;
                }
                case 6: case 7: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    mUiSettings.setScrollGesturesEnabled(false);
                    mUiSettings.setZoomGesturesEnabled(false);
                    mUiSettings.setRotateGesturesEnabled(false);
                    break;
                    }
                }


            Resources res = getResources();
            // Set the legend based on UI style
            switch (uiStyle) {
                case 0: case 3: case 4: case 6: { // Use a legend with dots
        /*            switch (conf_colors[screenCount]) {
                          case 2: {
                              legend.setImageDrawable(res.getDrawable(R.drawable.legend2dots, null));
                              break;
                          }
                        case 3: {
                   */           legend.setImageDrawable(res.getDrawable(R.drawable.legend4dots, null));
                  /*          break;
                        }
                        case 4: {
                            legend.setImageDrawable(res.getDrawable(R.drawable.legend4dots, null));
                            break;
                        }
                    }
              */      break;
                }
                case 1: case 2: case 5: case 7: { // Use a legend with icons
                /*    switch (conf_colors[screenCount]) {
                        case 2: {
                            legend.setImageDrawable(res.getDrawable(R.drawable.legend2icons, null));
                            break;
                        }
                        case 3: {
                 */           legend.setImageDrawable(res.getDrawable(R.drawable.legend4icons, null));
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

            for (int i=0; i<venuesList.size(); i++) {
                FoursquareVenue fsVenueTemp = new FoursquareVenue();
                fsVenueTemp = (FoursquareVenue) venuesList.get(i);
                float lat = fsVenueTemp.getLat();
                float lng = fsVenueTemp.getLng();
                String title = fsVenueTemp.getName();
                String category = fsVenueTemp.getCategory();
                Marker marker;
                switch (uiStyle) {
                    case 0:
                    case 3:
                    case 4:
                    case 6: { // Use dots as markers
                        if (category.equals("4bf58dd8d48988d16d941735") || category.equals("4bf58dd8d48988d155941735") ||
                                category.equals("4bf58dd8d48988d10f941735") || category.equals("4bf58dd8d48988d110941735") ||
                                category.equals("5283c7b4e4b094cb91ec88d7") || category.equals("4bf58dd8d48988d1c1941735") ||
                                category.equals("52e81612bcbc57f1066b79f9") || category.equals("4bf58dd8d48988d1c6941735") ||
                                category.equals("4bf58dd8d48988d1c4941735")) { //Cafe, Gastropub, Indian, Italian, Kebab, Modern European, Scandinavian resta, or just resta
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_food_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d181941735") || category.equals("47")) { //Museum or Old Building
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_museum_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d165941735") || category.equals("56aa371be4b08b9a8d573544") ||
                                category.equals("52e81612bcbc57f1066b7a22") || category.equals("56aa371be4b08b9a8d573562") ||
                                category.equals("56aa371be4b08b9a8d573547") || category.equals("4bf58dd8d48988d15a941735") ||
                                category.equals("4bf58dd8d48988d1e0941735") || category.equals("4bf58dd8d48988d163941735") ||
                                category.equals("4bf58dd8d48988d1e2941735")) { //Scenic Lookout, Bay, Botanical garden, Canal, Fountain, Garden, Harbor, Park or Beach
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoors_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d1fd941735") || category.equals("5744ccdfe4b0c0459246b4dc") ||
                                category.equals("4bf58dd8d48988d1f6941735")) { //Shopping Mall, Shopping Plaza, Department Store
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_nightlife_25)));
                            marker.setTag(fsVenueTemp.getId());
/*                        } else if (category.equals("4d4b7105d754a06376d81259") || category.equals("4bf58dd8d48988d120941735") ||
                                category.equals("4bf58dd8d48988d11b941735") || category.equals("4bf58dd8d48988d11f941735") ||
                                category.equals("50327c8591d4c4b30a586d5d") || category.equals("4bf58dd8d48988d116941735") ||
                                category.equals("4bf58dd8d48988d11e941735")) { //Nightlife, Karaoke Bar, Pub, Nightclub, Brewery, Bar or Cocktail Bar
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_nightlife_25)));
*/                        }
                        break;
                    }
                    case 1:
                    case 2:
                    case 5:
                    case 7: { // Use icons as markers
                        if (category.equals("4bf58dd8d48988d16d941735") || category.equals("4bf58dd8d48988d155941735") ||
                                category.equals("4bf58dd8d48988d10f941735") || category.equals("4bf58dd8d48988d110941735") ||
                                category.equals("5283c7b4e4b094cb91ec88d7") || category.equals("4bf58dd8d48988d1c1941735") ||
                                category.equals("52e81612bcbc57f1066b79f9") || category.equals("4bf58dd8d48988d1c6941735") ||
                                category.equals("4bf58dd8d48988d1c4941735")) { //Cafe, Gastropub, Indian, Italian, Kebab, Modern European, Scandinavian resta, or just resta
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_food_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d181941735") || category.equals("47")) { //Museum or Old Building
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_museum_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d165941735") || category.equals("56aa371be4b08b9a8d573544") ||
                                category.equals("52e81612bcbc57f1066b7a22") || category.equals("56aa371be4b08b9a8d573562") ||
                                category.equals("56aa371be4b08b9a8d573547") || category.equals("4bf58dd8d48988d15a941735") ||
                                category.equals("4bf58dd8d48988d1e0941735") || category.equals("4bf58dd8d48988d163941735") ||
                                category.equals("4bf58dd8d48988d1e2941735")) { //Scenic Lookout, Bay, Botanical garden, Canal, Fountain, Garden, Harbor, Park or Beach
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_outdoors_25)));
                            marker.setTag(fsVenueTemp.getId());
                        } else if (category.equals("4bf58dd8d48988d1fd941735") || category.equals("5744ccdfe4b0c0459246b4dc") ||
                                category.equals("4bf58dd8d48988d1f6941735")) { //Shopping Mall, Shopping Plaza, Department Store
                            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_shopping_24)));
                            marker.setTag(fsVenueTemp.getId());
/*                        } else if (category.equals("4d4b7105d754a06376d81259") || category.equals("4bf58dd8d48988d120941735") ||
                                category.equals("4bf58dd8d48988d11b941735") || category.equals("4bf58dd8d48988d11f941735") ||
                                category.equals("50327c8591d4c4b30a586d5d") || category.equals("4bf58dd8d48988d116941735") ||
                                category.equals("4bf58dd8d48988d11e941735")) { //Nightlife, Karaoke Bar, Pub, Nightclub, Brewery, Bar or Cocktail Bar
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(title)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_nightlife_25)));
*/                        }
                        break;
                    }

                }
            }

        // If UI style requires, adds a radar UI or black square overlay at Punavuori, Helsinki
        switch (uiStyle) {
            case 0: case 1: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq_1000));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 3150f, 3150f));
//                        .bearing(180));
            break;
            }
            case 4: case 5: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.black_sq));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 3150f, 3150f));
//                        .bearing(180));
                break;
            }
            case 6: case 7: {
                mImages.clear();
                mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq_1000));
                mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 3150f, 3150f));
//                        .bearing(180));
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
        if (uiStyle > 6)
            uiStyle = 0;
        uiStyle++;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (venue_ids[position].equals("47"))
                selected_ids.addFirst(venue_ids[position]);
            else
                selected_ids.add(venue_ids[position]);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        GeomagneticField field = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis()
        );

        // getDeclination returns degrees
        mDeclination = field.getDeclination();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            float bearing = (float)Math.toDegrees(orientation[0]) + mDeclination;
            updateCamera(bearing);
            if (uiStyle-1 == 0 || uiStyle-1 == 1)
                if (mGroundOverlay != null)
                    mGroundOverlay.setBearing(bearing);
//            bearing_old = bearing;
        }

       /* IF NEEDS TO LIMIT THE AMOUNT OF UPDATES
            if (Math.abs(Math.toDegrees(orientation[0]) - angle) > 0.8) {
                float bearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                updateCamera(bearing);
            }
            angle = Math.toDegrees(orientation[0]);
        */

    }

    private void updateCamera(float bearing) {
        if (mMap != null) {
            CameraPosition oldPos = mMap.getCameraPosition();
            CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        String venueId = (String)marker.getTag();
        Uri fsUri = Uri.parse("https://foursquare.com/venue/" + venueId);
        Intent launchFS = new Intent(Intent.ACTION_VIEW, fsUri);
        startActivity(launchFS);
    }
}
