package com.joblancr.activitiesAndAdapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.joblancr.cards.NotificationCard;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private List<NotificationCard> notifications;
    private RecyclerView rv;
    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private LinearLayoutManager llm;

    private SessionManager sessionManager;
    private AsyncTask getNotifications;
    NotAdapter adapter;

    public static Activity notificationActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        notificationActivity = this;

        //check LoginActivity
        sessionManager = new SessionManager(NotificationActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initializing
        notifications = new ArrayList<>();
        llm = new LinearLayoutManager(this);

        rv = (RecyclerView) findViewById(R.id.notication_recycler);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        getNotifications = new getNotifications().execute("getNotifications", userDetails.get("id"));
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

        getNotifications.cancel(true);
    }

    class getNotifications extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar notProgBar;

        private static final String NOTIFICATION_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Notification.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_NOTIFICATIONS = "notifications";
        private static final String TAG_NOT_DESC = "not_desc";
        private static final String TAG_NOT_TIME = "not_time";
        private static final String TAG_IMAGE = "image_name";
        private static final String TAG_USER_ID = "user_id";

        @Override
        protected void onPreExecute() {
            notProgBar = (ProgressBar) findViewById(R.id.notProgBar);
            notProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);

                Log.d("Request", "Starting");

                json = jsonParser.makeHttpRequest(
                        NOTIFICATION_URL, "POST", params);

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
            Log.d("Message", "NotificationActivity: getNotifications AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            notProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }

                    JSONArray notifs = json.getJSONArray(TAG_NOTIFICATIONS);

                    for(int i = 0; i < notifs.length(); i++) {
                        JSONObject notificationObj = (JSONObject) notifs.get(i);
                        notifications.add(new NotificationCard(notificationObj.getString(TAG_IMAGE),
                                notificationObj.getString(TAG_NOT_DESC),
                                notificationObj.getString(TAG_NOT_TIME),
                                notificationObj.getString(TAG_USER_ID)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                adapter = new NotAdapter(notifications);
                rv.setAdapter(adapter);
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(NotificationActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(NotificationActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
