package macro.hd.wallpapers.Interface.Activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import macro.hd.wallpapers.R

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        findViewById<TextView>(R.id.continue_ads).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_terms).setOnClickListener {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(

                        "https://socem.com/Service_Terms.html"
                    )
                )
            )
        }
        findViewById<TextView>(R.id.tv_policies).setOnClickListener {

            startActivity(

                Intent(

                    Intent.ACTION_VIEW, Uri.parse(

                        "https://socem.com/privacy-policy-apps.html"
                    )
                )
            )
        }

        findViewById<Button>(R.id.btn_buy).setOnClickListener {

            finish()
        }
    }
}