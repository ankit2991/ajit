package com.messaging.textrasms.manager.feature.compose

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.model.Attachments
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import java.net.URLDecoder
import javax.inject.Named

@Module
class ComposeActivityModule {
    @Provides
    @Named("fromgroup")
    fun providefrom(activity: ComposeActivity): Boolean =
        activity.intent.extras?.getBoolean("fromgroup")
            ?: false

    @Provides
    @Named("query")
    fun provideQuery(activity: ComposeActivity): String = activity.intent.extras?.getString("query")
        ?: ""

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: ComposeActivity): Long =
        activity.intent.extras?.getLong("threadId")
            ?: 0L

    @Provides
    @Named("addresses")
    fun provideAddresses(activity: ComposeActivity): List<String> {
        return activity.intent
            ?.decodedDataString()
            ?.substringAfter(':') // Remove scheme
            ?.replaceAfter("?", "") // Remove query
            ?.split(",")
            ?: listOf()
    }

    @Provides
    @Named("text")
    fun provideSharedText(activity: ComposeActivity): String {
        return activity.intent.extras?.getString(Intent.EXTRA_TEXT)
            ?: activity.intent.extras?.getString("sms_body")
            ?: activity.intent?.decodedDataString()
                ?.substringAfter('?') // Query string
                ?.split(',')
                ?.firstOrNull { param -> param.startsWith("body") }
                ?.substringAfter('=')
            ?: ""
    }

    @Provides
    @Named("attachments")
    fun provideSharedAttachments(activity: ComposeActivity): Attachments {
        val sharedImages = mutableListOf<Uri>()
        activity.intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.run(sharedImages::add)
        activity.intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            ?.run(sharedImages::addAll)
        return Attachments(sharedImages.map { Attachment.Image(it) })
    }

    @Provides
    @IntoMap
    @ViewModelKey(ComposeViewModel::class)
    fun provideComposeViewModel(viewModel: ComposeViewModel): ViewModel = viewModel

    // The dialer app on Oreo sends a URL encoded string, make sure to decode it
    private fun Intent.decodedDataString(): String? {
        val data = data?.toString()
        if (data?.contains('%') == true) {
            return URLDecoder.decode(data, "UTF-8")
        }
        return data
    }

}