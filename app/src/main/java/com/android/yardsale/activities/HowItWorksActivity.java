package com.android.yardsale.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.yardsale.R;
import com.android.yardsale.adapters.CustomPagerAdapter;

public class HowItWorksActivity extends ActionBarActivity {

    private TextView btnSignUp;
    private TextView btnLogin;
    private CustomPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);

        btnSignUp = (TextView) findViewById(R.id.btnSignUp);
        btnLogin = (TextView) findViewById(R.id.btnLogin);
        btnLogin.setTextColor(Color.WHITE);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowItWorksActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnSignUp.setTextColor(Color.WHITE);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HowItWorksActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        adapter = new CustomPagerAdapter(this);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_how_it_works, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
