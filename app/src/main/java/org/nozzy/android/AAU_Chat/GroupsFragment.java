package org.nozzy.android.AAU_Chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    public static GroupsFragment newInstance(int position) {
        GroupsFragment fragment = new GroupsFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);

        return fragment;
    }

    public GroupsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            /* mPosition = getArguments().getInt("position");*/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);


        return view;
    }

}