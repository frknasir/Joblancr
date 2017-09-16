package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.joblancr.cards.Project;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrowseActivity extends AppCompatActivity {

    private List<Project> projects;
    private RecyclerView rv;
    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private AsyncTask getProjects, loadProjects;

    private LinearLayoutManager llm;

    private boolean loading = true;
    private boolean firstload = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    private String cat_id;

    private SessionManager sessionManager;
    PRVAdapter adapter;

    private int offset = 0;
    private int limit = 10;
    private int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        //check LoginActivity
        sessionManager = new SessionManager(BrowseActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initializing
        projects = new ArrayList<>();
        loadProjects = null;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            cat_id = extras.getString("cat_id");
        }

        this.setTitle(getResources()
                .getStringArray(R.array.category_array)[Integer.parseInt(cat_id)-1]);

        rv = (RecyclerView) findViewById(R.id.project_recycler);

        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        getProjects = new getProjects().execute("loadprojects", userDetails.get("id"),
                ""+cat_id, ""+offset,""+limit);
        offset += limit;

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = llm.getChildCount();
                    totalItemCount = llm.getItemCount();
                    pastVisiblesItems = llm.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            firstload = false;
                            Log.v("...", "Last Item Wow !");
                            //Do pagination.. i.e. fetch new data
                            loadProjects = new getProjects().execute("loadprojects", userDetails.get("id"),
                                    ""+cat_id, ""+offset,""+limit);
                            if(offset < total) {
                                offset += limit;
                                loading = true;
                            }
                        }
                    }
                }
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            Intent refresh = new Intent(BrowseActivity.this, BrowseActivity.class);
            refresh.putExtra("cat_id", cat_id);

            refresh.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

            startActivity(refresh);
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_browse, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getProjects.cancel(true);

        if(loadProjects != null) {
            loadProjects.cancel(true);
        }
    }

    class getProjects extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar browseProgBar;

        private static final String LOAD_PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/LoadProject.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_TOTAL = "total";
        private static final String TAG_PROJECT = "projects";
        private static final String TAG_USER_ID = "user_id";
        private static final String TAG_PROJECT_ID = "p_id";
        private static final String TAG_TITLE = "p_title";
        private static final String TAG_BUDGET = "budget";
        private static final String TAG_DESC = "p_desc";
        private static final String TAG_DURATION = "p_duration";
        private static final String TAG_LOCALG_NAME = "localg_name";
        private static final String TAG_STATE_NAME = "state_name";
        private static final String TAG_BID = "bid";
        private static final String TAG_DATE_CREATED = "date_created";
        private static final String TAG_STATUS = "status";

        @Override
        protected void onPreExecute() {
            browseProgBar = (ProgressBar) findViewById(R.id.browseProgBar);
            browseProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);
                params.put("cat_id", args[2]);
                params.put("offset", args[3]);
                params.put("limit", args[4]);

                Log.d("Request", "Starting");

                json = jsonParser.makeHttpRequest(
                        LOAD_PROJECT_URL, "POST", params);

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
            Log.d("Message", "BrowseActivity: getProjects AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            browseProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                    total = json.getInt(TAG_TOTAL);

                    JSONArray projcts = json.getJSONArray(TAG_PROJECT);

                    for(int i = 0; i < projcts.length(); i++) {
                        JSONObject projectObj = (JSONObject) projcts.get(i);
                        projects.add(new Project(projectObj.getString(TAG_USER_ID),projectObj.getString(TAG_PROJECT_ID),
                                Html.fromHtml(projectObj.getString(TAG_TITLE)).toString(),
                                Html.fromHtml(projectObj.getString(TAG_BUDGET)).toString(),
                                Html.fromHtml(projectObj.getString(TAG_DESC)).toString(),
                                projectObj.getInt(TAG_DURATION),
                                ""+projectObj.getString(TAG_LOCALG_NAME)+", "+projectObj.getString(TAG_STATE_NAME),
                                projectObj.getInt(TAG_BID), projectObj.getString(TAG_DATE_CREATED),
                                projectObj.getInt(TAG_STATUS)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                if(firstload) {
                    adapter = new PRVAdapter(projects);
                    rv.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                    Log.d("adding items--OFFSET: ",""+offset);
                }
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(BrowseActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(BrowseActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
