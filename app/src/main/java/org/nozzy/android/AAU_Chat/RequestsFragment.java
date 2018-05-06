package org.nozzy.android.AAU_Chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;


/**
 * A simple {@link Fragment} subclass.
 */

public class RequestsFragment extends BaseFragment {
    
    private static final String TAG = RequestsFragment.class.getSimpleName();


    @Override
    public String getFragmentTitle() {
        return "Requests";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_requests;
    }
    public static String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //imageView = (ImageView) getView().findViewById(R.id.image);
        //imageView.setImageDrawable();

    }
}
