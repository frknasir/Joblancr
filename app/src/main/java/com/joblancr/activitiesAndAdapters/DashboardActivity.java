package com.joblancr.activitiesAndAdapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private ImageView userImage;
    private TextView userName, userEmail, latestText, latestTime, tipText;

    private NavigationView navigationView;
    private View headerView;
    private Menu nav_menu;

    private String[] tips;
    private int n;

    private SessionManager sessionManager;

    private AsyncTask getProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check LoginActivity
        sessionManager = new SessionManager(DashboardActivity.this);
        sessionManager.checkLogin();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        headerView = navigationView.inflateHeaderView(R.layout.nav_header_home);
        navigationView.setNavigationItemSelectedListener(this);

        nav_menu = navigationView.getMenu();

        //initializing
        userImage = (ImageView) headerView.findViewById(R.id.userImage);
        userName = (TextView) headerView.findViewById(R.id.userName);
        userEmail = (TextView) headerView.findViewById(R.id.userEmail);
        latestText = (TextView) findViewById(R.id.latest_text);
        latestTime = (TextView) findViewById(R.id.latest_time);
        tipText = (TextView) findViewById(R.id.quick_tip_text);

        //display tip
        Timer t = new Timer();
        t.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        displayTip();
                    }
                },
                0,
                300000
        );

        userDetails = sessionManager.getUserDetails();

        userName.setText(userDetails.get("name"));
        userEmail.setText(userDetails.get("email"));

        //get profile from database
        getProfile = new getProfile().execute("getProfile", userDetails.get("id"));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        //.setAction("Action", null).show();
                Intent newProjectIntent = new Intent(DashboardActivity.this, NewProjectActivity.class);
                startActivity(newProjectIntent);
            }
        });
    }

    public void displayTip() {
        Random generator = new Random();

        tips = getResources().getStringArray(R.array.tips);
        n = generator.nextInt(tips.length);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipText.setText(tips[n]);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the DashboardActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        } else if(id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("Message", "onPause called!");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Message", "onStop called!");

        getProfile.cancel(true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_inbox) {
            Intent inboxIntent = new Intent(DashboardActivity.this, NegotiationActivity.class);
            startActivity(inboxIntent);
        } else if (id == R.id.nav_notification) {
            Intent notificationIntent = new Intent(DashboardActivity.this, NotificationActivity.class);
            startActivity(notificationIntent);
        } else if (id == R.id.nav_project) {
            Intent projectIntent = new Intent(DashboardActivity.this, ProjectActivity.class);
            startActivity(projectIntent);
        } else if (id == R.id.nav_browse) {
            Intent hiredIntent = new Intent(DashboardActivity.this, SelectCategoryActivity.class);
            startActivity(hiredIntent);
        } else if (id == R.id.nav_profile) {
            Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(profileIntent);
        } else if (id == R.id.nav_share) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            i.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.action_share_msg));
            startActivity(Intent.createChooser(i, "Share via"));
        } else if(id == R.id.nav_logout) {
            sessionManager.logoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    class getProfile extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar latestUpdProgBar;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_PROFILE_EXISTS = "profile_exists";
        private static final String TAG_NAME = "full_name";
        private static final String TAG_EMAIL = "email";
        private static final String TAG_IMAGE = "image";
        private static final String TAG_NOT_COUNT = "not_count";
        private static final String TAG_LAST_ACTIVITY = "last_activity";
        private static final String TAG_LAST_ACTIVITY_TIME = "last_activity_time";

        @Override
        protected void onPreExecute() {
            latestUpdProgBar = (ProgressBar) findViewById(R.id.latestUpdProgBar);
            latestUpdProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);

                Log.d("Request", "Starting");

                try {
                    json = jsonParser.makeHttpRequest(
                            PROFILE_URL, "POST", params);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(json != null) {
                    Log.d("Response: ", "> " + json.toString());
                }

                else {
                    Log.e("JSON Data", "Didnt receive any data from server");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onCancelled() {
            Log.d("Message", "DashboardActivity: getProfile AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            int profileExists = 1;
            String errormsg = "";
            String image = null;
            String full_name = "";
            String email = "";
            String lastActivity = "";
            String lastActivityTime = "";
            int notCount = 0;

            latestUpdProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                    profileExists = json.getInt(TAG_PROFILE_EXISTS);

                    if(profileExists == 1) {
                        image = json.getString(TAG_IMAGE);
                        full_name = json.getString(TAG_NAME);
                        email = json.getString(TAG_EMAIL);
                        lastActivity = json.getString(TAG_LAST_ACTIVITY);
                        lastActivityTime = json.getString(TAG_LAST_ACTIVITY_TIME);
                    }
                    notCount = json.getInt(TAG_NOT_COUNT);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                if(full_name != null || !full_name.isEmpty()) {
                    userName.setText(full_name);
                }

                if(email != null || !email.isEmpty()) {
                    userEmail.setText(email);
                }

                nav_menu.findItem(R.id.nav_notification).setTitle(getResources().getString(R.string.nav_notification_title)+"("+notCount+")");

                if(!lastActivity.equals("")) {
                    latestText.setText(lastActivity);
                }

                if(!lastActivityTime.equals("")) {
                    latestTime.setText(lastActivityTime);
                }

                if(profileExists == 1) {
                    if(image != "0") {
                        new DownloadImageTask(userImage)
                                .execute("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+userDetails.get("id")+"/"+image);
                    }
                }
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(DashboardActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(DashboardActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            bmImage.setImageBitmap(result);
        }
    }
}
