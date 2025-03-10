package com.messaging.textrasms.manager.feature.gallery

import android.Manifest
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.model.MmsPart
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.gallery_activity.*
import javax.inject.Inject

class GalleryActivity : QkThemedActivity(), GalleryView {

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var pagerAdapter: GalleryPagerAdapter

    val partId by lazy { intent.getLongExtra("partId", 0L) }

    private val optionsItemSubject: Subject<Int> = PublishSubject.create()
    private val pageChangedSubject: Subject<MmsPart> = PublishSubject.create()
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[GalleryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)
        showBackButton(true)
        viewModel.bindView(this)

        pager.adapter = pagerAdapter
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                this@GalleryActivity.onPageSelected(position)
            }
        })

        pagerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                pagerAdapter.data?.takeIf { pagerAdapter.itemCount > 0 }
                    ?.indexOfFirst { part -> part.id == partId }
                    ?.let { index ->
                        onPageSelected(index)
                        pager.setCurrentItem(index, false)
                        pagerAdapter.unregisterAdapterDataObserver(this)
                    }
            }
        })
        showBackButton(true)
    }

    fun onPageSelected(position: Int) {
        toolbarSubtitle.text = pagerAdapter.getItem(position)?.messages?.firstOrNull()?.date
            ?.let(dateFormatter::getDetailedTimestamp)
        toolbarSubtitle.isVisible = toolbarTitle.text.isNotBlank()

        pagerAdapter.getItem(position)?.run(pageChangedSubject::onNext)
    }

    override fun render(state: GalleryState) {
        toolbar.setVisible(state.navigationVisible)

        title = state.title
        pagerAdapter.updateData(state.parts)
    }

    override fun optionsItemSelected(): Observable<Int> = optionsItemSubject

    override fun screenTouched(): Observable<*> = pagerAdapter.clicks

    override fun pageChanged(): Observable<MmsPart> = pageChangedSubject

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gallery, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                try {
                    menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                        resources.getColor(R.color.white),
                        PorterDuff.Mode.SRC_IN
                    )
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> optionsItemSubject.onNext(item.itemId)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        pagerAdapter.destroy()
    }

}