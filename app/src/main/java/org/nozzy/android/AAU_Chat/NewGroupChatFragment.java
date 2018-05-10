package org.nozzy.android.AAU_Chat;

/**
 * Created by default on 5/10/18.
 */

public class NewGroupChatFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public String getFragmentTitle() { return  "New Group"; }



    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_new_group_chat;
    }
    public static String getFragmentTag() {
        return TAG;
    }
}
