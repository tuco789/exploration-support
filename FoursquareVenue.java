package fi.aalto.ming.uitestscreens;

public class FoursquareVenue {
    private String name;
    private String city;

    private String category;
    private float lat;
    private float lng;

    public FoursquareVenue() {
        this.name = "";
        this.city = "";
        this.setCategory("");
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
}