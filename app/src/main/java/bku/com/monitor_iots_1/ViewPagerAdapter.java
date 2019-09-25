package bku.com.monitor_iots_1;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Phu on 9/25/2019.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {




    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return  new RealTimeGraph();
            case 1:
                return new RealTimeGraph1();
            default:
                return new RealTimeGraph();

        }
    }

    @Override
    public int getCount() {
        return 2;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        switch (position){
            case 0:
                title = "One";
                break;
            case 1:
                title = "Two";
                break;
        }
        return title;
    }
}
