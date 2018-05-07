package org.nozzy.android.AAU_Chat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Nozzy on 24/03/2018.
 */

// Class to facilitate our fragments on the main activity.
// 0 - RequestsFragment, 1 - ChatsFragment, 2 - FriendsFragment
class SectionsPagerAdapter extends FragmentPagerAdapter{
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){
        switch (position) {
            case 0:
                return "CHATS";

            case 1:
                return "FRIENDS";

            default:
                return null;
        }
    }
}
