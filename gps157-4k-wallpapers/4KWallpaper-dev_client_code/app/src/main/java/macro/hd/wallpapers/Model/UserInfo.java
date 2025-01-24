package macro.hd.wallpapers.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserInfo implements IModel, Serializable {

	@SerializedName("like")
	@Expose
	private List<Wallpapers> like = null;

	@SerializedName("download")
	@Expose
	private List<String> download = null;

	public List<Category> getCategory() {
		return category;
	}

	@SerializedName("category")
	@Expose
	private List<Category> category = null;

	public List<Category> getCategory_feature() {
		return category_feature;
	}

	@SerializedName("category_feature")
	@Expose
	private List<Category> category_feature = null;

	public List<Category> getLive_category() {
		return live_category;
	}

	@SerializedName("live_category")
	@Expose
	private List<Category> live_category = null;

	public List<Category> getStock_category() {
		return stock_category;
	}

	@SerializedName("stock_category")
	@Expose
	private List<Category> stock_category = null;

	public List<Wallpapers> getSplash() {
		return splash;
	}

	@SerializedName("splash")
	@Expose
	private List<Wallpapers> splash = null;

	@SerializedName("view")
	@Expose
	private List<String> view = null;

	public List<AppSettings> getApp_settings() {
		return app_settings;
	}

	public void setApp_settings(List<AppSettings> app_settings) {
		this.app_settings = app_settings;
	}

	@SerializedName("app_list")
	@Expose
	private List<AppSettings> app_settings = null;

	public List<Wallpapers> getLike() {
		if(like==null)
			like=new ArrayList<>();
		return like;
	}

	private List<Wallpapers> like_live = null;

	public void setLike_live(List<Wallpapers> like_live) {
		this.like_live = like_live;
	}

	public List<Wallpapers> getLikeLive() {
		if(like_live==null)
			like_live=new ArrayList<>();
		return like_live;
	}

	private List<Wallpapers> like_exclusive = null;

	public void setLike(List<Wallpapers> like) {
		this.like = like;
	}

	public List<String> getDownload() {
		return download;
	}

	public void setDownload(List<String> download) {
		this.download = download;
	}


	public List<String> getView() {
		return view;
	}

	public void setView(List<String> view) {
		this.view = view;
	}


	public String getUser_id() {
		return user_id;
	}

	public String getCountry_code() {
		return country_code;
	}

	@SerializedName("user_id")
	@Expose
	private String user_id = null;

	public String getIs_pro() {
		return is_pro;
	}

	@SerializedName("is_pro")
	@Expose
	private String is_pro = null;

	@SerializedName("country_code")
	@Expose
	private String country_code = null;

	public List<Wallpapers> getLike_exclusive() {
		if(like_exclusive==null)
			like_exclusive=new ArrayList<>();
		return like_exclusive;
	}

	public void setLike_exclusive(List<Wallpapers> like_exclusive) {
		this.like_exclusive = like_exclusive;
	}
}
