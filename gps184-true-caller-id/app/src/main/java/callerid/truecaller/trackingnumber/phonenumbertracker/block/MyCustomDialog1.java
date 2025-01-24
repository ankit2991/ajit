package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.adjust.sdk.Adjust;
import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.SQLiteAdapter;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.BuildConfig;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.PhonecallReceiver.nnn;

public class MyCustomDialog1 extends AppCompatActivity {
    TextView tv_client;
    String phone_no;
    LinearLayout dialog_ok;
    private int digits = 10;
    private int plus_sign_pos = 0;
    public static Activity a;
    private String searchnumber;
    private String mobile_no;
    private String search_no;
    private GetSearchDetailTask get_search_detail_task;
    private String operator_name;
    private String state_name;
    private SQLiteAdapter sqlite_adapter;
    private int mono;
    private int icon_value;
    private String latitude;
    private String longitude;
    private Handler data_handler = new C04731();
    private String TAG = "akki";
    private String contact_name;
    private TextView txt_contact_name;
    private ImageView img_operator;
    private TextView txt_state_name;
    private TextView txt_operator_name;
    private TextView mobile_num;

    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }
    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setFinishOnTouchOutside(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_layout);
        setWindowParams();
        a = this;
        sqlite_adapter = new SQLiteAdapter(MyCustomDialog1.this);
        sqlite_adapter.openToRead();
        img_operator = findViewById(R.id.imageViewCallScreen);
        txt_state_name = findViewById(R.id.textViewCallScreen);
        txt_operator_name = findViewById(R.id.operator);
        mobile_num = findViewById(R.id.mobilenumber);
        tv_client = (TextView) findViewById(R.id.textView1);
        dialog_ok = (LinearLayout) findViewById(R.id.button1);
        dialog_ok.bringToFront();
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        phone_no = nnn;
        if (phone_no != null) {
            tv_client.setText(phone_no);
            if (phone_no.contains("+")) {
                searchnumber = removeCountryCode(phone_no);
                if (searchnumber != null) {
                    SearchData(searchnumber);
                }
            } else {
                searchnumber = phone_no;
            }
        } else {
            tv_client.setText("No Information Available!!!");
        }
    }


    public void setWindowParams() {
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.dimAmount = 0;
        wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        getWindow().setAttributes(wlp);
    }

    private String removeCountryCode(String number) {
        if (hasCountryCode(number)) {
            int country_digits = number.length() - digits;
            number = number.substring(country_digits);
        }
        return number;
    }

    private boolean hasCountryCode(String number) {
        return number.charAt(plus_sign_pos) == '+';
    }

    protected void SearchData(String mobileno) {
        mobile_no = mobileno;
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

    class C04731 extends Handler {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    if (operator_name.length() <= 0 || state_name.length() <= 0) {
                        txt_operator_name.setText("No Data Available");
                        txt_state_name.setText("Cant Fetch State.");
                        mobile_num.setText(searchnumber);
                    } else {
                        LoadOperatorIcon();
                        txt_operator_name.setText(operator_name);
                        txt_state_name.setText(state_name);
                        mobile_num.setText(searchnumber);
                    }
                    getContactname();
                    return;
                case 1:
                    return;
                case 99:
                    return;
                default:
                    return;
            }
        }
    }

    public void getContactname() {
        try {
            contact_name = "";
            Uri contact_obj = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone_no));
            Cursor contact_obj2 = getApplicationContext().getContentResolver().query(contact_obj, new String[]{"display_name"}, phone_no, null, null);
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

    protected void LoadOperatorIcon() {
        int operator_bitmap = R.drawable.ic_aircel;
        if (this.icon_value == 1) {
            operator_bitmap = R.drawable.ic_aircel;
        } else if (this.icon_value == 2) {
            operator_bitmap = R.drawable.ic_airtel;
        } else if (this.icon_value == 3) {
            operator_bitmap = R.drawable.ic_bsnlgsm;
        } else if (this.icon_value == 4) {
            operator_bitmap = R.drawable.ic_bsnlgsm;
        } else if (this.icon_value == 5) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 6) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 7) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 8) {
            operator_bitmap = R.drawable.ic_idea;
        } else if (this.icon_value == 9) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 10) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 11) {
            operator_bitmap = R.drawable.ic_default_operator;
        } else if (this.icon_value == 12) {
            operator_bitmap = R.drawable.ic_relagsm;
        } else if (this.icon_value == 13) {
            operator_bitmap = R.drawable.ic_relagsm;
        } else if (this.icon_value != 14) {
            if (this.icon_value == 15) {
                operator_bitmap = R.drawable.ic_default_operator;
            } else if (this.icon_value == 16) {
                operator_bitmap = R.drawable.ic_default_operator;
            } else if (this.icon_value == 17) {
                operator_bitmap = R.drawable.ic_taomo;
            } else if (this.icon_value == 18) {
                operator_bitmap = R.drawable.ic_default_operator;
            } else if (this.icon_value == 19) {
                operator_bitmap = R.drawable.ic_uni;
            } else if (this.icon_value != 20) {
                if (this.icon_value == 21) {
                    operator_bitmap = R.drawable.ic_default_operator;
                } else if (this.icon_value == 22) {
                    operator_bitmap = R.drawable.ic_vodaf;
                } else if (this.icon_value == 23) {
                    operator_bitmap = R.drawable.ic_default_operator;
                } else if (this.icon_value == 24) {
                    operator_bitmap = R.drawable.ic_jeeio;

                } else {
                    operator_bitmap = R.drawable.ic_default_operator;
                }
            }
        }

        Picasso.get().load(operator_bitmap).into(img_operator);
    }
}