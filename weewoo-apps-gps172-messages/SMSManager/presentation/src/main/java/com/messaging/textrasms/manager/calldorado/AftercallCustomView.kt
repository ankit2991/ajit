package com.messaging.textrasms.manager.calldorado

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calldorado.ui.views.custom.CalldoradoCustomView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapterPersonalBR
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.model.Conversation
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject


class AftercallCustomView(context: Context?) : CalldoradoCustomView(context) {
    var rcv: RecyclerView? = null
    var conversationsAdapter: ConversationsAdapterPersonalBR

    @Inject
    lateinit var navigator: Navigator

    init {
        Log.e("calldorado>>","view after call")

        val formatter = DateFormatter(
            context!!
        )
        appComponent.inject(this)

        conversationsAdapter = ConversationsAdapterPersonalBR(context, formatter, navigator)
    }


    override fun getRootView(): View {
        val rl =
            View.inflate(context, R.layout.calldorado_screen, linearViewGroup) as LinearLayout
        conversationsAdapter.isCalldorado = false
        rcv = rl.findViewById(R.id.rcvList)
        rcv?.layoutManager = LinearLayoutManager(context)

        rcv?.setItemViewCacheSize(10);
        val recycledViewPool = RecyclerView.RecycledViewPool()
        rcv?.setRecycledViewPool(recycledViewPool)
        Thread {}
        val list = loadData()
        Log.d("RealmResults__", "getRootView: " + list.size)
        conversationsAdapter.updateData(list)
        rcv?.adapter = conversationsAdapter


       /* val adFrame: FrameLayout = rl.findViewById(R.id.adFrame)
        val advertiseHandler = AdvertiseHandler.getInstance(context)
        advertiseHandler.loadBannerAds(
            context,
            UtilsData.banner_ad_unit_id,
            adFrame, object : AdListener() {

            })
*/


        return rl
    }

    private fun loadData(): RealmResults<Conversation> {

        /*When we add layout to calldorado then only limited item will be shown
       * so add logic in which only first 10 item is shown on screen
       *
       * */
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .limit(4)
            .findAll()
    }


}