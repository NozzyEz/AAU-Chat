package org.nozzy.android.AAU_Chat;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class NewGroupChatFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private String users;

    @Override
    public String getFragmentTitle() { return  "New Group"; }



    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_new_group_chat;
    }
    public static String getFragmentTag() {
        return TAG;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            users = bundle.getString("users");
        }
        TextView textView = getView().findViewById(R.id.text);
        textView.setText(users);
    }
}
