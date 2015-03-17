package cz.vojacekmilan.refereeassistant;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import cz.vojacekmilan.refereeassistant.results.LeagueFragment;
import cz.vojacekmilan.refereeassistant.results.Region;
import cz.vojacekmilan.refereeassistant.tests.TestsFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, RegionFragment.RegionFragmentInteractionListener, LeagueFragment.LeagueFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (position) {
            case 0:
                fragmentTransaction.replace(R.id.container, RegionFragment.newInstance(0));
                loadLeague(5);
                break;
            case 1:
                fragmentTransaction.replace(R.id.container, RegionFragment.newInstance(0));
                break;
            case 2:
                fragmentTransaction.replace(R.id.container, TestsFragment.newInstance());
                break;
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        String[] titles = getResources().getStringArray(R.array.nav_drawer_items);
        if (position < titles.length)
            mTitle = titles[position];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void loadRegion(Region region) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, RegionFragment.newInstance(region.getId()));
        mTitle = region.getName();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void loadLeague(int id) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, LeagueFragment.newInstance(id));
        mTitle = selectName(id, "leagues");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void loadRegion(int id) {
        loadRegion(new Region(id, selectName(id, "regions")));
    }

    private String selectName(int id, String table) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM " + table + " WHERE _id = " + id, null);
        String title = "";
        if (cursor.moveToNext())
            title = cursor.getString(0);
        cursor.close();
        db.close();
        databaseHelper.close();
        return title;
    }
}
