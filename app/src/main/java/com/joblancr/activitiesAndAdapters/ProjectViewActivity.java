package com.joblancr.activitiesAndAdapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.joblancr.cards.Bids;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectViewActivity extends AppCompatActivity {
    static String project_title, project_id, project_user_id;
    static int project_status;
    private List<Bids> bids;
    private RecyclerView rv;

    private TextView project_category, project_days, project_description, skills_text, noBids;
    private Button bidBtn;

    private String category = "", description = "", days = "", skills = "", title = "",
            budget = "", localg = "", state = "";

    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private AsyncTask getDetails, getBidders, reportProject = null, deleteProject = null;

    SessionManager sessionManager;

    private boolean userBidded;
    BidAdapter adapter;

    public static Activity projectViewActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        projectViewActivity = this;

        //initialize session manager
        sessionManager = new SessionManager(ProjectViewActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initialize views
        project_category = (TextView) findViewById(R.id.project_category);
        project_days = (TextView) findViewById(R.id.project_days);
        project_description = (TextView) findViewById(R.id.project_description);
        skills_text = (TextView) findViewById(R.id.skills_text);
        noBids = (TextView) findViewById(R.id.noBids);

        bids = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            project_title = extras.getString("project_title");
            project_id = extras.getString("project_id");
            project_user_id = extras.getString("project_user_id");
            project_status = Integer.parseInt(extras.getString("project_status"));
        }

        this.setTitle(project_title);
        getDetails = new getProjectDetails().execute("getProjectDetails", project_id, userDetails.get("id"));

        rv = (RecyclerView) findViewById(R.id.bids_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        getBidders = new getBidders().execute("getBidders", project_id);

        bidBtn = (Button) findViewById(R.id.bidBtn);

        //check if user owns the project
        if(userDetails.get("id").equals(project_user_id)) {
            //hide bid bottom
            bidBtn.setVisibility(View.GONE);
            //reset margin of the scrollview above bid bottom
            NestedScrollView aboveBidBtn = (NestedScrollView) findViewById(R.id.aboveBidBtn);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) aboveBidBtn.getLayoutParams();
            params.bottomMargin = 0;
            aboveBidBtn.setLayoutParams(params);
            aboveBidBtn.requestLayout();
        } else {
            bidBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gotoBid = new Intent(v.getContext(), BidActivity.class);
                    gotoBid.putExtra("project_id", project_id);
                    gotoBid.putExtra("project_title", title);
                    gotoBid.putExtra("project_budget", budget);
                    gotoBid.putExtra("project_location", localg+", "+state);
                    v.getContext().startActivity(gotoBid);
                    finish();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

        getBidders.cancel(true);
        getDetails.cancel(true);

        if(deleteProject != null) {
            deleteProject.cancel(true);
        }

        if(reportProject != null) {
            reportProject.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            //show the up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(userDetails.get("id").equals(project_user_id)) {

            if(project_status == 1) {
                getMenuInflater().inflate(R.menu.menu_project_view_1, menu);
            } else if(project_status == 0) {
                getMenuInflater().inflate(R.menu.menu_project_view_3, menu);
            }
        } else {
            getMenuInflater().inflate(R.menu.menu_project_view_2, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }

        if (id == R.id.action_delete_project) {
            new AlertDialog.Builder(ProjectViewActivity.this)
                    .setTitle(getResources().getString(R.string.title_delete_project))
                    .setMessage(getResources().getString(R.string.delete_project_msg))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //continue with delete
                            deleteProject = new deleteProject().execute("deleteProject",project_id,userDetails.get("id"));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        if (id == R.id.action_report_project) {
            final EditText reason = new EditText(ProjectViewActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            reason.setLayoutParams(lp);

            new AlertDialog.Builder(ProjectViewActivity.this)
                    .setTitle(getResources().getString(R.string.title_report_project))
                    .setMessage(getResources().getString(R.string.report_project_msg))
                    .setView(reason)
                    .setPositiveButton(getResources().getString(R.string.report), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //continue with report
                            reportProject = new reportProject().execute("reportProject", project_id, userDetails.get("id"),
                                    reason.getText().toString());
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }

        if (id == R.id.action_write_review) {
            Intent gotoReview = new Intent(ProjectViewActivity.this, WriteReviewActivity.class);
            gotoReview.putExtra("project_id", project_id);
            gotoReview.putExtra("project_title", title);
            gotoReview.putExtra("project_budget", budget);
            gotoReview.putExtra("project_location", localg+", "+state);
            startActivity(gotoReview);
        }

        return super.onOptionsItemSelected(item);
    }

    class getProjectDetails extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private static final String GET_PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/LoadProject.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_CATEGORY = "category";
        private static final String TAG_DESC = "description";
        private static final String TAG_DURATION = "days";
        private static final String TAG_SKILLS = "skills";
        private static final String TAG_TITLE = "p_title";
        private static final String TAG_BUDGET = "budget_name";
        private static final String TAG_LOCALG_NAME = "localg_name";
        private static final String TAG_STATE_NAME = "state_name";
        private static final String TAG_USER_BIDDED = "userBidded";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);
                params.put("user_id", args[2]);

                Log.d("Request", "Starting");

                json = jsonParser.makeHttpRequest(
                        GET_PROJECT_URL, "POST", params);

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

            String nSkills = "";

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    } else {
                        category = json.getString(TAG_CATEGORY);
                        description = json.getString(TAG_DESC);
                        days = json.getString(TAG_DURATION);
                        skills = json.getString(TAG_SKILLS);
                        title = json.getString(TAG_TITLE);
                        budget = json.getString(TAG_BUDGET);
                        localg = json.getString(TAG_LOCALG_NAME);
                        state = json.getString(TAG_STATE_NAME);
                        userBidded = json.getBoolean(TAG_USER_BIDDED);

                        String[] Sklls = skills.split(",");
                        for(String s : Sklls) {
                            s = s.trim();
                            nSkills += s;
                            nSkills +="\n\n";
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                project_category.setText(category);
                project_days.setText(days);
                project_description.setText(description);
                skills_text.setText(nSkills);

                if(userBidded) {
                    bidBtn.setText(getResources().getString(R.string.label_user_bidded));
                    bidBtn.setEnabled(false);
                }
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProjectViewActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProjectViewActivity.this, "Failed to Retrieve Project Details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class getBidders extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar proViewProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_BIDDERS = "bidders";
        private static final String TAG_BIDDER_ID = "user_id";
        private static final String TAG_OWNER_ID = "owner_id";
        private static final String TAG_PROJECT_ID = "project_id";
        private static final String TAG_BID_ID = "bid_id";
        private static final String TAG_BIDDER_NAME = "full_name";
        private static final String TAG_OFFER = "offer_text";
        private static final String TAG_BIDDER_PIC = "image";
        private static final String TAG_DATE_CREATED = "date_created";
        private static final String TAG_BIDDERS_EXIST = "bidders_exist";

        @Override
        protected void onPreExecute() {
            proViewProgBar = (ProgressBar) findViewById(R.id.proViewProgBar);
            proViewProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);

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
            int b_exist = 0;

            proViewProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    } else {
                        b_exist = json.getInt(TAG_BIDDERS_EXIST);
                    }

                    JSONArray bidrs = json.getJSONArray(TAG_BIDDERS);

                    for(int i = 0; i < bidrs.length(); i++) {
                        JSONObject bidderObj = (JSONObject) bidrs.get(i);
                        bids.add(new Bids(bidderObj.getString(TAG_BIDDER_PIC),bidderObj.getString(TAG_BIDDER_ID),
                                bidderObj.getString(TAG_OWNER_ID),bidderObj.getString(TAG_PROJECT_ID),
                                bidderObj.getString(TAG_BIDDER_NAME),bidderObj.getString(TAG_DATE_CREATED),
                                bidderObj.getString(TAG_OFFER), bidderObj.getString(TAG_BID_ID)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                if(b_exist == 1) {
                    adapter = new BidAdapter(bids);
                    rv.setAdapter(adapter);
                } else {
                    noBids.setVisibility(View.VISIBLE);
                }

            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProjectViewActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProjectViewActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class deleteProject extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ProjectViewActivity.this);
            pDialog.setMessage("Deleting Project...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);
                params.put("user_id", args[2]);

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

            /*if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
                pDialog = null;
            }*/

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
                Intent i = new Intent(ProjectViewActivity.this, SelectCategoryActivity.class);
                startActivity(i);
                finish();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProjectViewActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProjectViewActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class reportProject extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ProjectViewActivity.this);
            pDialog.setMessage("Deleting Project...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("project_id", args[1]);
                params.put("user_id", args[2]);
                params.put("reason", args[3]);

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

            /*if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
                pDialog = null;
            }*/

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
                Toast.makeText(ProjectViewActivity.this,
                        getResources().getString(R.string.project_reported_msg), Toast.LENGTH_SHORT).show();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProjectViewActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProjectViewActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
