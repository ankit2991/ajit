package macro.hd.wallpapers.Model;

import java.io.Serializable;

/**
 * Created by hungam on 30/1/19.
 */

public class Images implements Serializable {


    public boolean isVideo() {
        return isVideo;
    }

    private boolean isVideo;
    public Images(String path, boolean isSelected) {
        this.path = path;
        this.isSelected = isSelected;
    }

    public Images(String path, boolean isSelected,boolean isVideo) {
        this.path = path;
        this.isSelected = isSelected;
        this.isVideo = isVideo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    private String path;
    private boolean isSelected;
}
