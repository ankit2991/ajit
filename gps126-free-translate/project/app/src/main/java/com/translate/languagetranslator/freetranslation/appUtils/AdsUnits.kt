package com.translate.languagetranslator.freetranslation.appUtils


import androidx.annotation.Keep


@Keep
enum class AdsPriority {
    ADMOB,
    FACEBOOK,
    ADMOB_FACEBOOK,
    FACEBOOK_ADMOB
}

fun getAdsPriority(priority: String?): AdsPriority {

    return when (priority) {
        "am" -> {
            AdsPriority.ADMOB
        }
        "af" -> {
            AdsPriority.ADMOB_FACEBOOK
        }
        "fb" -> {
            AdsPriority.FACEBOOK
        }
        else -> {
            AdsPriority.FACEBOOK_ADMOB
        }
    }
}

/*@Keep

data class InterAdPair(
    var interAM: InterstitialAd? = null,
    var interFB: com.facebook.ads.InterstitialAd? = null
) {
    fun showAd(context: Context): Boolean {
        val isShow = when {
            interAM?.isLoaded == true || interFB?.isAdLoaded == true -> {
                interAM?.show() ?: interFB?.show()
                true
            }
            else -> false
        }
        return isShow
    }

    fun isLoaded() = interAM?.isLoaded == true || interFB?.isAdLoaded == true
}*/
