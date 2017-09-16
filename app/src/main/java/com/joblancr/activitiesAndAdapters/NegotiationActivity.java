package com.joblancr.activitiesAndAdapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.joblancr.helpers.NegotiationTabsPagerAdapter;
import com.joblancr.helpers.SessionManager;

import java.util.HashMap;

public class NegotiationActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;

    public static Activity negotiationActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negotiation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        negotiationActivity = this;

        //initialize session manager
        sessionManager = new SessionManager(NegotiationActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        viewPager = (ViewPager) findViewById(R.id.negotiation_pager);
        viewPager.setAdapter(new NegotiationTabsPagerAdapter(getSupportFragmentManager(), NegotiationActivity.this));

        tabLayout = (TabLayout) findViewById(R.id.negotiation_tabs);
        tabLayout.setupWithViewPager(viewPager);

        //Adding onTabSelectedListener to swipe views

        tabLayout.addOnTabSelectedListener(this);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
