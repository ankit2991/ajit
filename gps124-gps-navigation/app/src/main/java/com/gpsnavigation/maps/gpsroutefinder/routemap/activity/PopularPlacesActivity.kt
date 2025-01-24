package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.gpslocation.utils.MapUtils.openPopularPlaces
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.PopularPlacesAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityPopularPlacesBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PopularPlacesModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.nearbyActionClickCount
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.EqualSpacingItemDecoration
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale.getDefault

class PopularPlacesActivity : BaseActivity(), View.OnClickListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    private var globalItem: PopularPlacesModel? = null

    lateinit var binding: ActivityPopularPlacesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopularPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        val popularPlacesAdapter = PopularPlacesAdapter(generateDataList()) { model: PopularPlacesModel ->
            globalItem = model
            nearbyActionClickCount += 1
            openPopularPlaces(this, model.query)
        }

        binding.rvPopularPlaces.apply {
            layoutManager = getGridLayoutManager()
            addItemDecoration(
                EqualSpacingItemDecoration(
                    resources.getDimensionPixelSize(R.dimen._10sdp),
                    resources.getDimensionPixelSize(R.dimen._16sdp)
                )
            )
            adapter = popularPlacesAdapter
        }

        binding.toolbar.title.text = getString(R.string.popular_places_activity_title)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium
    }

    private fun getGridLayoutManager(): RecyclerView.LayoutManager {
        val glm = GridLayoutManager(this, 6, GridLayoutManager.VERTICAL, false)
        glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(i: Int): Int {
                return if ((i + 1) % (PopularPlacesAdapter.ITEM_BLOCK_COUNT + 1) != 0) 2
                else 6
            }
        }
        return glm
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium
        binding.rvPopularPlaces.adapter?.notifyDataSetChanged()
    }

    private fun generateDataList(): List<PopularPlacesModel> =
        listOf(
            PopularPlacesModel(
                1L,
                R.drawable.ic_airplane_my_new,
                getString(R.string.airport),
                "airport"
            ),
            PopularPlacesModel(
                2L,
                R.drawable.ic_train_station,
                getString(R.string.train_station),
                "train_station"
            ),
            PopularPlacesModel(
                3L,
                R.drawable.ic_bus_stop_new,
                getString(R.string.bus_station),
                "bus_station"
            ),
            PopularPlacesModel(
                4L,
                R.drawable.ic_bank_new,
                getString(R.string.bank),
                "bank"
            ),
            PopularPlacesModel(
                5L,
                R.drawable.ic_atm_new,
                getString(R.string.atm),
                "atm"
            ),
            PopularPlacesModel(
                6L,
                R.drawable.ic_money_exchange_new,
                getString(R.string.real_estate_agency),
                "real_estate_agency"
            ),
            PopularPlacesModel(
                7L,
                R.drawable.ic_mosque_new,
                getString(R.string.mosque),
                "mosque"
            ),
            PopularPlacesModel(
                8L,
                R.drawable.ic_temple_new,
                getString(R.string.temple),
                "hindu_temple"
            ),
            PopularPlacesModel(
                9L,
                R.drawable.ic_doctor_new,
                getString(R.string.doctor),
                "doctor"
            ),
            PopularPlacesModel(
                10L,
                R.drawable.ic_veterinary,
                getString(R.string.veterinary_care),
                "veterinary_care"
            ),
            PopularPlacesModel(
                11L,
                R.drawable.ic_dentist_new,
                getString(R.string.dentist),
                "dentist"
            ),
            PopularPlacesModel(
                12L,
                R.drawable.ic_pharmacy_new,
                getString(R.string.pharmacy),
                "pharmacy"
            ),
            PopularPlacesModel(
                13L,
                R.drawable.ic_fire_station_new,
                getString(R.string.fire_station),
                "fire_station"
            ),
            PopularPlacesModel(
                14L,
                R.drawable.ic_pertrol_pump_new,
                getString(R.string.gas_station),
                "gas_station"
            ),
            PopularPlacesModel(
                15L,
                R.drawable.ic_services_station,
                getString(R.string.car_wash),
                "car_wash"
            ),
            PopularPlacesModel(
                16L,
                R.drawable.ic_car_repair,
                getString(R.string.car_repair),
                "car_repair"
            ),
            PopularPlacesModel(
                17L,
                R.drawable.ic_coffee_shop_new,
                getString(R.string.restaurant),
                "restaurant"
            ),
            PopularPlacesModel(
                18L,
                R.drawable.ic_coffee_new,
                getString(R.string.cafe),
                "cafe"
            ),
            PopularPlacesModel(
                19L,
                R.drawable.ic_shopping_mall_new,
                getString(R.string.shopping_mall),
                "shopping_mall"
            ),
            PopularPlacesModel(
                20L,
                R.drawable.ic_grocery_store_new,
                getString(R.string.department_store),
                "department_store"
            ),
            PopularPlacesModel(
                21L,
                R.drawable.ic_jawalry_show_new,
                getString(R.string.jewelry_store),
                "jewelry_store"
            ),
            PopularPlacesModel(
                22L,
                R.drawable.ic_night_club,
                getString(R.string.night_club),
                "night_club"
            ),
            PopularPlacesModel(
                23L,
                R.drawable.ic_casino_new,
                getString(R.string.casino),
                "casino"
            ),
            PopularPlacesModel(
                24L,
                R.drawable.ic_beauty_salon_new,
                getString(R.string.beauti_saloon),
                "beauty_salon"
            ),
            PopularPlacesModel(
                25L,
                R.drawable.ic_bar_club,
                getString(R.string.bar),
                "bar"
            ),
            PopularPlacesModel(
                26L,
                R.drawable.ic_zoo_new,
                getString(R.string.zoo),
                "zoo"
            ),
            PopularPlacesModel(
                27L,
                R.drawable.ic_perfomaing_art,
                getString(R.string.movie_theater),
                "movie_theater"
            ),
            PopularPlacesModel(
                28L,
                R.drawable.ic_school_new,
                getString(R.string.school),
                "school"
            ),
            PopularPlacesModel(
                29L,
                R.drawable.ic_book_store,
                getString(R.string.book_store),
                "book_store"
            ),
            PopularPlacesModel(
                30L,
                R.drawable.ic_library_new,
                getString(R.string.library),
                "library"
            ),
            PopularPlacesModel(
                31L,
                R.drawable.ic_stadium_new,
                getString(R.string.stadium),
                "stadium"
            ),
            PopularPlacesModel(
                32L,
                R.drawable.ic_park_new,
                getString(R.string.park),
                "park"
            ),
            PopularPlacesModel(
                33L,
                R.drawable.ic_aquarium_new,
                getString(R.string.aquarium),
                "aquarium"
            ),
            PopularPlacesModel(
                34L,
                R.drawable.ic_camping_new,
                getString(R.string.campground),
                "campground"
            ),
            PopularPlacesModel(
                35L,
                R.drawable.ic_city_hall_new,
                getString(R.string.city_hall),
                "city_hall"
            ),
            PopularPlacesModel(
                36L,
                R.drawable.ic_museum_new,
                getString(R.string.museum),
                "museum"
            )
        )

    fun filter(list: ArrayList<PopularPlacesModel>, text: String): ArrayList<PopularPlacesModel> {
        var query = text

        if (!TextUtils.isEmpty(query)) {
            query = query.toLowerCase(getDefault())
            val filteredPlacesList = ArrayList<PopularPlacesModel>();

            for (p in list) {
                if (p.title.toLowerCase(getDefault()).contains(query)) {
                    filteredPlacesList.add(p);
                }
            }
            return filteredPlacesList
        } else {
            return list
        }

    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        binding.rvPopularPlaces.forEach {
            if (it is FrameLayout) MaxAdManager.destroyNativeAd(it)
        }
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    private fun setInset() {
        binding.rvPopularPlaces.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            (view.layoutParams as FrameLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
        }
    }
}
