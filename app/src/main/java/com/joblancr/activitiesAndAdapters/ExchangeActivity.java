package com.joblancr.activitiesAndAdapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.joblancr.cards.ExchangeCard;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExchangeActivity extends AppCompatActivity {
    private String negotiation_title, negotiation_id, selected_bidder, project_owner;

    private List<ExchangeCard> exchangeCardList;
    private RecyclerView recyclerView;

    private EditText exchangeEditText;
    private ImageView sendBtn;

    public static HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;

    private ExchangeAdapter exchangeAdapter;
    private AsyncTask getExchanges, sendExchange = null, closeNegotiations = null;

    public static Activity exchangeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        //initialize
        exchangeActivity = this;

        //initialize session manager
        sessionManager = new SessionManager(ExchangeActivity.this);
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        exchangeCardList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.exchange_rv);
        exchangeEditText = (EditText) findViewById(R.id.exchange_editText);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            negotiation_title = extras.getString("negotiation_title");
            negotiation_id = extras.getString("negotiation_id");
            selected_bidder = extras.getString("selected_bidder");
            project_owner = extras.getString("project_owner");
        }

        this.setTitle(negotiation_title);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        getExchanges = new getExchanges().execute("getExchanges", negotiation_id, userDetails.get("id"));

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userDetails.get("id").equals(selected_bidder)) {
                    sendExchange = new sendExchange().execute("sendExchange", negotiation_id, userDetails.get("id"),
                            project_owner, exchangeEditText.getText().toString());
                } else {
                    sendExchange = new sendExchange().execute("sendExchange", negotiation_id, userDetails.get("id"),
                            selected_bidder, exchangeEditText.getText().toString());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exchange, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            Intent intent = new Intent(ExchangeActivity.this, NegotiationActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_close_negotiation) {
            new AlertDialog.Builder(ExchangeActivity.this)
                    .setTitle(getResources().getString(R.string.title_close_negotiation))
                    .setMessage(getResources().getString(R.string.close_negotiation_msg))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //continue with closing negotiation
                            closeNegotiations = new closeNegotiation().execute("closeNegotiation",negotiation_id);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_delete)
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

        getExchanges.cancel(true);

        if(sendExchange != null) {
            sendExchange.cancel(true);
        }

        if(closeNegotiations != null) {
            closeNegotiations.cancel(true);
        }
    }

    class getExchanges extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Negotiation.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_EXCHANGES = "exchanges";
        private static final String TAG_NAME = "from_name";
        private static final String TAG_IMAGE = "from_image";
        private static final String TAG_EXCHANGE = "exchange_msg";
        private static final String TAG_TIME = "exchange_time";
        private static final String TAG_USER_ID = "from_user";


        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ExchangeActivity.this);
            pDialog.setMessage("Loading Exchanges...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("negotiation_id", args[1]);
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
        protected void onCancelled() {
            Log.d("Message", "ExchangeActivity: getExchanges AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";
            int exist = 0;

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

                    JSONArray exchangz = json.getJSONArray(TAG_EXCHANGES);

                    for(int i = 0; i < exchangz.length(); i++) {
                        JSONObject exchangeObj = (JSONObject) exchangz.get(i);

                        exchangeCardList.add(new ExchangeCard(exchangeObj.getString(TAG_NAME),
                                exchangeObj.getString(TAG_IMAGE), exchangeObj.getString(TAG_EXCHANGE),
                                exchangeObj.getString(TAG_TIME), exchangeObj.getString(TAG_USER_ID)));


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                exchangeAdapter = new ExchangeAdapter(exchangeCardList);
                recyclerView.setAdapter(exchangeAdapter);

                recyclerView.scrollToPosition(exchangeCardList.size()-1);
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ExchangeActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ExchangeActivity.this, "Failure to get Exchanges. Try Again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class sendExchange extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        //private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Negotiation.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            /*pDialog = new ProgressDialog(ExchangeActivity.this);
            pDialog.setMessage("Deleting Project...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();*/

            //disable the send button so you dont send the same
            //message twice
            sendBtn.setEnabled(false);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("negotiation_id", args[1]);
                params.put("from_user", args[2]);
                params.put("to_user", args[3]);
                params.put("exchange", args[4]);

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
                Intent refresh = new Intent(ExchangeActivity.this, ExchangeActivity.class);
                refresh.putExtra("negotiation_title", negotiation_title);
                refresh.putExtra("negotiation_id", negotiation_id);
                refresh.putExtra("selected_bidder", selected_bidder);
                refresh.putExtra("project_owner", project_owner);

                refresh.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(refresh);
                finish();
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ExchangeActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ExchangeActivity.this, getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }

                //reenabling the button
                sendBtn.setEnabled(true);
            }
        }
    }

    class closeNegotiation extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressDialog pDialog;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Negotiation.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(ExchangeActivity.this);
            pDialog.setMessage("Closing Negotiation...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("negotiation_id", args[1]);

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
                Intent refresh = new Intent(ExchangeActivity.this, NegotiationActivity.class);
                startActivity(refresh);
                finish();
            } else {
                if(errormsg != "") {
                    Log.e("Failure", errormsg);
                    Toast.makeText(ExchangeActivity.this, errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(ExchangeActivity.this, "Negotiation Couldnt Be Closed. Try Again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
