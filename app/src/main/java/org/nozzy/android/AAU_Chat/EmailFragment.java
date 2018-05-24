package org.nozzy.android.AAU_Chat;

import android.support.v4.app.Fragment;

// This fragment shows all of the user's friends in a recycler view

/**
 * A simple {@link Fragment} subclass.
 */
public class EmailFragment extends BaseFragment {

    // UI

    public EmailFragment() {
        // Required empty public constructor
    }

    private static final String TAG = EmailFragment.class.getSimpleName();

    public static String getFragmentTitle() {
        return "Email";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_friends;
    }
    public static String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
