package fi.aalto.ming.uitestscreens;

/**
 * Created by svaittin on 18/10/2016.
 */

public class GridItem {
    private String imageURL;
    private String title;
    private boolean shouldRemove = false;

    public GridItem() {
        super();
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String image) {
        this.imageURL = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isShouldRemove() {
        return shouldRemove;
    }

    public void setShouldRemove(boolean shouldRemove) {
        this.shouldRemove = shouldRemove;
    }
}
