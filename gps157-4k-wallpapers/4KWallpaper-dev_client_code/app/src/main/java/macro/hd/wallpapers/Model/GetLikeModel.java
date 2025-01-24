package macro.hd.wallpapers.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GetLikeModel implements IModel, Serializable {


	@SerializedName("like")
	@Expose
	private List<Wallpapers> like = null;

	@SerializedName("like_live")
	@Expose
	private List<Wallpapers> like_live = null;

	public List<Wallpapers> getLike_exclusive() {
		return like_exclusive;
	}

	@SerializedName("like_exclusive")
	@Expose
	private List<Wallpapers> like_exclusive = null;


	@SerializedName("status")
	@Expose
	private String status;
	@SerializedName("msg")
	@Expose
	private String msg;


	public String getStatus() {
		return status;
	}



	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<Wallpapers> getLike_live() {
		return like_live;
	}

	public List<Wallpapers> getLike() {
		return like;
	}
}
