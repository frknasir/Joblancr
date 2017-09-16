package com.joblancr.activitiesAndAdapters;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/*import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;*/

import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    //CallbackManager callbackManager;

    private EditText email, password;
    private Button loginBtn;
    private TextView notReg;

    private String type,email_v, password_v;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //check login status
        sessionManager = new SessionManager(LoginActivity.this);
        if(sessionManager.isLoggedIn()) {
            Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
            startActivity(i);
            finish();
            MainActivity.mainActivity.finish();
        }

        //Facebook
        /*FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile", "email","user_friends");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                com.facebook.ProfileActivity profile;
                //profile.getName();
                //profile.getProfilePictureUri(20,20);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });*/

        notReg = (TextView) findViewById(R.id.notRegText);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        email = (EditText) findViewById(R.id.emailEditText);
        password = (EditText) findViewById(R.id.passwordEditText);

        notReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "login";
                email_v = email.getText().toString();
                password_v = password.getText().toString();

                //check if email and password and not empty
                if(!credsEmpty(email_v, password_v)) {
                    new loginUser().execute(type, email_v, password_v);
                }
            }
        });
    }

    //check user input
    private boolean credsEmpty(String email, String password) {
        boolean empty = false;

        //check if email is empty
        if(email == null || email.isEmpty()) {
            Toast.makeText(LoginActivity.this, getString(R.string.errmsg_email_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        //check if password is empty
        if(password == null || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, getString(R.string.errmsg_pass_empty), Toast.LENGTH_SHORT).show();
            empty = true;
        }

        return empty;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }*/

    class loginUser extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar pbSpinner;

        private static final String AUTH_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Authentication.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_ID = "id";
        private static final String TAG_NAME = "name";
        private static final String TAG_EMAIL = "email";

        @Override
        protected void onPreExecute() {
            pbSpinner = (ProgressBar) findViewById(R.id.loginProgBar);
            pbSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("email", args[1]);
                params.put("password", args[2]);

                Log.d("Request", "Starting");

                try {
                    json = jsonParser.makeHttpRequest(
                            AUTH_URL, "POST", params);
                } catch(Exception e) {
                    e.printStackTrace();
                }

                if (json != null) {
                    Log.d("Response: ", "> " + json.toString());
                } else {
                    Log.e("JSON Data", "Didnt receive any data from server");
                    Toast.makeText(LoginActivity.this, "Check Your Connection and Try Again", Toast.LENGTH_SHORT).show();
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
            String id = "";
            String name = "";
            String email = "";

            //hide progress bar
            pbSpinner.setVisibility(View.GONE);

            if (json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                    id = json.getString(TAG_ID);
                    name = json.getString(TAG_NAME);
                    email = json.getString(TAG_EMAIL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                SessionManager sessionManager = new SessionManager(LoginActivity.this);
                sessionManager.createLoginSession(id, name, email);

                if(sessionManager.isLoggedIn()) {
                    Log.d("Success!", "User Successfully Logged In!");

                    //Redirecting User to DashboardActivity
                    Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                    // Closing all the Activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    // Add new Flag to start new Activity
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Staring LoginActivity Activity
                    startActivity(i);

                    //kill off activity
                    finish();
                    MainActivity.mainActivity.finish();
                }
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(LoginActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(LoginActivity.this, getString(R.string.errmsg_check_conn), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
