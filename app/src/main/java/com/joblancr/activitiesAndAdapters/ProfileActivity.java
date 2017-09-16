package com.joblancr.activitiesAndAdapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joblancr.helpers.FilePath;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;
import com.joblancr.helpers.ProfileTabsPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private String localg_name, state_name;

    private ImageView profileImage;
    private TextView profileName, userLocation;
    private RatingBar ratingBar;
    private Button hireBtn;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    SessionManager sessionManager;

    private Bitmap bitmap;

    private Uri selectedImageUri;
    private String selectedFilePath;

    private int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private Bundle extras;
    private String user_profile_id = null;
    private String project_id = null;
    private String project_owner_id = null;
    private String project_bid_id = null;
    private AsyncTask getProfile, reportUser = null, hireUser = null, initNegotiation = null;

    private String project_title, project_user_id;
    private int project_status;

    public static Activity profileActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_profile);

        profileActivity = this;

        //initialize session manager
        sessionManager = new SessionManager(ProfileActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initialize views
        profileImage = (ImageView) findViewById(R.id.user_profile_photo);
        profileName = (TextView) findViewById(R.id.user_profile_name);
        userLocation = (TextView) findViewById(R.id.user_location);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        hireBtn = (Button) findViewById(R.id.hireBtn);

        extras = getIntent().getExtras();
        if(extras != null) {
            user_profile_id = extras.getString("bidder_id");
            project_id = extras.getString("project_id");
            project_owner_id = extras.getString("owner_id");
            project_bid_id = extras.getString("bid_id");

            project_title = extras.getString("project_title");
            project_user_id = extras.getString("project_user_id");
            project_status = Integer.parseInt(extras.getString("project_status"));
        }

        //check user is checking own profile
        if(user_profile_id == null) {
            //hide hire Button
            hideHireButton();

            //temporarily set the profile Name
            profileName.setText(userDetails.get("name"));
            getProfile = new getProfile().execute("getProfile", userDetails.get("id"));
        } else {
            getProfile = new getProfile().execute("getProfile", user_profile_id);

            if(!userDetails.get("id").equals(project_owner_id)) {
                //hide hire Button
                hideHireButton();
            }
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new ProfileTabsPagerAdapter(getSupportFragmentManager(), ProfileActivity.this));

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //Adding onTabSelectedListener to swipe views
        tabLayout.addOnTabSelectedListener(this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        hireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hireUser = new hireUser().execute("hireBidder", user_profile_id, project_bid_id, project_id);
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

    private void hideHireButton() {
        //hide hire Button
        hireBtn.setVisibility(View.GONE);

        //reset margin of the LinearLayout Above hire Button
        LinearLayout above = (LinearLayout) findViewById(R.id.hire_button_top);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) above.getLayoutParams();
        params.bottomMargin = 0;
        above.setLayoutParams(params);
        above.requestLayout();
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            selectedImageUri = data.getData();
            selectedFilePath = FilePath.getPath(this, selectedImageUri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //on upload button Click
            if(selectedFilePath != null){
                //show progress dialog

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //creating new thread to handle Http Operations
                        uploadFile(selectedFilePath);
                    }
                }).start();
            }else{
                Toast.makeText(ProfileActivity.this,"Please choose a File First",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //check user is checking own profile
        if(user_profile_id == null || user_profile_id.equals(userDetails.get("id"))) {
            getMenuInflater().inflate(R.menu.menu_profile_1, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_profile_2, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //check user is checking own profile
            if(user_profile_id == null || user_profile_id.equals(userDetails.get("id"))) {
                //user checking own profile
                Intent dash = new Intent(ProfileActivity.this, DashboardActivity.class);
                startActivity(dash);
                finish();
            } else {
                //user not checking own profile
                //NavUtils.navigateUpFromSameTask(this);
                Intent projectView = new Intent(ProfileActivity.this, ProjectViewActivity.class);
                projectView.putExtra("project_title", project_title);
                projectView.putExtra("project_user_id", project_user_id);
                projectView.putExtra("project_status", Integer.toString(project_status));
                projectView.putExtra("project_id", project_id);
                startActivity(projectView);
                finish();
            }

            return true;
        }

        if (id == R.id.action_edit_profile) {
            Intent edit = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(edit);
        }

        if (id == R.id.action_report_user) {

            final EditText reason = new EditText(ProfileActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            reason.setLayoutParams(lp);

            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle(getResources().getString(R.string.title_report_user))
                    .setMessage(getResources().getString(R.string.report_user_msg))
                    .setView(reason)
                    .setPositiveButton(getResources().getString(R.string.report), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //continue with report
                            reportUser = new reportUser().execute("reportUser", user_profile_id, userDetails.get("id"),
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getProfile.cancel(true);

        if(hireUser != null) {
            hireUser.cancel(true);
        }

        if(reportUser != null) {
            reportUser.cancel(true);
        }

        if(initNegotiation != null) {
            initNegotiation.cancel(true);
        }

    }

    public int uploadFile(final String selectedFilePath){

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String SERVER_UPLOAD_URL;

        if(user_profile_id == null) {
            SERVER_UPLOAD_URL = "http://192.168.43.14/Joblancr/Php/Webservice/UploadHandler.php?user_id=" + userDetails.get("id");
        } else {
            SERVER_UPLOAD_URL = "http://192.168.43.14/Joblancr/Php/Webservice/UploadHandler.php?user_id=" + user_profile_id;
        }


        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){
            //dismiss progress bar

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ProfileActivity.this, "Source File Doesn't Exist: " + selectedFilePath, Toast.LENGTH_SHORT).show();
                }
            });
            return 0;
        } else {
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(SERVER_UPLOAD_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("image_file",selectedFilePath);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"image_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                final String serverResponseMessage = connection.getResponseMessage();

                Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfileActivity.this, getString(R.string.msg_profilepic_upload),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if(serverResponseCode == 500){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfileActivity.this, serverResponseMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfileActivity.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(ProfileActivity.this, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ProfileActivity.this, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }

            //dismiss progress bar

            return serverResponseCode;
        }

    }

    class getProfile extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar profProgBar;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_PROFILE_EXISTS = "profile_exists";
        private static final String TAG_NAME = "full_name";
        private static final String TAG_STATE_NAME = "state_name";
        private static final String TAG_LOCALG_NAME = "localg_name";
        private static final String TAG_RATING = "rating";
        private static final String TAG_IMAGE = "image";

        @Override
        protected void onPreExecute() {
            profProgBar = (ProgressBar) findViewById(R.id.profProgBar);
            profProgBar.setVisibility(View.VISIBLE);
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

        @Override
        protected void onCancelled() {
            Log.d("Message", "ProfileActivity: getProfile AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            int profileExists = 1;
            String errormsg = "";
            String image = null;
            String full_name = null;
            int rating = 0;

            profProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                    profileExists = json.getInt(TAG_PROFILE_EXISTS);

                    if(profileExists == 1) {
                        image = json.getString(TAG_IMAGE);
                        rating = json.getInt(TAG_RATING);
                        full_name = json.getString(TAG_NAME);
                        state_name = json.getString(TAG_STATE_NAME);
                        localg_name = json.getString(TAG_LOCALG_NAME);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {

                if(profileExists == 1) {
                    userLocation.setText(""+localg_name+", "+state_name);
                    profileName.setText(full_name);
                    ratingBar.setRating(rating);
                    if(image != "0") {
                        if(user_profile_id == null) {
                            new DownloadImageTask(profileImage)
                                    .execute("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+userDetails.get("id")+"/"+image);
                        } else {
                            new DownloadImageTask(profileImage)
                                    .execute("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+user_profile_id+"/"+image);
                        }
                    }
                }

            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.errmsg_prof_ret_failure),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            bmImage.setImageBitmap(result);
        }
    }

    class reportUser extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Deleting Project...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("reported_id", args[1]);
                params.put("reporter_id", args[2]);
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
        protected void onCancelled() {
            Log.d("Message", "ProfileActivity: reportUser AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
                pDialog = null;
            }

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
                Toast.makeText(ProfileActivity.this,
                        getResources().getString(R.string.user_reported_msg), Toast.LENGTH_SHORT).show();
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProfileActivity.this, "User Couldnt Be Reported. Try Again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class hireUser extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Project.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Hiring User...");
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
                params.put("bid_id", args[2]);
                params.put("project_id", args[3]);

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
            Log.d("Message", "ProfileActivity: hireUser AsyncTask Cancelled!");
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
                initNegotiation = new initNegotiation().execute("startNegotiation", project_id);
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProfileActivity.this, "User Couldn't Be Hired. Try Again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class initNegotiation extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Negotiation.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Initiating Negotiations...");
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
            Log.d("Message", "ProfileActivity: initNegotiation AsyncTask Cancelled!");
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
                Log.d("Success!", "Negotiation Successfully Initiated");

                //Redirecting User to DashboardActivity
                Intent i = new Intent(ProfileActivity.this, NegotiationActivity.class);
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
                    Toast.makeText(ProfileActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
