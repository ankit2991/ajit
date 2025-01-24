package com.lockerroom.face.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.lockerroom.face.R;

public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        TextView abc = findViewById(R.id.abc);
        abc.setText(Html.fromHtml(getString(R.string.terms)));
        ((MaterialToolbar) findViewById(R.id.top_appbar)).setNavigationOnClickListener(v -> finish());
    }
}