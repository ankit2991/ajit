package callerid.truecaller.trackingnumber.phonenumbertracker.block.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import callerid.truecaller.trackingnumber.phonenumbertracker.block.InAppsActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.StartPage
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils
import com.calldorado.Calldorado
import com.calldorado.Calldorado.getAcceptedConditions

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        setupOnBoarding()
    }

    private fun setupOnBoarding() {
        val viewPager = findViewById<ViewPager>(R.id.view_page_onboard)
        val onBoardingAdapter = OnBoardingAdapter(
            onNextClicked = {
                if (viewPager.currentItem < viewPager.adapter!!.count - 1) {
                    viewPager.currentItem = viewPager.currentItem + 1
                } else {
                    screenNavigate()
                }
            },
            onFinished = {
                screenNavigate()
            })
        viewPager.adapter = onBoardingAdapter

    }

    private fun screenNavigate() {
        eulaAccepted(this)
        TinyDB.getInstance(this).setNotFirstLaunchApp()
        val shouldShowPaymentCard =
            Utils.getRemoteConfig().getBoolean(Utils.REMOTE_KEY_PAYMENT_CARD_FLAG)
        Log.d("Remote_config", "payment_card_flag: $shouldShowPaymentCard")
        if (shouldShowPaymentCard && !TinyDB.getInstance(this).weeklyPurchased()) {
            val intent = Intent(this, InAppsActivity::class.java)
                .setAction(Utils.ACTION_FROM_ON_BOARDING)
                .putExtra(Utils.EXTRA_FROM_ON_BOARDING, true)
            startActivity(intent)
        } else {
            val intent = Intent(this, StartPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
        finish()
    }


    private fun eulaAccepted(context: Context) {
        val conditionsMap: HashMap<Calldorado.Condition, Boolean> = HashMap()
        conditionsMap[Calldorado.Condition.EULA] = true
        conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
        Calldorado.acceptConditions(context, conditionsMap)

        Log.d("getAcceptedConditions", "onCreate: " + getAcceptedConditions(this))
    }
}