package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



public class SearchMobileNumActivity extends AppCompatActivity {
    private static final String TAG = "akki";
    private EditText et_mobile;
    private String mobile_no;
    private String search_no;
    private GetSearchDetailTask get_search_detail_task;
    private ProgressDialog mProgressDialog;
    private String operator_name = "";
    private String state_name = "";
    private int mono;
    private int icon_value;
    private String latitude;
    private String longitude;
    private Handler data_handler = new C04731();
    private String contact_name;
    private GoogleMap google_map;
    private TextView txt_operator_name;
    private TextView txt_state_name;
    private TextView txt_contact_name;
    private ImageView img_operator;
    private TextView mobile_num;
    private SQLiteAdapter sqlite_adapter;
    private String mobilenumber;
    private CardView cardinfo;
    private SFun sfun;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_mobile_num);
        sfun = new SFun(this);
        et_mobile = findViewById(R.id.et_mobile);
        cardinfo = findViewById(R.id.cardinfo);
        sqlite_adapter = new SQLiteAdapter(SearchMobileNumActivity.this);
        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        try {
            sqlite_adapter.openToRead();
        } catch (Exception e) {

        }
        findViewById(R.id.btnsearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobilenumber = et_mobile.getText().toString();
                if (!mobilenumber.isEmpty()) {
                    if (mobilenumber.length() == 10) {
                        try {
                            SearchData(mobilenumber);
                        } catch (Exception e) {
                        }
                    } else {
                        et_mobile.setError("Please Enter A Valid Number");
                    }
                } else {
                    et_mobile.setError("Enter 10 Digit Mobile Number");
                }
            }
        });
        txt_operator_name = findViewById(R.id.txt_operator_name);
        txt_state_name = findViewById(R.id.txt_state_name);

        txt_contact_name = findViewById(R.id.txt_contact_name);
        img_operator = findViewById(R.id.img_operator);
        mobile_num = findViewById(R.id.mobile_num);


    }

    protected void SearchData(String mobileno) {
        this.mobile_no = mobileno;
        try {
            try {
                search_no = this.mobile_no.substring(0, 4).trim();
            } catch (Exception e) {
            }
        } catch (
                Exception e) {
        }
        try {
            SearchProcess();
        } catch (
                Exception e) {
        }
    }


    @SuppressLint("MissingPermission")
    private void SearchProcess() {
        get_search_detail_task = new GetSearchDetailTask();
        get_search_detail_task.execute(new String[0]);
    }


    public class GetSearchDetailTask extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
            showProgressBar();
        }

        public String doInBackground(String... args) {
            try {
                operator_name = "";
                state_name = "";
                Cursor cursor = sqlite_adapter.GetSearchResultByMobileno(search_no);

                if (cursor != null) {
                    startManagingCursor(cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            mono = cursor.getInt(cursor.getColumnIndex(SQLiteAdapter.KEY_MOBILE_NO));
                            operator_name = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_OPERATOR_NAME));
                            state_name = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_STATE_NAME));
                            icon_value = cursor.getInt(cursor.getColumnIndex(SQLiteAdapter.KEY_ICON_VALUE));
                            latitude = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_LATITUDE));
                            longitude = cursor.getString(cursor.getColumnIndex(SQLiteAdapter.KEY_LONGITUDE));
                        } while (cursor.moveToNext());
                    }
                }
                data_handler.sendMessage(data_handler.obtainMessage(0));
            } catch (Exception e) {
                e.printStackTrace();
                data_handler.sendMessage(data_handler.obtainMessage(1));
            }
            return null;
        }

        protected void onPostExecute(String result) {
        }
    }

    public void showProgressBar() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
            return;
        }
        mProgressDialog = new ProgressDialog(SearchMobileNumActivity.this);
        mProgressDialog.show();
    }

    public void dismissProgressBar() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                cardinfo.setVisibility(View.VISIBLE);

            }
        });
    }

    class C04731 extends Handler {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (operator_name.length() <= 0 || state_name.length() <= 0) {
                        et_mobile.setError("Phone number not found.");
                        et_mobile.setText("");
                    } else {
                        LoadOperatorIcon();
                        txt_operator_name.setText(operator_name);
                        txt_state_name.setText(state_name);
                        mobile_num.setText(mobilenumber);
                    }
                    getContactname();
                    showMap();
                    dismissProgressBar();
                    return;
                case 1:
                    dismissProgressBar();
                    return;
                case 99:
                    dismissProgressBar();
                    return;
                default:
                    return;
            }
        }
    }

    public void getContactname() {
        try {
            this.contact_name = "";
            this.mobile_no = et_mobile.getText().toString().trim();
            Uri contact_obj = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(this.mobile_no));
            Cursor contact_obj2 = getApplicationContext().getContentResolver().query(contact_obj, new String[]{"display_name"}, this.mobile_no, null, null);
            if (contact_obj2.moveToFirst()) {
                this.contact_name = contact_obj2.getString(contact_obj2.getColumnIndex("display_name"));
            }
            contact_obj2.close();
            if (contact_name.length() > 0) {
                txt_contact_name.setVisibility(View.VISIBLE);
                txt_contact_name.setText(this.contact_name);
                return;
            }
            txt_contact_name.setText("Name Not Available.");
        } catch (Exception e) {
            this.contact_name = "";
        }
    }

    private void showMap() {
        if (latitude == null && longitude == null) {
            latitude = String.valueOf(21.20481811);
            longitude = String.valueOf(72.84110427);
        }
        double lat = Double.parseDouble(this.latitude);
        double longi = Double.parseDouble(this.longitude);
        final String address = this.state_name;
        final LatLng position = new LatLng(lat, longi);


        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                google_map = googleMap;
                googleMap.clear();
                MarkerOptions options = new MarkerOptions();
                options.position(position);
                options.title("Position");
                options.snippet(address);
                google_map.addMarker(options);
                CameraUpdate updatePosition = CameraUpdateFactory.newLatLng(position);
                CameraUpdate updateZoom = CameraUpdateFactory.zoomTo(5.0f);
                google_map.moveCamera(updatePosition);
                google_map.animateCamera(updateZoom);
            }
        });
    }

    protected void LoadOperatorIcon() {
        Bitmap operator_bitmap = null;
        if (this.icon_value == 1) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_aircel);
        } else if (this.icon_value == 2) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_airtel);
        } else if (this.icon_value == 3) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bsnlgsm);
        } else if (this.icon_value == 4) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bsnlgsm);
        } else if (this.icon_value == 5) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_vidile);
        } else if (this.icon_value == 6) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mtnldolphin);
        } else if (this.icon_value == 7) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_etisalatindia);
        } else if (this.icon_value == 8) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_idea);
        } else if (this.icon_value == 9) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_loopmobile);
        } else if (this.icon_value == 10) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mtsindia);
        } else if (this.icon_value == 11) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pingcdma);
        } else if (this.icon_value == 12) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_relagsm);
        } else if (this.icon_value == 13) {
            operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_relagsm);
        } else if (this.icon_value != 14) {
            if (this.icon_value == 15) {
                operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stel);
            } else if (this.icon_value == 16) {
                operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_ttewntyfour);
            } else if (this.icon_value == 17) {
                operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_taomo);
            } else if (this.icon_value == 18) {
                operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_tatcom);
            } else if (this.icon_value == 19) {
                operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_uni);
            } else if (this.icon_value != 20) {
                if (this.icon_value == 21) {
                    operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_virgsm);
                } else if (this.icon_value == 22) {
                    operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_vodaf);
                } else if (this.icon_value == 23) {
                    operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_datacom);
                } else if (this.icon_value == 24) {
                    operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_jeeio);

                } else {
                    operator_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_operator);
                }
            }
        }
        img_operator.setImageBitmap(operator_bitmap);
    }
}
