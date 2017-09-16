package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class BidActivity extends AppCompatActivity {

    private TextView project_title, project_budget, project_location;
    private EditText offerEditText;
    private Button bidBtn;
    private String project_id, title, budget, location, offer;

    private SessionManager sessionManager;
    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private AsyncTask makeBid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        sessionManager = new SessionManager(BidActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initializing views
        project_title = (TextView) findViewById(R.id.project_title);
        project_budget = (TextView) findViewById(R.id.project_budget);
        project_location = (TextView) findViewById(R.id.project_location);
        bidBtn = (Button) findViewById(R.id.bidBtn);
        offerEditText = (EditText) findViewById(R.id.offerEditText);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            project_id = extras.getString("project_id");
            title = extras.getString("project_title");
            budget = extras.getString("project_budget");
            location = extras.getString("project_location");

            project_title.setText(getResources().getString(R.string.label_project_title)+": "+ title);
            project_budget.setText(getResources().getString(R.string.label_desc_budget)+": "+Html.fromHtml(budget));
            project_location.setText(getResources().getString(R.string.label_project_location)+": "+location);
        } else {
            Intent i = new Intent(BidActivity.this, SelectCategoryActivity.class);
            startActivity(i);
        }

        bidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offer = offerEditText.getText().toString();
                makeBid = new makeBid().execute("bidProject", project_id, userDetails.get("id"), ""+offer);
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            //show the up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        makeBid.cancel(true);
    }

    class makeBid extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar bidProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            bidProgBar = (ProgressBar) findViewById(R.id.bidProgBar);
            bidProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);
                params.put("user_id", args[2]);
                params.put("offer", args[3]);

                Log.d("Request", "Starting");

                json = jsonParser.makeHttpRequest(
                        PROJECT_URL, "POST", params);

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
            Log.d("Message", "BidActivity: makeBid AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            bidProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                finish();
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(BidActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(BidActivity.this, "Failed to Make a Bid. Try Again",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
