package com.nvite.container;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nvite.R;
import com.nvite.fragment.PublicEventsFragment;

/**
 * Created by android on 16/3/17.
 */

public class PublicEventsContainer extends Fragment_Container {
    private boolean mIsViewInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.container_for_tab, null);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!mIsViewInited) {
            mIsViewInited = true;
            onIntialView();
        }
    }

    private void onIntialView() {
        replaceFragment(new PublicEventsFragment(), false);
    }
}

