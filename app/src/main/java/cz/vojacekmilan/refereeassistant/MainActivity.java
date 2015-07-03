package cz.vojacekmilan.refereeassistant;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cz.vojacekmilan.refereeassistant.results.ClubFragment;
import cz.vojacekmilan.refereeassistant.results.HomeFragment;
import cz.vojacekmilan.refereeassistant.results.League;
import cz.vojacekmilan.refereeassistant.results.LeagueFragment;
import cz.vojacekmilan.refereeassistant.results.Region;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;
import cz.vojacekmilan.refereeassistant.tests.TestsExamLauncherActivity;
import cz.vojacekmilan.refereeassistant.tests.TestsFragment;
import cz.vojacekmilan.refereeassistant.tests.TestsPracticeActivity;


public class MainActivity extends ActionBarActivity
        implements RegionFragment.OnFragmentInteractionListener, LeagueFragment.OnFragmentInteractionListener, ClubFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private CharSequence mTitle;
    private Stack<String> titlesStack;

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerRelativeLayout;
    private ListView mDrawerListView;
    private int mCurrentSelectedPosition = 0;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerItem[] drawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        mTitle = toolbar.getTitle();

        reloadMenu();
        mDrawerListView = (ListView) findViewById(R.id.list_view);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setAdapter(new DrawerItemAdapter(this,
                R.layout.fragment_navigation_drawer_list_item, drawerItems));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        mDrawerLayout.openDrawer(mDrawerRelativeLayout);
        ListUtils.setDynamicHeight(mDrawerListView);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open,
                R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                toolbar.setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }

            public void onDrawerOpened(View drawerView) {
                toolbar.setTitle(R.string.app_name);
                ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
                scrollView.pageScroll(View.FOCUS_UP);
                ActivityCompat.invalidateOptionsMenu(MainActivity.this);
            }
        };
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        selectItem(mCurrentSelectedPosition);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerOpen(mDrawerRelativeLayout))
                mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
            else
                mDrawerLayout.openDrawer(mDrawerRelativeLayout);
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, R.string.action_settings, Toast.LENGTH_SHORT).show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void selectItem(int position) {
        try {
            drawerItems[mCurrentSelectedPosition].deactivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            drawerItems[position].activate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
        }
        onNavigationDrawerItemSelected(position);
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

    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
            fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (titlesStack != null)
            titlesStack.clear();
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
                    fragmentTransaction.replace(R.id.container, HomeFragment.newInstance());
                    setTitle(null);
                    break;
                case 1:
                    loadRegion(0);
                    break;
            }
        else {
            switch (position - favouriteCount - 1) {
                case 1:
                    fragmentTransaction.replace(R.id.container, TestsFragment.newInstance());
                    break;
                case 2:
                    startActivity(new Intent(this, TestsPracticeActivity.class));
                    break;
                case 3:
                    startActivity(new Intent(this, TestsExamLauncherActivity.class));
                    break;
            }
            setTitle(null);
        }
        fragmentTransaction.commit();
        if (mTitle == null || mTitle.length() == 0)
            setTitle(null);
    }

    public void reloadMenu() {
        CharSequence[] menuList = getResources().getTextArray(R.array.menu);
        List<List<String>> subMenuLists = new ArrayList<>(menuList.length);
        for (CharSequence ignored : menuList)
            subMenuLists.add(new LinkedList<String>());

        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM leagues WHERE favourite=1 ORDER BY _id", null);
        while (cursor.moveToNext()) {
            subMenuLists.get(1).add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        databaseHelper.close();

        subMenuLists.get(2).add(this.getResources().getString(R.string.title_activity_tests_browsing));
        subMenuLists.get(2).add(this.getResources().getString(R.string.title_activity_tests_exam));

        int[] iconArray = new int[]{R.drawable.ic_home,
                R.drawable.ic_results,
                R.drawable.ic_test};
        int count = menuList.length;
        for (List<String> list : subMenuLists)
            count += list.size();
        drawerItems = new DrawerItem[count];
        int i = 0;
        int j = 0;
        for (List<String> list : subMenuLists) {
            drawerItems[j++] = new DrawerItem(iconArray[i], String.valueOf(menuList[i++]));
            for (String s : list)
                drawerItems[j++] = new DrawerItem(R.id.icon, s);//TODO ikona
        }
        for (DrawerItem drawerItem : drawerItems)
            Log.i("drawerItems", drawerItem.getName());
        //TODO obnovit listview
        if (mDrawerListView != null) {
            mDrawerListView.setAdapter(new DrawerItemAdapter(this,
                    R.layout.fragment_navigation_drawer_list_item, drawerItems));
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
            ListUtils.setDynamicHeight(mDrawerListView);
        }
    }

    public void restoreActionBar() {
        toolbar.setTitle(mTitle);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setTitle(mTitle);
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
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    public void loadRegion(Region region) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, RegionFragment.newInstance(region.getId()));
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        restoreActionBar();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void loadLeague(int id) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, LeagueFragment.newInstance(id));
        fragmentTransaction.addToBackStack(null);
        restoreActionBar();
        fragmentTransaction.commit();
        setTitle(selectName(id, "leagues"));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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
