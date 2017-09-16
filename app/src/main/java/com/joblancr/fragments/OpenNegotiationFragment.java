package com.joblancr.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.joblancr.activitiesAndAdapters.NegotiationAdapter;
import com.joblancr.activitiesAndAdapters.R;
import com.joblancr.cards.NegotiationCard;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OpenNegotiationFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private List<NegotiationCard> open_negotiations;
    private RecyclerView open_neg_rv;

    private NegotiationAdapter adapter;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;
    private AsyncTask getNegotiations;
    private final int NEG_STATUS = 1;

    public static OpenNegotiationFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        OpenNegotiationFragment fragment = new OpenNegotiationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

        //initialize session manager
        sessionManager = new SessionManager(getContext());
        sessionManager.checkLogin();

        userDetails = sessionManager.getUserDetails();

        //initializing
        open_negotiations = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_open_negotiation, container, false);

        open_neg_rv = (RecyclerView) view.findViewById(R.id.open_negotiation_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        open_neg_rv.setLayoutManager(llm);
        open_neg_rv.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getNegotiations = new getNegotiations().execute("getNegotiations", userDetails.get("id"), "" + NEG_STATUS);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getNegotiations.cancel(true);
    }

    class getNegotiations extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar negProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Negotiation.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_NEGOTIATIONS = "negotiations";
        private static final String TAG_IMAGE = "image";
        private static final String TAG_TITLE = "p_title";
        private static final String TAG_LAST_EXCHANGE = "exchange_msg";
        private static final String TAG_DATE_SENT = "exchange_time";
        private static final String TAG_SELECTED_BIDDER = "selected_bidder_id";
        private static final String TAG_PROJECT_OWNER = "p_user_id";
        private static final String TAG_NEGOTIATION_ID = "negotiation_id";
        private static final String TAG_STATUS = "status";
        private static final String TAG_EXCHANGE_STATUS = "exchange_status";
        private static final String TAG_NEGOTIATIONS_EXIST = "negotiations_exist";


        @Override
        protected void onPreExecute() {
            negProgBar = (ProgressBar) getView().findViewById(R.id.negProgBar);
            negProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);
                params.put("neg_status", args[2]);

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
            int exist = 0;

            negProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    } else {
                        exist = json.getInt(TAG_NEGOTIATIONS_EXIST);
                    }

                    JSONArray negotiationz = json.getJSONArray(TAG_NEGOTIATIONS);

                    for(int i = 0; i < negotiationz.length(); i++) {
                        JSONObject negotiationObj = (JSONObject) negotiationz.get(i);

                        open_negotiations.add(new NegotiationCard(negotiationObj.getString(TAG_IMAGE),negotiationObj.getString(TAG_TITLE),
                                negotiationObj.getString(TAG_LAST_EXCHANGE),negotiationObj.getString(TAG_DATE_SENT),
                                negotiationObj.getString(TAG_SELECTED_BIDDER),negotiationObj.getString(TAG_NEGOTIATION_ID),
                                negotiationObj.getInt(TAG_EXCHANGE_STATUS), negotiationObj.getString(TAG_PROJECT_OWNER)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                if(exist == 1) {
                    adapter = new NegotiationAdapter(open_negotiations);

                    open_neg_rv.setAdapter(adapter);
                }
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(getActivity(), errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getActivity(), getString(R.string.errmsg_check_conn),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
