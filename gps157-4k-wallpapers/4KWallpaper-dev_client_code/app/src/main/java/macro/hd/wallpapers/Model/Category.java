package macro.hd.wallpapers.Model;

import java.io.Serializable;

public class Category implements IModel, Serializable {


	public Object getNativeAd() {
		return nativeAd;
	}

	public void setNativeAd(Object nativeAd) {
		this.nativeAd = nativeAd;
	}

	transient private Object nativeAd;


	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	private String name = null;

	public String getImg_feature() {
		return img_feature;
	}

	private String img_feature = null;

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	private String display_name = null;

	public void setImage(String image) {
		this.image = image;
	}

	private String image = null;

	public String getPriority() {
		return priority;
	}

	private String priority = null;

	public String getStatus() {
		return status;
	}

	private String status = null;

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	private String link = null;


	public String getCat_id() {
		return cat_id;
	}

	private String cat_id = null;

	public String getTags() {
		return tags;
	}

	private String tags = null;

	public String getImage_new() {
		return image_new;
	}

	private String image_new = "";

	public void setCat_id(String cat_id) {
		this.cat_id = cat_id;
	}

	private boolean isSelected;
	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}
}
