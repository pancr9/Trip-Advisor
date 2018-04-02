package builderspace.tripadvisor.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import builderspace.tripadvisor.controller.fragments.MyTrips;
import builderspace.tripadvisor.controller.fragments.OtherTrips;

public class TripsPagerAdapter extends FragmentPagerAdapter {

    public TripsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        Context mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0) {
            return new MyTrips();
        } else
            return new OtherTrips();

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0)
            return "My Trips";
        else return "Other Trips";
    }
}
