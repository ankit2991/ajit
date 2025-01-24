package com.gpsnavigation.maps.gpsroutefinder.routemap.di

import androidx.annotation.Keep
import androidx.room.Room
import com.example.routesmap.viewModels.SubscriptionViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries.DataRepositry
import com.gps.maps.navigation.routeplanner.viewModels.EditRouteActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.room.RouteDb
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.RemoteConfigUtil
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.DisplayGeoGuessingResultViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


import kotlin.collections.HashMap

@Keep
object AppModule {
    val getModule = module {

        // Room Database
        single {
            Room.databaseBuilder(androidContext(), RouteDb::class.java, "myGPSNavigationDb").fallbackToDestructiveMigration().build()
        }
        single {
            FirebaseAnalytics.getInstance(get())
        }

        single { get<RouteDb>().routesTblDao() }

        single {
             TinyDB(get())
        }
        single {
             HashMap<String, RouteModel>()
        }
        //current Location
        single {
             LatLng()
        }

        single {
            RemoteConfigUtil.initializeConfigs()
        }


        single {
            DataRepositry(
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        viewModel { MainActivityViewModel(get()) }
        viewModel { EditRouteActivityViewModel(get()) }
        viewModel { SubscriptionViewModel(get()) }
        viewModel { DisplayGeoGuessingResultViewModel() }

    }
}