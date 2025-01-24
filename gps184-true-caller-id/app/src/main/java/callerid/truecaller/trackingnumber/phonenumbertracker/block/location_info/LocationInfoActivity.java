package callerid.truecaller.trackingnumber.phonenumbertracker.block.location_info;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



public class LocationInfoActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;


    private TextView currentAddTv, currentcity, currentstate, currentcountry, currentpostal, currentlati, currentlongi;

    private Location currentLocation;

    private LocationCallback locationCallback;
    private ProgressDialog pd;
    private LocationManager lm;
    private boolean gps_enabled;
    private boolean network_enabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_info);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        lm = (LocationManager) LocationInfoActivity.this.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = false;
        network_enabled = false;


        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        currentAddTv = findViewById(R.id.current_address);
        currentcity = findViewById(R.id.current_city);
        currentstate = findViewById(R.id.current_state);
        currentcountry = findViewById(R.id.current_country);
        currentpostal = findViewById(R.id.current_pin);
        currentpostal = findViewById(R.id.current_pin);
        currentlati = findViewById(R.id.current_latitude);
        currentlongi = findViewById(R.id.current_longitude);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                currentLocation = locationResult.getLocations().get(0);
                Geocoder geocoder = new Geocoder(LocationInfoActivity.this, Locale.getDefault());

                List<Address> addresses = null;
                if (currentLocation != null) {
                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addresses != null) {
                        showResults(addresses);
                    }
                } else {
                    Toast.makeText(LocationInfoActivity.this, "Something wen wrong, Check your location setting.", Toast.LENGTH_SHORT).show();
                }

            }

            ;
        };


        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(LocationInfoActivity.this)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1000);
                        }
                    })
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        } else {
            startLocationUpdates();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {
        pd = new ProgressDialog(LocationInfoActivity.this);
        pd.setMessage("Fetching Current Location Information.");
        pd.setTitle("Please Wait");
        pd.setCancelable(false);
        pd.show();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null);
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Location permission not granted, " +
                                    "restart the app if you want the feature",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }


    private void showResults(List<Address> currentAdd) {
        try {
            currentAddTv.setText(currentAdd.get(0).getAddressLine(0));
            currentcity.setText(currentAdd.get(0).getLocality());
            currentstate.setText(currentAdd.get(0).getAdminArea());
            currentcountry.setText(currentAdd.get(0).getCountryName());
            currentpostal.setText(currentAdd.get(0).getPostalCode());
            String lati = String.valueOf(currentAdd.get(0).getLatitude());
            String longi = String.valueOf(currentAdd.get(0).getLongitude());
            currentlati.setText(lati);
            currentlongi.setText(longi);
        } catch (Exception e) {
            Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
        }
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onBackPressed() {
        Utils.ad_count++;
        if (Utils.defaultValue<=Utils.ad_count) {
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

        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }

            try {
                network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }
            if (!gps_enabled && !network_enabled) {
                new AlertDialog.Builder(LocationInfoActivity.this)
                        .setMessage(R.string.gps_network_not_enabled)
                        .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton(R.string.Cancel, null)
                        .show();
            } else {
                startLocationUpdates();
            }
        }
    }
}