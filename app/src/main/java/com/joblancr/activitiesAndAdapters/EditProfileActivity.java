package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.joblancr.cards.LocalG;
import com.joblancr.cards.States;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText name, summary, skills;
    private Spinner states_spinner, localg_spinner;
    private Button updateBtn;

    private int state_id, localg_id, i_state_id, i_localg_id, state_position = -1,
            localg_position = -1;
    private String summary_v, skills_v, i_about, i_skills, i_state_name, i_localg_name;

    private ArrayList<States> statesList;
    private ArrayList<LocalG> localsList;

    private HashMap<String, String> userDetails = new HashMap<String, String>();
    private AsyncTask getProfile, getStates, update = null, getLocal = null;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        //initialize session manager
        sessionManager = new SessionManager(EditProfileActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        name = (EditText) findViewById(R.id.nameEditText);
        summary = (EditText) findViewById(R.id.summaryEditText);
        skills = (EditText) findViewById(R.id.skillsEditText);
        states_spinner = (Spinner) findViewById(R.id.states_spinner);
        localg_spinner = (Spinner) findViewById(R.id.localg_spinner);
        updateBtn = (Button) findViewById(R.id.saveChangesBtn);

        statesList = new ArrayList<States>();
        localsList = new ArrayList<LocalG>();

        name.setTag(name.getKeyListener());
        name.setKeyListener(null);

        name.setText(userDetails.get("name"));

        getStates = new getStates().execute("states");

        states_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                States state = (States) states_spinner.getItemAtPosition(position);
                state_id = state.getId();

                getLocal = new getLocalG().execute("local", "" + state_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                summary_v = summary.getText().toString();
                skills_v = skills.getText().toString();

                update = new updateUserProfile().execute("update",userDetails.get("id").toString(),
                        ""+localg_id,summary_v,skills_v);
            }
        });

        getProfile = new getProfile().execute("getProfile", userDetails.get("id"));
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
            Intent profile = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(profile);
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

        getStates.cancel(true);
        getProfile.cancel(true);

        if(update != null) {
            update.cancel(true);
        }

        if(getLocal != null) {
            getLocal.cancel(true);
        }
    }

    class getProfile extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_STATE_ID = "state_id";
        private static final String TAG_LOCALG_ID = "localg_id";
        private static final String TAG_ABOUT = "about";
        private static final String TAG_SKILLS = "skills";
        private static final String TAG_STATE_NAME = "state_name";
        private static final String TAG_LOCALG_NAME = "localg_name";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(EditProfileActivity.this);
            pDialog.setMessage("Getting ProfileActivity...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
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
                    Toast.makeText(EditProfileActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onCancelled() {
            Log.d("Message", "EditProfileActivity: getProfile AsyncTask Cancelled!");
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
                    i_state_id = json.getInt(TAG_STATE_ID);
                    i_state_name = json.getString(TAG_STATE_NAME);
                    i_localg_id = json.getInt(TAG_LOCALG_ID);
                    i_localg_name = json.getString(TAG_LOCALG_NAME);
                    i_about = json.getString(TAG_ABOUT);
                    i_skills = json.getString(TAG_SKILLS);

                    States st = new States(i_state_id,i_state_name);
                    for (int i=0;i<states_spinner.getCount();i++){
                        States tmp = (States) states_spinner.getItemAtPosition(i);
                        if (tmp.getId() == st.getId()){
                            state_position = i;
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                summary.setText(i_about);
                skills.setText(i_skills);
                states_spinner.setSelection(state_position);
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(EditProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(EditProfileActivity.this, "Failure to retrieve profile", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class getStates extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar statesProgBar;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_STATES = "states";

        @Override
        protected void onPreExecute() {
            statesProgBar = (ProgressBar) findViewById(R.id.statesProgBar);
            statesProgBar.setVisibility(View.VISIBLE);
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
                    JSONArray states = json
                            .getJSONArray(TAG_STATES);

                    for (int i = 0; i < states.length(); i++) {
                        JSONObject stateObj = (JSONObject) states.get(i);
                        States state = new States(stateObj.getInt("id"),
                                stateObj.getString("name"));
                        statesList.add(state);
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
        protected void onCancelled() {
            Log.d("Message", "EditProfileActivity: getStates AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            statesProgBar.setVisibility(View.GONE);

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
                populateStatesSpinner();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(EditProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(EditProfileActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void populateStatesSpinner() {

        // Creating adapter for spinner
        ArrayAdapter<States> spinnerAdapter = new ArrayAdapter<States>(this,
                android.R.layout.simple_spinner_item, statesList);

        // Drop down layout style - list view with radio button
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        states_spinner.setAdapter(spinnerAdapter);
    }

    class getLocalG extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar localgProgBar;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_LOCALS = "localg";

        @Override
        protected void onPreExecute() {
            localgProgBar = (ProgressBar) findViewById(R.id.localgProgBar);
            localgProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            HashMap<String, String> params = new HashMap<>();
            params.put("type", args[0]);
            params.put("state_id", args[1]);

            try {
                json = jsonParser.makeHttpRequest(
                        SPINNER_URL, "POST", params);
            } catch(Exception e) {
                e.printStackTrace();
            }

            if(json != null) {
                Log.d("Response: ", "> " + json.toString());

                try {
                    JSONArray locals = json
                            .getJSONArray(TAG_LOCALS);

                    localsList.clear();

                    for (int i = 0; i < locals.length(); i++) {
                        JSONObject localObj = (JSONObject) locals.get(i);
                        LocalG local = new LocalG(localObj.getInt("id"),
                                localObj.getString("name"));
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
                populateLocalsSpinner();
                LocalG lc = new LocalG(i_localg_id,i_localg_name);
                for (int i=0;i<localg_spinner.getCount();i++){
                    LocalG tmp = (LocalG) localg_spinner.getItemAtPosition(i);
                    if (tmp.getId() == lc.getId()){
                        localg_position = i;
                        break;
                    }
                }

                localg_spinner.setSelection(localg_position);
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(EditProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(EditProfileActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void populateLocalsSpinner() {

        // Creating adapter for spinner
        ArrayAdapter<LocalG> spinnerAdapter = new ArrayAdapter<LocalG>(this,
                android.R.layout.simple_spinner_item, localsList);

        // Drop down layout style - list view with radio button
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        localg_spinner.setAdapter(spinnerAdapter);
    }

    class updateUserProfile extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar updateProgBar;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            updateProgBar = (ProgressBar) findViewById(R.id.updateProgBar);
            updateProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);
                params.put("localg_id", args[2]);
                params.put("about", args[3]);
                params.put("skills", args[4]);

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
                    Toast.makeText(EditProfileActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
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

            updateProgBar.setVisibility(View.GONE);

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

            if (success == 1) {
                Log.d("Success!", "ProfileActivity Updated");

                //Redirecting User to DashboardActivity
                Intent i = new Intent(EditProfileActivity.this, ProfileActivity.class);
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
                    Toast.makeText(EditProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(EditProfileActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
