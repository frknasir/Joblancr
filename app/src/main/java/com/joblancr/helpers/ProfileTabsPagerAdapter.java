package com.joblancr.helpers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.joblancr.fragments.AboutFragment;
import com.joblancr.fragments.ReviewsFragment;

/**
 * Created by Faruk on 9/16/16.
 */
public class ProfileTabsPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "About", "Reviews" };
    private Context context;

    public ProfileTabsPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0 :
                return AboutFragment.newInstance(0);
            case 1 :
                return ReviewsFragment.newInstance(1);
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
