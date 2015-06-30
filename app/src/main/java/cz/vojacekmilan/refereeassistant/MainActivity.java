package cz.vojacekmilan.refereeassistant;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cz.vojacekmilan.refereeassistant.results.ClubFragment;
import cz.vojacekmilan.refereeassistant.results.LeagueFragment;
import cz.vojacekmilan.refereeassistant.results.Region;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;
import cz.vojacekmilan.refereeassistant.results.ResultSearchFragment;
import cz.vojacekmilan.refereeassistant.tests.TestsExamLauncherActivity;
import cz.vojacekmilan.refereeassistant.tests.TestsFragment;
import cz.vojacekmilan.refereeassistant.tests.TestsPracticeActivity;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, RegionFragment.RegionFragmentInteractionListener, LeagueFragment.LeagueFragmentInteractionListener, ClubFragment.ClubFragmentInteractionListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private Stack<String> titlesStack;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (titlesStack.size() > 0)
            titlesStack.pop();
        if (titlesStack.size() > 0)
            setTitle(titlesStack.pop());
        else
            finish();
        super.onBackPressed();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
            fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (titlesStack != null)
            titlesStack.clear();//TODO vybrat z db pocet oblibenych

        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM leagues WHERE favourite=1 ORDER BY _id", null);
        List<Integer> ids = new LinkedList<>();
        while (cursor.moveToNext()) {
            ids.add(cursor.getInt(0));
        }
        cursor.close();
        db.close();
        databaseHelper.close();
        int favouriteCount = ids.size();
        if (position > 1 && position < favouriteCount + 2) {
            loadLeague(ids.get(position - 2));
        } else if (position < 2)
            switch (position) {
                case 0:
                    fragmentTransaction.replace(R.id.container, ResultSearchFragment.newInstance());
                    setTitle(null);
                    break;
                case 1:
                    loadRegion(0);
                    break;
            }
        else
            switch (position - favouriteCount - 1) {
                case 1:
                    fragmentTransaction.replace(R.id.container, TestsFragment.newInstance());
                    setTitle(null);
                    break;
                case 2:
                    startActivity(new Intent(this, TestsPracticeActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(this, TestsExamLauncherActivity.class));
                    break;
            }
        fragmentTransaction.commit();
        if (mTitle == null || mTitle.length() == 0)
            setTitle(null);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void setTitle(String title) {
        if (titlesStack == null)
            titlesStack = new Stack<>();
        if (title != null) {
            if (title.length() > 0)
                mTitle = title;
            else
                mTitle = getString(R.string.title_activity_main);
            titlesStack.push(title);
        } else
            mTitle = getString(R.string.title_activity_main);
        restoreActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void reloadMenu() {
        if (mNavigationDrawerFragment != null)
            mNavigationDrawerFragment.reloadMenu();
    }

    public void loadRegion(Region region) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, RegionFragment.newInstance(region.getId()));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(region.getName());
    }


    @Override
    public void loadClub(int id) {
        if (id < 1)
            return;
        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(String.format("SELECT _id FROM results WHERE id_clubs_home = %d OR id_clubs_away = %d", id, id), null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        databaseHelper.close();

        if (count < 1) {
            Toast.makeText(getApplicationContext(), "Pro vybraný tým nejsou k dispozici žádné výsledky", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, ClubFragment.newInstance(id));
        fragmentTransaction.addToBackStack(null);
        restoreActionBar();
        fragmentTransaction.commit();
        setTitle(selectName(id, "clubs").replace("&quot;", "\""));
    }

    @Override
    public void loadLeague(int id) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, LeagueFragment.newInstance(id));
        restoreActionBar();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        setTitle(selectName(id, "leagues"));
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
        String name = "";
        if (cursor.moveToNext())
            name = cursor.getString(0);
        cursor.close();
        db.close();
        databaseHelper.close();
        return name;
    }
}
