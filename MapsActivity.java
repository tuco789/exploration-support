package fi.aalto.ming.uitestscreens;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;

import fi.aalto.ming.uitestscreens.R;
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

    private static final LatLng HELSINKI = new LatLng(60.160999, 24.944800);
    private static final LatLng NEAR_HKI =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.0001);
    private static final LatLng WEST =
            new LatLng(HELSINKI.latitude, HELSINKI.longitude - 0.016059);
    private static final LatLng SOUTH =
            new LatLng(HELSINKI.latitude - 0.008, HELSINKI.longitude);
    private GoogleMap mMap;
    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();
    private UiSettings mUiSettings;

    private GroundOverlay mGroundOverlay;

    private int mCurrentEntry = 0;

    private TypedArray latArray;
    private TypedArray lngArray;
    private int[] conf_colors;
    private int[] conf_markers;
    private int colorCount=0;
    private int markerStart = 0;
    private int markerCount;
    private int screenCount = 0;
//    private int markersPerScreenCount = 0;

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

        // Sets the map type to be "none"
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        // Disable controls
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(false);
        mUiSettings.setZoomGesturesEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mUiSettings.setRotateGesturesEnabled(false);
        mUiSettings.setCompassEnabled(false);

        // Add a marker in Punavuori, Helsinki and move the camera

        /*        LatLng DeliCafeMaya = new LatLng(60.1616273, 24.9390844);
        LatLng Kaivopuisto = new LatLng(60.1568098, 24.9567318);
        LatLng MikaelAgr = new LatLng(60.1583393, 24.939403);
        LatLng Observatory = new LatLng(60.1622016, 24.9516979);
        LatLng Ursa = new LatLng(60.155325, 24.955435);
        LatLng Mannerheim = new LatLng(60.1588078, 24.9601059);
        LatLng Olympia = new LatLng(60.1607413, 24.9566449);
*/
/*      mMap.addMarker(new MarkerOptions().position(DeliCafeMaya).title("Deli Café Maya")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_cafe_20)));
        mMap.addMarker(new MarkerOptions().position(Kaivopuisto).title("Kaivopuisto")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_park_20)));
        mMap.addMarker(new MarkerOptions().position(MikaelAgr).title("Mikael Agricola")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_church_20)));
        mMap.addMarker(new MarkerOptions().position(Observatory).title("Observatory")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_culture_20)));
        mMap.addMarker(new MarkerOptions().position(Ursa).title("Ursa")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_viewpoint_20)));
        mMap.addMarker(new MarkerOptions().position(Mannerheim).title("Mannerheim Museum")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_museum_20)));
        mMap.addMarker(new MarkerOptions().position(Olympia).title("Olympia Terminal")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_harbor_20)));

        mMap.addMarker(new MarkerOptions().position(DeliCafeMaya).title("Deli Café Maya")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_eating_20)));
        mMap.addMarker(new MarkerOptions().position(Kaivopuisto).title("Kaivopuisto")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoor_20)));
        mMap.addMarker(new MarkerOptions().position(MikaelAgr).title("Mikael Agricola")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_culture_20)));
        mMap.addMarker(new MarkerOptions().position(Observatory).title("Observatory")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_culture_20)));
        mMap.addMarker(new MarkerOptions().position(Ursa).title("Ursa")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoor_20)));
        mMap.addMarker(new MarkerOptions().position(Mannerheim).title("Mannerheim Museum")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_culture_20)));
        mMap.addMarker(new MarkerOptions().position(Olympia).title("Olympia Terminal")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoor_20))); */

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(NEAR_HKI)           // Sets the center of the map to fit the UI overlay on screen
                .zoom(14)                   // Sets the zoom to fit the UI overlay on screen
                .bearing(180)               // Sets the orientation of the camera to south
                .build();                   // Creates a CameraPosition from the builder
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Resources res = getResources();
        latArray = res.obtainTypedArray(R.array.lat);
        lngArray = res.obtainTypedArray(R.array.lng);
        conf_colors = res.getIntArray(R.array.conf_colors);
        conf_markers = res.getIntArray(R.array.conf_markers);

        //Test consistency of array lengths
        //MISSING

    }

    public void drawMarkers(View view) {

        mMap.clear();
        if (screenCount<conf_colors.length) { // For each screen
            int markersPerScreen = 0;

            // Count the number markers to be drawn for this screen
            for (int i = colorCount; i < (colorCount + conf_colors[screenCount]); i++) {
                markersPerScreen = markersPerScreen + conf_markers[i];
            }

            for (markerCount = markerStart; markerCount < markerStart + markersPerScreen; markerCount++) { // For each marker in this screen

                // Scale marker locations to the area covered by radar/map
                float lat = (float) (HELSINKI.latitude - 0.008 + (0.016 * latArray.getFloat(markerCount, 1)));
                float lng = (float) (HELSINKI.longitude - 0.016059 + (0.032118 * lngArray.getFloat(markerCount, 1)));


                // Crashes if this is included
//            mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + i)
//                    .icon(BitmapDescriptorFactory.fromResource(typeArray.getResourceId(i, -1))));
// or                    .icon(BitmapDescriptorFactory.fromResource(getResources().getIdentifier(typeArray.getString(i), null, getPackageName()))));

                // Draw markers with the appropriate color i.e. check from conf_color if there was 2nd and 3rd color for this screen and from conf_markers how many markers to print with each color
                if (markerCount < (markerStart + conf_markers[colorCount])) // If should use the first color
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_eating_20)));
                else if (conf_colors[screenCount] > 1 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount+1]) // If should use the second color
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_culture_20)));
                else if (conf_colors[screenCount] > 2 && markerCount < markerStart + conf_markers[colorCount] + conf_markers[colorCount+1] + conf_markers[colorCount+2]) // If should use the third color
                    mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker " + markerCount)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_outdoor_20)));
            }
            markerStart = markerStart + markersPerScreen;
//            markersPerScreenCount = markersPerScreenCount + markersPerScreen;
            colorCount = colorCount + conf_colors[screenCount];
            screenCount++;
        }

        mMap.addMarker(new MarkerOptions().position(HELSINKI).title("Your location"));
        mMap.addMarker(new MarkerOptions().position(WEST).title("West"));
        mMap.addMarker(new MarkerOptions().position(SOUTH).title("South"));

        mImages.clear();
        mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.radar_sq));

        // Add a radar UI overlay at Punavuori, Helsinki.
        mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(mImages.get(mCurrentEntry)).anchor(0.495f, 0.5f)
                .position(HELSINKI, 1969f, 1969f)
                .bearing(180));
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        latArray.recycle();
        lngArray.recycle();
    }
}
