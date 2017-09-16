package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
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

public class WriteReviewActivity extends AppCompatActivity {

    private TextView projectTitle, projectBudget, projectLocation;
    private EditText ratingEditText, commentEditText;
    private Button saveReviewBtn;
    private String project_id;

    private SessionManager sessionManager;
    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private Bundle extras;
    private AsyncTask saveReview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        sessionManager = new SessionManager(WriteReviewActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initialize views
        projectTitle = (TextView) findViewById(R.id.project_title);
        projectBudget = (TextView) findViewById(R.id.project_budget);
        projectLocation = (TextView) findViewById(R.id.project_location);
        ratingEditText = (EditText) findViewById(R.id.rating);
        commentEditText = (EditText) findViewById(R.id.comment);
        saveReviewBtn = (Button) findViewById(R.id.saveReviewBtn);

        extras = getIntent().getExtras();

        if(extras != null) {
            project_id = extras.getString("project_id");

            projectTitle.setText(getResources().getString(R.string.label_project_title)+": "+
                    extras.getString("project_title"));
            projectBudget.setText(getResources().getString(R.string.label_desc_budget)+": "+
                    Html.fromHtml(extras.getString("project_budget")));
            projectLocation.setText(getResources().getString(R.string.label_project_location)+": "+
                    extras.getString("project_location"));
        }

        saveReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ratingEditText.getText().toString().equals("")) {
                    Toast.makeText(WriteReviewActivity.this, "Rating is empty", Toast.LENGTH_SHORT).show();
                } else if(Integer.parseInt(ratingEditText.getText().toString()) > 5) {
                    Toast.makeText(WriteReviewActivity.this, "Rating cant be more than 5", Toast.LENGTH_SHORT).show();
                } else {
                    saveReview = new saveReview().execute("saveReview", project_id, commentEditText.getText().toString(),
                            ratingEditText.getText().toString(), userDetails.get("id"));
                }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
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

        if(saveReview != null) {
            saveReview.cancel(true);
        }
    }

    class saveReview extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar writeRevProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Review.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            writeRevProgBar = (ProgressBar) findViewById(R.id.writeRevProgBar);
            writeRevProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);
                params.put("comment", args[2]);
                params.put("rating_value", args[3]);
                params.put("user_id", args[4]);

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
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            writeRevProgBar.setVisibility(View.GONE);

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
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(WriteReviewActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(WriteReviewActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
