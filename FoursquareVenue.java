package fi.aalto.ming.uitestscreens;

import com.google.android.gms.maps.model.LatLng;

public class FoursquareVenue {
    private String name;
    private String city;
    private String id;
    private String category;
    private float lat;
    private float lng;

    public FoursquareVenue() {
        this.name = "";
        this.city = "";
        this.setCategory("");
    }
    public FoursquareVenue(String nameArg, String categoryArg, float latArg, float lngArg) {
        this.name = nameArg;
        this.city = "";
        this.id = "";
        this.setCategory(categoryArg);
        this.lat = latArg;
        this.lng = lngArg;
    }

    public String getCity() {
        if (city.length() > 0) {
            return city;
        }
        return city;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city.replaceAll("\\(", "").replaceAll("\\)", "");
            ;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String venueid) {
        this.id = venueid;
    }

    public void setLat(String latitude) {
        this.lat = Float.parseFloat(latitude);
    }

    public float getLat() {
        return lat;
    }

    public void setLng(String longitude) {
        this.lng = Float.parseFloat(longitude);
    }

    public float getLng() {
        return lng;
    }

    public LatLng getLatLng () { return new LatLng((double) lat, (double) lng); }
}