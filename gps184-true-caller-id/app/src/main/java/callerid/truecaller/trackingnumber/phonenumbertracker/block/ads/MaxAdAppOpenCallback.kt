package callerid.truecaller.trackingnumber.phonenumbertracker.block.ads

interface MaxAdAppOpenCallback {
    fun onAdLoaded() {}
    fun onAdDisplayed() {}
    fun onAdHidden() {}
    fun onAdClicked() {}
    fun onAdLoadFailed() {}
    fun onAdDisplayFailed() {}
}