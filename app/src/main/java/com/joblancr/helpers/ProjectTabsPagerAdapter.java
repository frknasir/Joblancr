package com.joblancr.helpers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.joblancr.fragments.HiredOnFragment;
import com.joblancr.fragments.MyProjectFragment;

/**
 * Created by Faruk on 9/16/16.
 */
public class ProjectTabsPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "My Projects", "Hired On" };
    private Context context;

    public ProjectTabsPagerAdapter(FragmentManager manager, Context context) {
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
                return MyProjectFragment.newInstance(0);
            case 1 :
                return HiredOnFragment.newInstance(1);
        }

        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
