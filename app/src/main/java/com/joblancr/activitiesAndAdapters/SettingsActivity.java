package com.joblancr.activitiesAndAdapters;

import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.joblancr.helpers.AlertDialogManager;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private EditText oldPass, newPass, confirmPass;
    private Button changePassBtn;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;
    private AsyncTask changePassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        //initialize session manager
        sessionManager = new SessionManager(SettingsActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initialize views
        oldPass = (EditText) findViewById(R.id.oldPasswordEditText);
        newPass = (EditText) findViewById(R.id.newPasswordEditText);
        confirmPass = (EditText) findViewById(R.id.confirmPasswordEditText);
        changePassBtn = (Button) findViewById(R.id.changePasswordBtn);

        changePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!credsEmpty(oldPass.getText().toString(), newPass.getText().toString(),
                        confirmPass.getText().toString())) {
                    changePassword = new changePassword().execute("changePassword", userDetails.get("id"),
                            oldPass.getText().toString(), newPass.getText().toString(),
                            confirmPass.getText().toString());
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

    //check user input
    private boolean credsEmpty(String oldPass, String newPass, String confPass) {
        boolean empty = false;

        //check if old pass is empty
        if(oldPass == null || oldPass.isEmpty()) {
            Toast.makeText(SettingsActivity.this, getString(R.string.errmsg_oldPass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if new pass is empty
        if(newPass == null || newPass.isEmpty()) {
            Toast.makeText(SettingsActivity.this, getString(R.string.errmsg_newPass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if new pass is empty
        if(confPass == null || confPass.isEmpty()) {
            Toast.makeText(SettingsActivity.this, getString(R.string.errmsg_confPass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if new pass equals confpass
        if(!newPass.equals(confPass)) {
            Toast.makeText(SettingsActivity.this, getString(R.string.errmsg_pass_not_equal), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        return empty;
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

        if(changePassword != null) {
            changePassword.cancel(true);
        }
    }

    class changePassword extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar settingsProgBar;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Settings.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            settingsProgBar = (ProgressBar) findViewById(R.id.settingsProgBar);
            settingsProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);
                params.put("oldPass", args[2]);
                params.put("newPass", args[3]);
                params.put("confirmPass", args[4]);

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
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            settingsProgBar.setVisibility(View.GONE);

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
                Log.d("Success!", "Password Changed");

                oldPass.setText("");
                newPass.setText("");
                confirmPass.setText("");

                AlertDialogManager alertDialogManager = new AlertDialogManager();
                alertDialogManager.showAlertDialog(SettingsActivity.this, "Message", "Password Changed", Boolean.TRUE);

            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(SettingsActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(SettingsActivity.this, getString(R.string.errmsg_check_conn), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
