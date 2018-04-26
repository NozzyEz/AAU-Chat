package org.nozzy.android.AAU_Chat;


<<<<<<< HEAD

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
=======
import android.os.Bundle;
import android.support.v4.app.Fragment;
>>>>>>> parent of d0d592b... Removed All references to the old name
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    public ChatsFragment() {
        // Required empty public constructor
    }


    // Initialize
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        ViewPager mViewPager = (ViewPager) view.findViewById(R.id.container_main);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        return view;

    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
// Fetching fragments and returning them in positions
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return PrivateFragment.newInstance(1);
                default:
                    return GroupsFragment.newInstance(2);
            }

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 2;
        }

// Renaming elements in positions
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "PRIVATE";
                default:
                    return "GROUPS";
            }


        }
    }


}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

<<<<<<< HEAD


=======
}
>>>>>>> parent of d0d592b... Removed All references to the old name
