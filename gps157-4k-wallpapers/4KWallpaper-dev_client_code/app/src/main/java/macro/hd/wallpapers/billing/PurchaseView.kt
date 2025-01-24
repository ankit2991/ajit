package macro.hd.wallpapers.billing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.billingclient.api.SkuDetails
import com.weewoo.sdkproject.events.EventHelper
import macro.hd.wallpapers.R

class PurchaseView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    val view: View
    var didClickPurchase: (() -> Unit)? = null
    private lateinit var tvPrice: TextView
    private val eventHelper = EventHelper()
    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.view_purchase, this, true)
        view.findViewById<ImageView>(R.id.imv_background).setOnClickListener {  }
        view.findViewById<TextView>(R.id.continue_ads).setOnClickListener {

            eventHelper.sendCTACancelEvent()
            this@PurchaseView.visibility = GONE
        }
        view.findViewById<TextView>(R.id.tv_terms).setOnClickListener {

            context.startActivity(
                Intent(

                    Intent.ACTION_VIEW, Uri.parse("https://socem.com/Service_Terms.html")
                )
            )
        }
        view.findViewById<TextView>(R.id.tv_policies).setOnClickListener {

            context.startActivity(

                Intent(

                    Intent.ACTION_VIEW, Uri.parse("https://socem.com/privacy-policy-apps.html")
                )
            )
        }

        view.findViewById<Button>(R.id.btn_buy).setOnClickListener {

            eventHelper.sendCTASubscribeEvent()
            didClickPurchase?.invoke()
        }

        tvPrice = view.findViewById(R.id.tv_price)
    }

    fun refreshView(skuDetailsList: MutableList<SkuDetails>) {

        skuDetailsList.forEach {

            val nText = "Enjoy a <b>3 Day Free Trial</b> - <b>then " + it.price + " / " + it.subscriptionPeriod + ".</b>"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                tvPrice.text = Html.fromHtml(nText, Html.FROM_HTML_MODE_LEGACY)
            } else {

                tvPrice.text = Html.fromHtml(nText)
            }
        }
    }
}