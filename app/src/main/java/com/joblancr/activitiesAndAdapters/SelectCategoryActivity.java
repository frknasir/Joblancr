package com.joblancr.activitiesAndAdapters;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.joblancr.cards.SelectCategoryCard;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends AppCompatActivity {

    private List<SelectCategoryCard> selectCategoryCardLists;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();

        rv = (RecyclerView) findViewById(R.id.select_category_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeData() {
        selectCategoryCardLists = new ArrayList<>();

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.local_jobs,
                    1, getResources().getString(R.string.label_local_jobs)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.web_development,
                2, getResources().getString(R.string.label_web_development)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.software_development,
                3, getResources().getString(R.string.label_software_dev)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.writing,
                4, getResources().getString(R.string.label_writing)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.event_planning,
                5, getResources().getString(R.string.label_event_planning)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.sales,
                6, getResources().getString(R.string.label_market_sales)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.music_production,
                7, getResources().getString(R.string.label_music_prod)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.film_making,
                8, getResources().getString(R.string.label_film_making)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.graphic_design,
                9, getResources().getString(R.string.label_graphic_design)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.fashion,
                10, getResources().getString(R.string.label_fashion)));

        selectCategoryCardLists.add(new SelectCategoryCard(R.drawable.other,
                11, getResources().getString(R.string.label_others)));
    }

    private void initializeAdapter() {
        SelectCategoryAdapter adapter = new SelectCategoryAdapter(selectCategoryCardLists);
        rv.setAdapter(adapter);
    }
}
