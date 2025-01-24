package macro.hd.wallpapers.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Wallpapers implements IModel, Serializable {

	public Object getNativeAd() {
		return nativeAd;
	}

	public void setNativeAd(Object nativeAd) {
		this.nativeAd = nativeAd;
	}

	transient private Object nativeAd;

	@SerializedName("post_id")
	@Expose
	private String postId;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("category")
	@Expose
	private String category;

	public String getDownload() {
		return download;
	}

	@SerializedName("download")
	@Expose
	private String download;


	public String getAuthor() {
		return author;
	}

	public String getSource_link() {
		return source_link;
	}

	public String getLicense() {
		return license;
	}

	@SerializedName("author")
	@Expose
	private String author;

	@SerializedName("source_link")
	@Expose
	private String source_link;

	@SerializedName("license")
	@Expose
	private String license;

	@SerializedName("img")
	@Expose
	private String img;

	@SerializedName("is_share")
	@Expose
	private String is_share;

	public String getCategory_title() {
		return category_title;
	}

	public void setCategory_title(String category_title) {
		this.category_title = category_title;
	}

	private String category_title;

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	private String tags;

	public String getSearch_thumb() {
		return search_thumb;
	}

	public void setSearch_thumb(String search_thumb) {
		this.search_thumb = search_thumb;
	}

	public String getSearch_small() {
		return search_small;
	}

	public void setSearch_small(String search_small) {
		this.search_small = search_small;
	}

	private String search_thumb,search_small;

	public String getVid() {
		return vid;
	}

	@SerializedName("vid")
	@Expose
	private String vid;

	public int getSr_no() {
		return sr_no;
	}

	private int sr_no;
	private String progress;

	public String getDownload_id() {
		return download_id;
	}

	public void setDownload_id(String download_id) {
		this.download_id = download_id;
	}

	private String download_id;

	public boolean isDownloading() {
		return isDownloading;
	}

	public void setDownloading(boolean downloading) {
		isDownloading = downloading;
	}

	private boolean isDownloading;


	public String getPostId() {
		return postId;
	}

	public void setPostId(String postId) {
		this.postId = postId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImg() {
	    return img;
	}

	public void setImg(String img) {
		this.img = img;
	}


	public void setSr_no(int sr_no) {
		this.sr_no = sr_no;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}


	public String getIs_share() {
		return is_share;
	}
}
