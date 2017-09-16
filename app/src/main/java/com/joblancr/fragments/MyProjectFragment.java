package com.joblancr.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.joblancr.activitiesAndAdapters.PRVAdapter;
import com.joblancr.activitiesAndAdapters.R;
import com.joblancr.cards.Project;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MyProjectFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private List<Project> projects;
    private RecyclerView rv;

    private PRVAdapter adapter;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;
    private AsyncTask getMyProjects;

    public static MyProjectFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        MyProjectFragment fragment = new MyProjectFragment();
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
        projects = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_project, container, false);

        rv = (RecyclerView) view.findViewById(R.id.my_project_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getMyProjects = new getMyProjects().execute("getMyProjects", userDetails.get("id"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getMyProjects.cancel(true);
    }

    class getMyProjects extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar myProjProgBar;

        private static final String LOAD_PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/LoadProject.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
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
            myProjProgBar = (ProgressBar) getView().findViewById(R.id.myProjProgBar);
            myProjProgBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("type", args[0]);
                params.put("user_id", args[1]);

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
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String errormsg = "";

            myProjProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }

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
                adapter = new PRVAdapter(projects);
                rv.setAdapter(adapter);
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
