package macro.hd.wallpapers.Model;


public class LeftDrawerItem {

    private String catName, title, catId;
    private int imgResID;

    public LeftDrawerItem(String catName, String catId, int imgResID) {
        setCatName(catName);
        setCatId(catId);
        setImgResID(imgResID);
        setTitle(null);
    }
    
    public LeftDrawerItem(String catName, int imgResID) {
        setCatName(catName);
        setCatId("0");
        setImgResID(imgResID);
        setTitle(null);
    }

    public LeftDrawerItem(String catName, String catId) {
        setCatName(catName);
        setCatId(catId);
        setImgResID(0);
        setTitle(null);
    }

//	public DrawerItem(String title) {
//		setTitle(title);
//		setCatId(null);
//		setImgResID(0);
//	}

    public LeftDrawerItem(String title) {
        setCatName(title);
        setCatId(null);
        setImgResID(0);
    }

    public String getCatName() {
        return catName;
    }

    public String getTitle() {
        return title;
    }

    public String getCatId() {
        return catId;
    }

    public int getImgResID() {
        return imgResID;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }



	
	/*public DrawerItem(JSONObject obj) {

		try {
			setCatcid(obj.getString(JsonParams.categoryid));
		} catch (Exception e) {
			setCatcid("0");
		}

		try {

			setCatname(obj.getString(JsonParams.categoryname));

		} catch (Exception e) {

			setCatname("");
		}

		try {

			setCatdescription(obj.getString(JsonParams.description));

		} catch (Exception e) {

			setCatdescription("");
		}

	}
*/
}
