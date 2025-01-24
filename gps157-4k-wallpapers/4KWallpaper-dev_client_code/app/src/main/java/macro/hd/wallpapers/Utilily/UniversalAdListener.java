package macro.hd.wallpapers.Utilily;

/**
 * Created by Admin on 24-02-2017.
 */

public interface UniversalAdListener {
	public void onAdLoaded();
	public void onAdFailedToLoad(int errorCode);
	public void onAdClosed() ;

}
