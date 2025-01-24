package com.messaging.textrasms.manager.feature.conversationinfo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.repository.SyncRepository
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.container_activity.*
import javax.inject.Inject

class ConversationInfoActivity : QkThemedActivity() {

    private lateinit var router: Router

    @Inject
    lateinit var syncManager: SyncRepository

    companion object {
        var isRefreshList = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)

        conversations.visibility = View.GONE
        backcompose.visibility = View.GONE
        isRefreshList = false
        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            val threadId = intent.extras?.getLong("threadId") ?: 0L
            router.setRoot(RouterTransaction.with(ConversationInfoController(threadId)))
        }
        showBackButton(true)
        backcompose.setOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRefreshList && router.hasRootController()) {
            isRefreshList = false
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Thread {
                    syncManager.syncContacts()
                }.start()
            }
        }
    }
}