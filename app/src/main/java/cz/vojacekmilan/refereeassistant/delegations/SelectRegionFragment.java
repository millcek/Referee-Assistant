package cz.vojacekmilan.refereeassistant.delegations;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.results.RegionAdapter;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;

public class SelectRegionFragment extends Fragment {
    OnFragmentInteractionListener mListener;
    private SearchView searchView;
    private ListView listView;
    private RegionAdapter adapter;
    private List<RegionAdapter.RegionItem> items;

    public static SelectRegionFragment newInstance() {
        return new SelectRegionFragment();
    }

    public SelectRegionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_region, container, false);
        searchView = (SearchView) view.findViewById(R.id.search_view);
        listView = (ListView) view.findViewById(R.id.list_view);
        items = new LinkedList<>();
        adapter = new RegionAdapter(getActivity(), R.layout.icon_list_item, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.addDelegation(items.get(position).getId(), items.get(position).getText());
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fillListView(newText);
                return false;
            }
        });
        searchView.requestFocus();
        fillListView("");
        return view;
    }

    private void fillListView(String text) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT _id, name FROM regions" +
                (text.isEmpty() ? "" : " WHERE name LIKE \"%" + text + "%\""));// TODO hledat po slovech
        items.clear();
        while (cursor.moveToNext())
            items.add(new RegionAdapter.RegionItem(cursor.getInt(0), R.drawable.ic_region, cursor.getString(1)));//TODO ikona
        cursor.close();
        databaseHelper.close();
        adapter.notifyDataSetChanged();
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
        void addDelegation(int idRegion, String name);
    }

}
