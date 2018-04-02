package builderspace.tripadvisor.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import builderspace.tripadvisor.controller.fragments.FriendRequestsFragment;
import builderspace.tripadvisor.controller.fragments.MyFriendsFragment;
import builderspace.tripadvisor.controller.fragments.YouMayKnowFriendsFragment;

public class FriendsPagerAdapter extends FragmentPagerAdapter {

    public FriendsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        Context mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new MyFriendsFragment();
            case 1:
                return new FriendRequestsFragment();
            default:
                return new YouMayKnowFriendsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Friends";
            case 1:
                return "Requests";
            default:
                return "You May Know";
        }
    }
}
