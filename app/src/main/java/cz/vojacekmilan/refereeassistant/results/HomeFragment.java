package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;

public class HomeFragment extends Fragment {
    OnFragmentInteractionListener mListener;
    private SearchView searchView;
    private ListView listView;
    private RegionAdapter adapter;
    private List<RegionItem> items;
    private FloatingActionButton floatingActionButton;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchView = (SearchView) view.findViewById(R.id.search_view);
        listView = (ListView) view.findViewById(R.id.list_view);
        items = new LinkedList<>();
        adapter = new RegionAdapter(getActivity(), R.layout.icon_list_item, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
                databaseHelper.openDataBase();
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT _id, name FROM regions WHERE name LIKE \"%" + newText + "%\"", null);// TODO hledat po slovech
                items.clear();
                while (cursor.moveToNext())
                    items.add(new RegionItem(cursor.getInt(0), R.drawable.ic_region, cursor.getString(1)));//TODO ikona
                cursor.close();
                db.close();
                databaseHelper.close();
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "ahoj", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (Exception e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    }
}
