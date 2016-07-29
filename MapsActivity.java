package fi.aalto.ming.uitestscreens;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "fi.aalto.ming.MapsActivity";
/*    private static final LatLng HELSINKI = new LatLng(60.160999, 24.944800);
    private static final LatLng NEAR_HKI =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng WEST =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.016059);
    private static final LatLng SOUTH =
            new LatLng(HELSINKI.latitude - 0.008, HELSINKI.longitude);
*/
    private static final LatLng HELSINKI = new LatLng(60.168928, 24.937195);

    private static final double LAT_RADIUS = 0.007315;
    private static final double LNG_RADIUS = 0.015789;

    private static final LatLng NEAR_HKI =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng WEST =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - LNG_RADIUS);
    private static final LatLng NORTH =
            new LatLng(HELSINKI.latitude + LAT_RADIUS, HELSINKI.longitude);
    private static final LatLng SOUTH =
            new LatLng(HELSINKI.latitude - LAT_RADIUS, HELSINKI.longitude);
    private static final LatLng SOUTHWEST =
            new LatLng(HELSINKI.latitude - LAT_RADIUS, HELSINKI.longitude - LNG_RADIUS);
    private static final LatLng NORTHEAST =
            new LatLng(HELSINKI.latitude + LAT_RADIUS, HELSINKI.longitude + LNG_RADIUS);

    private GoogleMap mMap;
    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();
    private UiSettings mUiSettings;

    private GroundOverlay mGroundOverlay;
    ImageView legend;

    private int mCurrentEntry = 0;

    private int uiStyle = 0;

    private TypedArray latArray;
    private TypedArray lngArray;
    private int[] conf_colors;
    private int[] conf_markers;
    private int colorCount=0;
    private int markerStart = 0;
    private int markerCount;
    private int screenCount = 0;
    private boolean arraysRecycled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        mUiSettings.setCompassEnabled(false);

        // Move the camera
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(NEAR_HKI)           // Sets the center of the map to fit the UI overlay on screen
                .zoom(14)                   // Sets the zoom to fit the UI overlay on screen
                .bearing(180)               // Sets the orientation of the camera to south
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // Read the information about configuring the screens from resource file
        Resources res = getResources();
        latArray = res.obtainTypedArray(R.array.lat);
        lngArray = res.obtainTypedArray(R.array.lng);
        conf_colors = res.getIntArray(R.array.conf_colors);
        conf_markers = res.getIntArray(R.array.conf_markers);

        //Test consistency of array lengths
        int totalMarkers = 0;
        int totalColors = 0;

        for (int i=0; i<conf_colors.length; i++){
            totalColors = totalColors+conf_colors[i];
        }
        if (totalColors != conf_markers.length)
            Log.e(TAG,"conf_markers length mismatch");

        for (int i=0; i<conf_markers.length; i++){
            totalMarkers = totalMarkers+conf_markers[i];
        }
        if (totalMarkers != latArray.length())
            Log.e(TAG,"latArray length mismatch");
        if (totalMarkers != lngArray.length())
            Log.e(TAG,"latArray length mismatch");

        // Draw the first UI screen
        legend = (ImageView)findViewById(R.id.imageView);
        legend.performClick();
    }

    public void drawMarkers(View view) {

        if (screenCount<conf_colors.length) { // For each screen
            mMap.clear();
            int markersPerScreen = 0;

            // If UI style requires, sets the map type to be "none"
            switch (uiStyle) {
                case 0: case 1: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                    break;
                }
                case 2: {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                }
            }

            // Count the number markers to be drawn for this screen
            for (int i = colorCount; i < (colorCount + conf_colors[screenCount]); i++) {
                markersPerScreen = markersPerScreen + conf_markers[i];
            }

            for (markerCount = markerStart; markerCount < markerStart + markersPerScreen; markerCount++) { // For each marker in this screen

                // Scale marker locations to the area covered by radar/map
                float lat = (float) (HELSINKI.latitude - LAT_RADIUS + (2 * LAT_RADIUS * latArray.getFloat(markerCount, 1)));
                float lng = (float) (HELSINKI.longitude - LNG_RADIUS + (2 * LNG_RADIUS * lngArray.getFloat(markerCount, 1)));

                switch (uiStyle) {

                    case 0: case 3: { // Use dots as markers
                        // Draw markers with the appropriate color i.e. check from conf_color if there was 2nd and 3rd color for this screen and from conf_markers how many markers to print with each color
                        if (markerCount < (markerStart + conf_markers[colorCount])) // If should use the first color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_eating_20)));
                        else if (conf_colors[screenCount] > 1 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1]) // If should use the second color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_culture_20)));
                        else if (conf_colors[screenCount] > 2 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2]) // If should use the third color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoors_20)));
                    break;
                    }

                    case 1: case 2: { // Use icons as markers
                        // Draw markers with the appropriate color i.e. check from conf_color if there was 2nd and 3rd color for this screen and from conf_markers how many markers to print with each color
                        if (markerCount < (markerStart + conf_markers[colorCount])) // If should use the first color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_food_20)));
                        else if (conf_colors[screenCount] > 1 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1]) // If should use the second color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_culture_20)));
                        else if (conf_colors[screenCount] > 2 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount + 1] + conf_markers[colorCount + 2]) // If should use the third color
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bkg_outdoors_20)));
                    break;
                    }
                }

            }
            markerStart = markerStart + markersPerScreen;
            colorCount = colorCount + conf_colors[screenCount];
            screenCount++;
        } else if (uiStyle < 4) {
            // Switch to new UI style and start over for next screen
            uiStyle++;
            markerStart = 0;
            colorCount = 0;
            screenCount = 0;
        } else if (!arraysRecycled) { // If drawn all the screens for all UI styles, recycle the resources
            latArray.recycle();
            lngArray.recycle();
            arraysRecycled = true;
        }

        mMap.addMarker(new MarkerOptions().position(HELSINKI).title("Your location"));
/*        mMap.addMarker(new MarkerOptions().position(WEST).title("West"));
        mMap.addMarker(new MarkerOptions().position(SOUTH).title("South"));
        mMap.addMarker(new MarkerOptions().position(NORTH).title("North"));
        mMap.addMarker(new MarkerOptions().position(NORTHEAST).title("Northeast"));
        mMap.addMarker(new MarkerOptions().position(SOUTHWEST).title("Southwest"));
*/
        // If UI style requires, adds a radar UI overlay at Punavuori, Helsinki
        if (uiStyle == 0 || uiStyle == 1) {
            mImages.clear();
            mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq));
            mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                    .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                    .position(HELSINKI, 1969f, 1969f)
                    .bearing(180));
        }

        if (screenCount == 0)
            legend.performClick();
    }

}
