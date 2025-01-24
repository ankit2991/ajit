package callerid.truecaller.trackingnumber.phonenumbertracker.block.near_by_place;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjust.sdk.Adjust;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

public class NearByPlaceActivity extends AppCompatActivity {
    public static View.OnClickListener myOnClickListener;
    private String str = "bike store";
    private ArrayList<DataModel2> placedata = new ArrayList<>();
    private CustomAdapterNearby adapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private SFun sfun;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation = null;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Adjust.onResume();

        final FrameLayout app_ad = (FrameLayout) findViewById(R.id.app_ad);
        MaxAdManager.INSTANCE.createNativeAd(
                this,
                app_ad,
                "GPS119_Native_Small_flag"
        );
    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                        }
                        Boolean coarseLocationGranted = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        }
                        if (fineLocationGranted != null && fineLocationGranted) {
                            getLocation();
                            // Precise location access granted.

                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            getLocation();
                        } else {
                            // No location access granted.
                        }
                    }
            );


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        currentLocation = location;
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_place);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });


        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        myOnClickListener = new MyOnClickListener(this);

        String str3 = "aquarium";
        placedata.add(new DataModel2("Aquarium", str3));
        String str = "airport";
        placedata.add(new DataModel2("Airport", str));
        String str4 = "art_gallery";
        placedata.add(new DataModel2("Art gallery", str4));
        String str9 = "bicycle_store";
        placedata.add(new DataModel2("Bicycle store", str9));
        String str6 = "bakery";
        placedata.add(new DataModel2("Bakery", str6));
        String str16 = "car_repair";
        placedata.add(new DataModel2("Car repair", str16));
        String str12 = "bus_station";
        placedata.add(new DataModel2("Bus station", str12));
        String str10 = "book_store";
        placedata.add(new DataModel2("Book store", str10));
        String str13 = "cafe";
        placedata.add(new DataModel2("Cafe", str13));
        String str18 = "casino";
        placedata.add(new DataModel2("Casino", str18));
        String str15 = "car_rental";
        placedata.add(new DataModel2("Car rental", str15));
        String str7 = "Bank";
        placedata.add(new DataModel2(str7, "bank"));
        String str22 = "church";
        placedata.add(new DataModel2("Church", str22));
        String str19 = "cemetery";
        placedata.add(new DataModel2("Cemetery", str19));
        String str21 = "city_hall";
        placedata.add(new DataModel2("City hall", str21));
        String str27 = "doctor";
        placedata.add(new DataModel2("Doctor", str27));
        String str24 = "courthouse";
        placedata.add(new DataModel2("Courthouse", str24));
        String str25 = "dentist";
        placedata.add(new DataModel2("Dentist", str25));
        placedata.add(new DataModel2("Meal delivery", "meal_delivery"));
        placedata.add(new DataModel2("Locksmith", "locksmith"));
        String str20 = "clothing_store";
        placedata.add(new DataModel2("Clothing store", str20));
        String str8 = "beauty_salon";
        placedata.add(new DataModel2("Beauty salon", str8));
        String str14 = "car_dealer";
        placedata.add(new DataModel2("Car dealer", str14));
        placedata.add(new DataModel2("Gas station", "gas_station"));
        placedata.add(new DataModel2("Library", "library"));
        String str29 = "electronics_store";
        placedata.add(new DataModel2("Electronics store", str29));
        placedata.add(new DataModel2("Embassy", "embassy"));
        String str17 = "car_wash";
        placedata.add(new DataModel2("Car wash", str17));
        String str5 = "atm";
        placedata.add(new DataModel2("Atm", str5));
        placedata.add(new DataModel2("Hospital", "hospital"));
        placedata.add(new DataModel2("Museum", "museum"));
        placedata.add(new DataModel2("Movie rental", "movie_rental"));
        String str2 = "amusement_park";
        placedata.add(new DataModel2("Amusement park", str2));
        String str26 = "department_store";
        placedata.add(new DataModel2("Department store", str26));
        String str23 = "convenience_store";
        placedata.add(new DataModel2("Convenience store", str23));
        placedata.add(new DataModel2("Home goods store", "home_goods_store"));
        placedata.add(new DataModel2("Mosque", "mosque"));
        placedata.add(new DataModel2("Insurance agency", "insurance_agency"));
        placedata.add(new DataModel2("Hindu temple", "hindu_temple"));
        placedata.add(new DataModel2("Hair care", "hair_care"));
        placedata.add(new DataModel2("Hardware store", "hardware_store"));
        placedata.add(new DataModel2("Gym", "gym"));
        placedata.add(new DataModel2("Furniture store", "furniture_store"));
        String str28 = "electrician";
        placedata.add(new DataModel2("Electrician", str28));
        String str11 = "bowling_alley";
        placedata.add(new DataModel2("Bowling alley", str11));
        placedata.add(new DataModel2("Jewelry store", "jewelry_store"));
        placedata.add(new DataModel2("Fire station", "fire_station"));
        placedata.add(new DataModel2("School", "school"));
        placedata.add(new DataModel2("Movie theater", "movie_theater"));
        placedata.add(new DataModel2("Roofing", "roofing_contractor"));
        placedata.add(new DataModel2("Shoes store", "shoe_store"));
        placedata.add(new DataModel2("Real State", "real_estate_agency"));
        placedata.add(new DataModel2("Park", "park"));
        placedata.add(new DataModel2("Plumber", "plumber"));
        placedata.add(new DataModel2("Restaurant", "restaurant"));
        placedata.add(new DataModel2("Post office", "post_office"));
        placedata.add(new DataModel2("Pharmaceutic", "pharmacy"));
        placedata.add(new DataModel2("Physiotherapist", "physiotherapist"));
        placedata.add(new DataModel2("Police", "police"));
        placedata.add(new DataModel2("Zoo", "zoo"));
        placedata.add(new DataModel2("Pet store", "pet_store"));
        placedata.add(new DataModel2("Subway station", "subway_station"));
        placedata.add(new DataModel2("Veterinary", "veterinary_care"));
        placedata.add(new DataModel2("Supermarket", "supermarket"));
        placedata.add(new DataModel2("Travel agency", "travel_agency"));
        placedata.add(new DataModel2("Stadium", "stadium"));
        placedata.add(new DataModel2("Taxi", "taxi_stand"));
        placedata.add(new DataModel2("Mall", "shopping_mall"));
        placedata.add(new DataModel2("Train", "train_station"));
        placedata.add(new DataModel2("Painter", "painter"));
        placedata.add(new DataModel2("Spa", "spa"));
        placedata.add(new DataModel2("Parking", "parking"));
        placedata.add(new DataModel2("Club", "night_club"));
        placedata.add(new DataModel2("Rv park", "rv_park"));
