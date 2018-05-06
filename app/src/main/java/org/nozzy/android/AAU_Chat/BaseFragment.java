package org.nozzy.android.AAU_Chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public abstract class BaseFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getCurrentFragmentLayout(), container, false);
        setHasOptionsMenu(true);
        return view;
    }

    public abstract String getFragmentTitle();

    protected abstract int getCurrentFragmentLayout();
}
