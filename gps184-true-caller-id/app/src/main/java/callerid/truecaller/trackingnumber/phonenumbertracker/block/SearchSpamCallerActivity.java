package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

public class SearchSpamCallerActivity extends AppCompatActivity {

    private EditText etSearch;
    private RequestQueue mRequestQueue;
    private LinearLayout mDefaultLayout;
    private LinearLayout mNotFoundLayout;
    private CardView mSpamLayout;
    private TextView tvPhone;
    private TextView tvSpamText;
    private ImageView imgIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_spam_caller);

        mRequestQueue = Volley.newRequestQueue(this);
        etSearch = findViewById(R.id.et_search);
        mDefaultLayout = findViewById(R.id.layout_default);
        mDefaultLayout.setVisibility(View.VISIBLE);
        mNotFoundLayout = findViewById(R.id.layout_not_found);
        mSpamLayout = findViewById(R.id.layout_spam_result);
        tvPhone = findViewById(R.id.tv_phone);
        tvSpamText = findViewById(R.id.tv_spam_text);
        imgIcon = findViewById(R.id.icon_spam);

        final FrameLayout app_ad = (FrameLayout) findViewById(R.id.app_ad);
        MaxAdManager.INSTANCE.createNativeAd(
                this,
                app_ad,
                "GPS119_Native_Small_flag"
        );

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
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

    private void performSearch() {
        etSearch.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

        if (etSearch.getText().length() == 0) {
            return;
        }

        mDefaultLayout.setVisibility(View.GONE);
        mSpamLayout.setVisibility(View.GONE);
        mNotFoundLayout.setVisibility(View.GONE);

        if (isNetworkAvailable(this)) {
            Dialog dialog = new Dialog(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
            dialog.setContentView(dialogView);
            dialog.setCancelable(false);
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            String url = "https://spamcheck.p.rapidapi.com/index.php?number=" + etSearch.getText();
            JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Volts", "onResponse() -> " + response.toString());

                    String res = response.optString("response");
                    if (res.toLowerCase().equals("Not Spam".toLowerCase())) {
                        //mNotFoundLayout.setVisibility(View.VISIBLE);
                        mSpamLayout.setVisibility(View.VISIBLE);

                        imgIcon.setImageResource(R.drawable.ic_done_white);
                        tvSpamText.setText("NOT SPAM");
                        tvPhone.setText(etSearch.getText());
                    } else {
                        tvSpamText.setText("SPAM");
                        tvPhone.setText(etSearch.getText());
                        imgIcon.setImageResource(R.drawable.ic_dislike);
                        mSpamLayout.setVisibility(View.VISIBLE);
                        //mNotFoundLayout.setVisibility(View.GONE);
                    }

                    dialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volts", "onErrorResponse() -> " + error.getMessage());
                    mSpamLayout.setVisibility(View.GONE);
                    mNotFoundLayout.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-RapidAPI-Key", "68699c0e10msh5a316e510190fb9p162170jsn839f7d67656e");

                    return headers;
                }
            };

            mRequestQueue.add(req);
        } else {
            Toast.makeText(this, "internet is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            } else {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}