//        placedata.add(new DataModel2("Local government office", "local_government_office"));
        adapter = new CustomAdapterNearby(this, placedata);
        recyclerView.setAdapter(adapter);
    }


    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            if (currentLocation == null) {
                Toast.makeText(NearByPlaceActivity.this, "Location Permisson not granted", Toast.LENGTH_LONG).show();
            } else {

                int selectedItemPosition = recyclerView.getChildPosition(v);
                RecyclerView.ViewHolder viewHolder
                        = recyclerView.findViewHolderForPosition(selectedItemPosition);
                String str2 = placedata.get(selectedItemPosition).getName();
                StringBuilder sb = new StringBuilder();
                sb.append("geo:%f,%f");
                sb.append(currentLocation.getLatitude());
                sb.append(",");
                sb.append(currentLocation.getLongitude());
                sb.append("?q=");
                sb.append(str2);
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(sb.toString()));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

             /*   if (isGoogleMapsInstalled()) {
                    startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NearByPlaceActivity.this);
                    builder.setMessage("Google Maps Not available. Please Install or Enable Google Maps");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Install", getGoogleMapsListener());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }*/
            }
        }
    }

    @Override
    public void onBackPressed() {

        Utils.ad_count++;
        if (Utils.defaultValue <= Utils.ad_count) {
            Utils.ad_count = 0;
            Utils.showInter(this, new adShowCallBack() {
                @Override
                public void adShown(Boolean bol) {
                    finish();
                }

                @Override
                public void adFailed(Boolean fal) {
                    finish();
                }
            });

        } else {
            finish();
        }


    }

    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public DialogInterface.OnClickListener getGoogleMapsListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                startActivity(intent);

            }
        };
    }


}
