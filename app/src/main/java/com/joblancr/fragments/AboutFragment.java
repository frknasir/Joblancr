package com.joblancr.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joblancr.helpers.JSONParser;
import com.joblancr.activitiesAndAdapters.R;
import com.joblancr.helpers.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class AboutFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private String summary_v, skills_v;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    SessionManager sessionManager;

    private Bundle extras;
    private String user_profile_id = null;
    private String project_profile_id = null;

    private TextView skillsTextView;
    private TextView summaryTextView;
    private ProgressBar aboutProgBar;
    private AsyncTask getProfile;

    public static AboutFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        AboutFragment fragment = new AboutFragment();
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

        extras = getActivity().getIntent().getExtras();

        if(extras != null) {
            user_profile_id = extras.getString("bidder_id");
            project_profile_id = extras.getString("project_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        skillsTextView = (TextView) view.findViewById(R.id.skills_text);
        summaryTextView = (TextView) view.findViewById(R.id.summary_text);
        aboutProgBar = (ProgressBar) view.findViewById(R.id.aboutProgBar);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(user_profile_id == null) {
            getProfile = new getProfile().execute("getProfile", userDetails.get("id"));
        } else {
            getProfile = new getProfile().execute("getProfile", user_profile_id);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getProfile.cancel(true);
    }

    class getProfile extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private static final String PROFILE_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Profile.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_ABOUT = "about";
        private static final String TAG_SKILLS = "skills";

        @Override
        protected void onPreExecute() {
            //show progress bar
            aboutProgBar.setVisibility(View.VISIBLE);
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
            Log.d("Message", "AboutFragment: getProfile AsyncTask Cancelled!");
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            //hide progressbar
            aboutProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }
                    summary_v = json.getString(TAG_ABOUT);
                    skills_v = json.getString(TAG_SKILLS);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                String nSkills = "";

                if(summary_v == null) {
                    summary_v = getString(R.string.errmsg_no_profile);
                }

                if(skills_v != null) {
                    String[] Sklls = skills_v.split(",");
                    for(String s : Sklls) {
                        s = s.trim();
                        nSkills += s;
                        nSkills +="\n\n";
                    }
                } else {
                    nSkills = getString(R.string.errmsg_no_profile);
                }

                summaryTextView.setText(summary_v);
                skillsTextView.setText(nSkills);
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(getActivity(), errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getActivity(), getString(R.string.errmsg_prof_ret_failure),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
