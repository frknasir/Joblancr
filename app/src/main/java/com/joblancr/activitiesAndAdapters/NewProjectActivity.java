package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.joblancr.cards.Budget;
import com.joblancr.cards.Category;
import com.joblancr.cards.LocalG;
import com.joblancr.cards.States;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NewProjectActivity extends AppCompatActivity {

    private Spinner states_spinner, localg_spinner, category_spinner, budget_spinner;
    private EditText title, desc, skills, days;
    private Button addBtn;
    private ArrayList<States> statesList;
    private ArrayList<LocalG> localsList;
    private ArrayList<Category> categoryList;
    private ArrayList<Budget> budgetList;

    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private AsyncTask getLocation, getCategory, getBudget, addProject = null;

    private int localg_id, cat_id, budget_id;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        //initialize session manager
        sessionManager = new SessionManager(NewProjectActivity.this);
        sessionManager.checkLogin();

        //setup spinners
        states_spinner = (Spinner) findViewById(R.id.states_spinner);
        localg_spinner = (Spinner) findViewById(R.id.localg_spinner);
        category_spinner = (Spinner) findViewById(R.id.category_spinner);
        budget_spinner = (Spinner) findViewById(R.id.budget_spinner);
        addBtn = (Button) findViewById(R.id.addBtn);

        //initialize edittext
        title = (EditText) findViewById(R.id.pTitleEditText);
        desc = (EditText) findViewById(R.id.pDescEditText);
        skills = (EditText) findViewById(R.id.pSkillsEditText);
        days = (EditText) findViewById(R.id.ptimeEditText);

        statesList = new ArrayList<States>();
        localsList = new ArrayList<LocalG>();
        categoryList = new ArrayList<Category>();
        budgetList = new ArrayList<Budget>();

        userDetails = sessionManager.getUserDetails();

        getCategory = new getCategorySpinner().execute("category");

        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category category = (Category) category_spinner.getItemAtPosition(position);
                cat_id = category.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getLocation = new getLocation().execute("location", userDetails.get("id"));

        localg_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocalG localg = (LocalG) localg_spinner.getItemAtPosition(position);
                localg_id = localg.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getBudget = new getBudgetSpinner().execute("budget");

        budget_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Budget budget = (Budget) budget_spinner.getItemAtPosition(position);
                budget_id = budget.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if there are no empty user input
                if(!credsEmpty(title.getText().toString(), desc.getText().toString(),
                        skills.getText().toString(), days.getText().toString())) {
                    addProject = new addProject().execute("add", title.getText().toString(), desc.getText().toString(),
                            skills.getText().toString(), "" + localg_id, "" + budget_id, days.getText().toString(),
                            userDetails.get("id"), "" + cat_id);
                }
            }
        });
    }

    //check user input
    private boolean credsEmpty(String title, String desc, String skills, String duration) {

        boolean empty = false;

        //check if title is empty
        if(title == null || title.isEmpty()) {
            Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_title_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if desc is empty
        if(desc == null || desc.isEmpty()) {
            Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_desc_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if skills is empty
        if(skills == null || skills.isEmpty()) {
            Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_skills_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if duration is empty
        if(duration == null || duration.isEmpty()) {
            Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_duration_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        return empty;

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

        getBudget.cancel(true);
        getLocation.cancel(true);
        getCategory.cancel(true);

        if(addProject != null) {
            addProject.cancel(true);
        }
    }

    class getLocation extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar statesProgBar, localgProgBar;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_LOCATION = "location";

        @Override
        protected void onPreExecute() {
            statesProgBar = (ProgressBar) findViewById(R.id.statesProgBar);
            localgProgBar = (ProgressBar) findViewById(R.id.localgProgBar);

            statesProgBar.setVisibility(View.VISIBLE);
            localgProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HashMap<String, String> params = new HashMap<>();
            params.put("type", args[0]);
            params.put("user_id", args[1]);

            try {
                json = jsonParser.makeHttpRequest(
                        SPINNER_URL, "POST", params);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(json != null) {
                Log.d("Response: ", "> " + json.toString());

                try {
                    JSONArray location = json
                            .getJSONArray(TAG_LOCATION);

                    for (int i = 0; i < location.length(); i++) {
                        JSONObject locObj = (JSONObject) location.get(i);

                        States state = new States(locObj.getInt("state_id"),
                                locObj.getString("state_name"));
                        statesList.add(state);

                        LocalG local = new LocalG(locObj.getInt("localg_id"),
                                locObj.getString("localg_name"));
                        localsList.add(local);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "Didnt receive any data from server");
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            statesProgBar.setVisibility(View.GONE);
            localgProgBar.setVisibility(View.GONE);

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
                populateLocationSpinner();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(NewProjectActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void populateLocationSpinner() {

        // Creating adapter for State spinner
        ArrayAdapter<States> stateAdapter = new ArrayAdapter<States>(this,
                android.R.layout.simple_spinner_item, statesList);

        // Drop down layout style for State spinner - list view with radio button
        stateAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to State spinner
        states_spinner.setAdapter(stateAdapter);

        // Creating adapter for Local spinner
        ArrayAdapter<LocalG> localAdapter = new ArrayAdapter<LocalG>(this,
                android.R.layout.simple_spinner_item, localsList);

        // Drop down layout style for Local spinner - List view with radio button
        localAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to Local spinner
        localg_spinner.setAdapter(localAdapter);
    }

    class getCategorySpinner extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar catProgBar;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_CATEGORY = "categories";

        @Override
        protected void onPreExecute() {
            catProgBar = (ProgressBar) findViewById(R.id.catProgBar);
            catProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HashMap<String, String> params = new HashMap<>();
            params.put("type", args[0]);

            try {
                json = jsonParser.makeHttpRequest(
                        SPINNER_URL, "POST", params);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(json != null) {
                Log.d("Response: ", "> " + json.toString());

                try {
                    JSONArray cats = json
                            .getJSONArray(TAG_CATEGORY);

                    for (int i = 0; i < cats.length(); i++) {
                        JSONObject catObj = (JSONObject) cats.get(i);
                        Category category = new Category(catObj.getInt("id"),
                                catObj.getString("name"));
                        categoryList.add(category);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "Didnt receive any data from server");
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            catProgBar.setVisibility(View.GONE);

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
                populateCategorySpinner();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(NewProjectActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void populateCategorySpinner() {

        // Creating adapter for spinner
        ArrayAdapter<Category> spinnerAdapter = new ArrayAdapter<Category>(this,
                android.R.layout.simple_spinner_item, categoryList);

        // Drop down layout style - list view with radio button
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        category_spinner.setAdapter(spinnerAdapter);
    }

    class getBudgetSpinner extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar budgetProgBar;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_BUDGET = "budget";

        @Override
        protected void onPreExecute() {
            budgetProgBar = (ProgressBar) findViewById(R.id.budgetProgBar);
            budgetProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HashMap<String, String> params = new HashMap<>();
            params.put("type", args[0]);

            try {
                json = jsonParser.makeHttpRequest(
                        SPINNER_URL, "POST", params);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(json != null) {
                Log.d("Response: ", "> " + json.toString());

                try {
                    JSONArray budgets = json
                            .getJSONArray(TAG_BUDGET);

                    for (int i = 0; i < budgets.length(); i++) {
                        JSONObject budObj = (JSONObject) budgets.get(i);
                        Budget bdgt = new Budget(budObj.getInt("id"),
                                Html.fromHtml(budObj.getString("name")).toString());
                        budgetList.add(bdgt);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Log.e("JSON Data", "Didnt receive any data from server");
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            budgetProgBar.setVisibility(View.GONE);

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
                populateBudgetSpinner();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(NewProjectActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void populateBudgetSpinner() {

        // Creating adapter for spinner
        ArrayAdapter<Budget> budgetAdapter = new ArrayAdapter<Budget>(this,
                android.R.layout.simple_spinner_item, budgetList);

        // Drop down layout style - list view with radio button
        budgetAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        budget_spinner.setAdapter(budgetAdapter);
    }

    class addProject extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar addProProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            addProProgBar = (ProgressBar) findViewById(R.id.addProProgBar);
            addProProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("title", args[1]);
                params.put("desc", args[2]);
                params.put("skills", args[3]);
                params.put("localg_id", args[4]);
                params.put("budget_id", args[5]);
                params.put("days", args[6]);
                params.put("user_id", args[7]);
                params.put("cat_id", args[8]);

                Log.d("Request", "Starting");

                try {
                    json = jsonParser.makeHttpRequest(
                            PROJECT_URL, "POST", params);
                } catch(Exception e) {
                    e.printStackTrace();
                }

                if (json != null) {
                    Log.d("Response: ", "> " + json.toString());
                } else {
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

            addProProgBar.setVisibility(View.GONE);

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                Log.d("Success!", "Project Successfully Added!");

                //Redirecting User to DashboardActivity
                Intent i = new Intent(NewProjectActivity.this, DashboardActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Staring LoginActivity Activity
                startActivity(i);

                //kill off activity
                finish();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(NewProjectActivity.this, errormsg, Toast.LENGTH_LONG).show();
                }

                else {
                    Toast.makeText(NewProjectActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
