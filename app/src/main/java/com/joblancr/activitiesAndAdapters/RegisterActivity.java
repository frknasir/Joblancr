package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText fullName, email, password, cpassword;
    private Spinner states_spinner, localg_spinner;
    private Button registerBtn;
    private TextView Reg;

    private String type, full_name, email_v, password_v, cpassword_v;
    private int localg_id;

    private ArrayList<States> statesList;
    private ArrayList<LocalG> localsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //setup input fields
        fullName = (EditText) findViewById(R.id.nameEditText);
        email = (EditText) findViewById(R.id.emailEditText);
        password = (EditText) findViewById(R.id.passwordEditText);
        cpassword = (EditText) findViewById(R.id.cPasswordEditText);

        //setup spinners
        states_spinner = (Spinner) findViewById(R.id.states_spinner);
        localg_spinner = (Spinner) findViewById(R.id.localg_spinner);

        //setup Button
        registerBtn = (Button) findViewById(R.id.registerBtn);

        statesList = new ArrayList<States>();
        localsList = new ArrayList<LocalG>();

        Reg = (TextView) findViewById(R.id.RegText);

        Reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(registerIntent);
            }
        });

        new getStates().execute("states");

        states_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //int selected_id = (int) states_spinner.getItemIdAtPosition(position);
                States state = (States) states_spinner.getItemAtPosition(position);

                String name = state.getName();
                int s_id = state.getId();

                //Toast.makeText(RegisterActivity.this, s_id + " " +name, Toast.LENGTH_SHORT).show();

                new getLocalG().execute("local", "" + s_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        localg_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //int selected_id = (int) localg_spinner.getItemIdAtPosition(position);
                LocalG localg = (LocalG) localg_spinner.getItemAtPosition(position);

                String name = localg.getName();
                localg_id = localg.getId();

                //Toast.makeText(RegisterActivity.this, name +" "+ s_id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "register";
                full_name = fullName.getText().toString();
                email_v = email.getText().toString();
                password_v = password.getText().toString();
                cpassword_v = cpassword.getText().toString();

                //check if creds are empty or not
                if(!credsEmpty(full_name,email_v,password_v,cpassword_v,localg_id)) {
                    new registerUser().execute(type, full_name, email_v, password_v, cpassword_v, "" + localg_id);
                }
            }
        });
    }

    //check user input
    private boolean credsEmpty(String fullName, String email, String password,
                               String cPassword, int localgId) {
        boolean empty = false;

        //check if full name is empty
        if(fullName == null || fullName.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_name_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if email is empty
        if(email == null || email.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_email_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if password is empty
        if(password == null || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_pass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if confirm password is empty
        if(cPassword == null || cPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_cpass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if password matches confirmation
        if(!password.equals(cPassword)) {
            Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_pass_not_equal), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        return empty;
    }

    class getStates extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar statesLoader;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_STATES = "states";

        @Override
        protected void onPreExecute() {
            statesLoader = (ProgressBar) findViewById(R.id.statesProgBar);
            statesLoader.setVisibility(View.VISIBLE);
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
                Toast.makeText(RegisterActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            statesLoader.setVisibility(View.GONE);

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
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(RegisterActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(RegisterActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
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

        private ProgressBar localgLoader;

        private static final String SPINNER_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Spinner.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_LOCALS = "localg";

        @Override
        protected void onPreExecute() {
            localgLoader = (ProgressBar) findViewById(R.id.localgProgBar);
            localgLoader.setVisibility(View.VISIBLE);
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
                Toast.makeText(RegisterActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            localgLoader.setVisibility(View.GONE);

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
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(RegisterActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(RegisterActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
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

    class registerUser extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar regProgBar;

        private static final String AUTH_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Authentication.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_ID = "id";
        private static final String TAG_NAME = "name";
        private static final String TAG_EMAIL = "email";

        @Override
        protected void onPreExecute() {
            regProgBar = (ProgressBar) findViewById(R.id.regProgBar);
            regProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("name", args[1]);
                params.put("email", args[2]);
                params.put("password", args[3]);
                params.put("cpassword", args[4]);
                params.put("localg_id", args[5]);

                Log.d("Request", "Starting");

                try {
                    json = jsonParser.makeHttpRequest(
                            AUTH_URL, "POST", params);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(json != null) {
                    Log.d("Response: ", "> " + json.toString());
                }

                else {
                    Log.e("JSON Data", "Didnt receive any data from server");
                    Toast.makeText(RegisterActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
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

            regProgBar.setVisibility(View.GONE);

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

                Log.d("Success!", "User Successfully Registered!");
                Log.d("Redirecting", "Login Page");

                //Redirecting User to LoginActivity
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);

                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // Add new Flag to start new Activity
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Starting LoginActivity Activity
                startActivity(i);

                //kill off activity
                finish();
            }else{
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(RegisterActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.errmsg_check_conn), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
