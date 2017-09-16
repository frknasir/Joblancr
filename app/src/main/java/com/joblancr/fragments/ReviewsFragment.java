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

import com.joblancr.activitiesAndAdapters.R;
import com.joblancr.cards.ReviewCard;
import com.joblancr.helpers.JSONParser;
import com.joblancr.helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReviewsFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;

    private List<ReviewCard> reviews;
    private RecyclerView rv;

    private HashMap<String, String> userDetails = new HashMap<String, String>();

    private SessionManager sessionManager;

    private Bundle extras;

    private String user_profile_id = null;

    ReviewCardAdapter adapter;
    private AsyncTask getReview;

    public static ReviewsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ReviewsFragment fragment = new ReviewsFragment();
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

        reviews = new ArrayList<>();

        extras = getActivity().getIntent().getExtras();

        if(extras != null) {
            user_profile_id = extras.getString("bidder_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        rv = (RecyclerView) view.findViewById(R.id.reviews_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(user_profile_id == null) {
            getReview = new getReviews().execute("getReviews", userDetails.get("id"));
        } else {
            getReview = new getReviews().execute("getReviews", user_profile_id);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        getReview.cancel(true);
    }

    class getReviews extends AsyncTask<String, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();
        JSONObject json;

        private ProgressBar revProgBar;

        private static final String PROJECT_URL = "http://192.168.43.14/Joblancr/Php/Webservice/Review.php";

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "error-msg";
        private static final String TAG_REVIEWS = "reviews";
        private static final String TAG_IMAGE = "image";
        private static final String TAG_USER_ID = "user_id";
        private static final String TAG_NAME = "full_name";
        private static final String TAG_RATING = "rating_value";
        private static final String TAG_DATE = "rating_date";
        private static final String TAG_REVIEW_COMMENT = "comment";


        @Override
        protected void onPreExecute() {
            revProgBar = (ProgressBar) getView().findViewById(R.id.revProgBar);
            revProgBar.setVisibility(View.VISIBLE);
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
                            PROJECT_URL, "POST", params);
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

            revProgBar.setVisibility(View.GONE);

            if(json != null) {
                try {
                    success = json.getInt(TAG_SUCCESS);
                    if(success != 1) {
                        errormsg = json.getString(TAG_MESSAGE);
                    }

                    JSONArray reviewz = json.getJSONArray(TAG_REVIEWS);

                    for(int i = 0; i < reviewz.length(); i++) {
                        JSONObject reviewObj = (JSONObject) reviewz.get(i);

                        reviews.add(new ReviewCard(reviewObj.getString(TAG_IMAGE),
                                reviewObj.getString(TAG_USER_ID), reviewObj.getString(TAG_NAME),
                                reviewObj.getDouble(TAG_RATING), reviewObj.getString(TAG_DATE),
                                reviewObj.getString(TAG_REVIEW_COMMENT)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if(success == 1) {
                adapter = new ReviewCardAdapter(reviews);
                rv.setAdapter(adapter);
            } else {
                if(!errormsg.equals("")) {
                    Log.e("Failure", errormsg);
                    Toast.makeText(getActivity(), errormsg, Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(getActivity(), getString(R.string.errmsg_reviews_ret_failure),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
