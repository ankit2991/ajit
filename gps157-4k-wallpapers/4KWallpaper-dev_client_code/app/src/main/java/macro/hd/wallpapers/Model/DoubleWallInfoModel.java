package macro.hd.wallpapers.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DoubleWallInfoModel implements IModel, Serializable {



	@SerializedName("post")
	@Expose
	private List<DoubleWallpaper> post = null;



	public List<DoubleWallpaper> getPost() {
		return post;
	}

	@SerializedName("status")
	@Expose
	private String status;
	@SerializedName("msg")
	@Expose
	private String msg;


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
