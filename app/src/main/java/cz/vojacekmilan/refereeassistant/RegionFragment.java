package cz.vojacekmilan.refereeassistant;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.results.Region;

public class RegionFragment extends Fragment implements AbsListView.OnItemClickListener {
    public static final String DB_NAME = "results";
    public static final String ID_REGION = "id_region";
    private RegionFragmentInteractionListener mListener;

    private ArrayAdapter regionAdapter;
    private AbsListView regionListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Region> regions;
    private int idRegion;
    private int boundary;

    public static RegionFragment newInstance(int idRegion) {
        RegionFragment fragment = new RegionFragment();
        Bundle args = new Bundle();
        args.putInt(ID_REGION, idRegion);
        fragment.setArguments(args);
        return fragment;
    }

    public RegionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            idRegion = getArguments().getInt(ID_REGION);
        regions = new ArrayList<>();
        regionAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, regions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        regionListView = (AbsListView) view.findViewById(R.id.listRegions);
        regionListView.setAdapter(regionAdapter);

        regionListView.setOnItemClickListener(this);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);
        regions.clear();

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int i = 0;
        Cursor cursor = db.rawQuery("SELECT _id, name FROM leagues WHERE id_regions = " + idRegion, null);
        while (cursor.moveToNext()) {
            regions.add(new Region(cursor.getInt(0), cursor.getString(1)));
            i++;
        }
        boundary = i;
        cursor.close();
        cursor = db.rawQuery("SELECT _id, name FROM regions WHERE id_regions = " + idRegion + ((idRegion == 0) ? " OR favourite=1 ORDER BY favourite DESC" : ""), null);
        while (cursor.moveToNext()) {
            regions.add(new Region(cursor.getInt(0), cursor.getString(1)));
        }
        cursor.close();
        db.close();
        databaseHelper.close();
        regionAdapter.notifyDataSetChanged();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (RegionFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            if (position < boundary) {
                mListener.loadLeague(regions.get(position).getId());
            } else {
                mListener.loadRegion(regions.get(position));
            }
        }
    }

    public interface RegionFragmentInteractionListener {
        public void loadRegion(Region region);

        public void loadLeague(int id);
    }

}
