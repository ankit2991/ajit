package callerid.truecaller.trackingnumber.phonenumbertracker.block.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.util.formatSpanColor
import com.calldorado.Calldorado
import com.calldorado.Calldorado.acceptConditions


class OnBoardingAdapter(val onNextClicked : () -> Unit, val onFinished: () -> Unit) : PagerAdapter() {

    private val layouts = listOf(
        R.layout.onboarding_1,
        R.layout.onboarding_2,
        R.layout.onboarding_3,
    )

    override fun getCount(): Int = layouts.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
       return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(layouts[position], container, false)
        container.addView(view)

        if(position==0){
          val tvPrivacy=  view.findViewById<TextView>(R.id.tv_privacy)
            tvPrivacy.formatSpanColor(view.context.getString(R.string.privacy_des))
        }

        view.findViewById<View>(R.id.continue_btn).setOnClickListener {
            onNextClicked()
        }
        view.findViewById<ImageView>(R.id.btn_close).setOnClickListener {
            onFinished()
        }
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }


}