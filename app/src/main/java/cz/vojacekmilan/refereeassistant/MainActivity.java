package cz.vojacekmilan.refereeassistant;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cz.vojacekmilan.refereeassistant.delegations.AddDelegationFragment;
import cz.vojacekmilan.refereeassistant.delegations.HomeFragment;
import cz.vojacekmilan.refereeassistant.delegations.RefereeFragment;
import cz.vojacekmilan.refereeassistant.delegations.SelectRegionFragment;
import cz.vojacekmilan.refereeassistant.results.ClubFragment;
import cz.vojacekmilan.refereeassistant.results.LeagueAdapter;
import cz.vojacekmilan.refereeassistant.results.LeagueFragment;
import cz.vojacekmilan.refereeassistant.results.Region;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;
import cz.vojacekmilan.refereeassistant.tests.ExamLauncherActivity;
import cz.vojacekmilan.refereeassistant.tests.PracticeActivity;


public class MainActivity extends AppCompatActivity
        implements RegionFragment.OnFragmentInteractionListener, LeagueFragment.OnFragmentInteractionListener,
        ClubFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener,
        SelectRegionFragment.OnFragmentInteractionListener, AddDelegationFragment.OnFragmentInteractionListener, RefereeFragment.OnFragmentInteractionListener {
    private SubMenu favouritesSubMenu;
    private Stack<CharSequence> titlesStack;
    private NavigationView navigation;
    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigation = (NavigationView) findViewById(R.id.navigation_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectItem(menuItem);
                return true;
            }
        });
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open,
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titlesStack.size() > 1)
                    onBackPressed();
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        reloadMenu();
        selectItem(navigation.getMenu().findItem(R.id.menu_item_home));
        restoreActionBar();
    }

    private void selectItem(MenuItem menuItem) {
        if (menuItem == null) return;
        FragmentManager fm = getSupportFragmentManager();
        int backStackCount = fm.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            int backStackId = fm.getBackStackEntryAt(i).getId();
            fm.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (titlesStack != null)
            titlesStack.clear();
        if (menuItem.getGroupId() == R.id.menu_item_submenu_favourites) {
            loadLeague(menuItem.getItemId());
            navigation.getMenu().findItem(R.id.menu_item_results).setChecked(true);
        } else if (menuItem.getGroupId() == R.id.menu_item_submenu_tests) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_tests_exam:
                    startActivity(new Intent(this, ExamLauncherActivity.class));
                    break;
                case R.id.menu_item_tests_practice:
                    startActivity(new Intent(this, PracticeActivity.class));
                    break;
            }
        } else {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit();
                    setTitle(null);
                    break;
                case R.id.menu_item_results:
                    loadRegion(0);
                    break;
            }
            navigation.getMenu().findItem(menuItem.getItemId()).setChecked(true);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (titlesStack == null || titlesStack.size() < 2) {
            finish();
            return;
        }
        titlesStack.pop();
        setTitle(titlesStack.pop());
        super.onBackPressed();
    }

    public void reloadMenu() {
        TextView textViewRegion = (TextView) findViewById(R.id.text_view_region);
        TextView textViewName = (TextView) findViewById(R.id.text_view_name);
        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT subscribed_delegations.name, " +
                "(SELECT regions.name FROM regions WHERE _id = subscribed_delegations.id_regions) " +
                "FROM subscribed_delegations");
        if (cursor.moveToNext()) {
            textViewRegion.setText(cursor.getString(1));
            textViewName.setText(cursor.getString(0));
        } else {
            textViewName.setText(R.string.blank_name);
            textViewRegion.setText(R.string.no_delegation);
        }
        cursor.close();
        List<String> strings = new LinkedList<>();
        List<Integer> integers = new LinkedList<>();
        cursor = databaseHelper.rawQuery("SELECT _id, name FROM leagues WHERE favourite=1 ORDER BY _id");
        while (cursor.moveToNext()) {
            integers.add(cursor.getInt(0));
            strings.add(cursor.getString(1));
        }
        cursor.close();
        databaseHelper.close();
        if (favouritesSubMenu != null)
            favouritesSubMenu.clear();
        if (strings.size() > 0) {
            if (favouritesSubMenu == null)
                favouritesSubMenu = navigation.getMenu().addSubMenu(R.string.favourites);
            for (int i = 0; i < strings.size(); i++)
                favouritesSubMenu.add(R.id.menu_item_submenu_favourites, integers.get(i),
                        favouritesSubMenu.size() + 1, strings.get(i));
        }
        for (int i = 0, count = navigation.getChildCount(); i < count; i++) {
            final View child = navigation.getChildAt(i);
            if (child != null && child instanceof ListView) {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
    }

    private void restoreActionBar() {
        if (titlesStack != null && titlesStack.size() > 1) {
            toolbar.setNavigationIcon(R.drawable.arrow_back);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_drawer);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (titlesStack == null)
            titlesStack = new Stack<>();
        if (title == null || title.length() == 0)
            title = getString(R.string.title_activity_main);
        titlesStack.push(title);
        toolbar.setTitle(title);
        restoreActionBar();
    }

    @Override
    public void loadClub(int id) {
        if (id < 1)
            return;
        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery(String.format(
                "SELECT _id FROM results WHERE id_clubs_home = %d OR id_clubs_away = %d", id, id));
        int count = cursor.getCount();
        cursor.close();
        databaseHelper.close();
        if (count < 1) {
            Toast.makeText(this,
                    "Pro vybraný tým nejsou k dispozici žádné výsledky", Toast.LENGTH_SHORT).show();
            return;
        }
        replaceFragment(ClubFragment.newInstance(id), selectName(id, "clubs").replace("&quot;", "\""));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void editDelegations() {
        replaceFragment(RefereeFragment.newInstance(), R.string.edit_subscribed_delegations);
    }

    @Override
    public void loadLeague(int id) {
        replaceFragment(LeagueFragment.newInstance(id), selectName(id, "leagues"));
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

    private void loadRegion(Region region) {
        replaceFragment(RegionFragment.newInstance(region.getId()), region.getName());
    }

    private String selectName(int id, String table) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this, RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT name FROM " + table + " WHERE _id = " + id);
        String name = "";
        if (cursor.moveToNext())
            name = cursor.getString(0);
        cursor.close();
        databaseHelper.close();
        return name;
    }

    @Override
    public void selectRegionForDelegation() {
        replaceFragment(SelectRegionFragment.newInstance(), R.string.add_referee);
    }

    @Override
    public void editReferee(int id) {

    }

    @Override
    public void addDelegation(int idRegion, String name) {
        replaceFragment(AddDelegationFragment.newInstance(idRegion), name);
    }

    private void replaceFragment(Fragment fragment, int title) {
        replaceFragment(fragment, getResources().getString(title));
    }

    private void replaceFragment(Fragment fragment, String title) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        restoreActionBar();
        setTitle(title);
    }
}
