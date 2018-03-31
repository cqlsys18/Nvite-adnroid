package com.nvite.container;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.nvite.R;


/**
 * Created by android on 16/3/17.
 */

public class Fragment_Container extends Fragment {
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.replace(R.id.frame_layout_for_container, fragment, "search");
        transaction.commit();
        getChildFragmentManager().executePendingTransactions();
    }

    public boolean popFragment() {
        boolean isPop = false;
        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            isPop = true;
            getChildFragmentManager().popBackStack();
        }
        return isPop;
    }

}