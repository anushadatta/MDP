package Multidisplinary.Project.MDP_Group_9.Settings;


import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import Multidisplinary.Project.MDP_Group_9.R;

import Multidisplinary.Project.MDP_Group_9.Fragments.CommunicationFragment;
import Multidisplinary.Project.MDP_Group_9.Fragments.ControlFragment;
import Multidisplinary.Project.MDP_Group_9.Fragments.MapFragment;
import Multidisplinary.Project.MDP_Group_9.Fragments.PlaceholderFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_2, R.string.tab_text_3, R.string.tab_text_4};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch(position) {
            case 0:
                return CommunicationFragment.newInstance(position +1);
            case 1:
                return MapFragment.newInstance(position+1);
            case 2:
                return ControlFragment.newInstance(position+1);
            default:
                return PlaceholderFragment.newInstance(position + 1);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }
}