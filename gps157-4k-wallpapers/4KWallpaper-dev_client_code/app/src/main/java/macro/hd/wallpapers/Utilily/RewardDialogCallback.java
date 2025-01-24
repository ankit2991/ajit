package macro.hd.wallpapers.Utilily;

import macro.hd.wallpapers.Interface.Fragments.RewardedDialog;

public interface RewardDialogCallback {
	public void setWatchAdListener(RewardedDialog rewardedDialog);
	public void setLikeAdsListener(RewardedDialog rewardedDialog);
	public void setRemoveAdListener(RewardedDialog rewardedDialog);
}
