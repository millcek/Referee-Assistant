package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;

public class RegionFragment extends Fragment implements AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String DB_NAME = "results";
    public static final String ID_REGION = "id_region";
    private RegionFragmentInteractionListener mListener;

    private ArrayAdapter regionAdapter;
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

    //TODO tlacitko na hledani
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

        AbsListView regionListView = (AbsListView) view.findViewById(R.id.listRegions);
        regionListView.setAdapter(regionAdapter);

        regionListView.setOnItemClickListener(this);

        regionListView.setOnItemLongClickListener(this);

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
        cursor = db.rawQuery("SELECT _id, name, favourite FROM regions WHERE id_regions = " + idRegion + ((idRegion == 0) ? " OR favourite=1 ORDER BY favourite DESC" : ""), null);
        while (cursor.moveToNext()) {
            Region region = new Region(cursor.getInt(0), cursor.getString(1));
            if (cursor.getInt(2) == 1)
                region.setFavourite(cursor.getInt(2) == 1);
            regions.add(region);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= boundary) {
            regions.get(position).negFavourite();
            if (mListener != null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), DB_NAME);
                databaseHelper.openDataBase();
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                db.execSQL("UPDATE regions SET favourite = " + (regions.get(position).isFavourite() ? 1 : 0) + " WHERE _id = " + regions.get(position).getId());
                db.close();
                databaseHelper.close();
                Toast.makeText(mListener.getApplicationContext(), "region " + regions.get(position).getName() + (regions.get(position).isFavourite() ? " přidán do oblíbených" : " odebrán z oblíbených"), Toast.LENGTH_SHORT).show();
                regionAdapter.notifyDataSetChanged();
                mListener.reloadMenu();
            }
            return true;
        }
        return false;
    }

    public interface RegionFragmentInteractionListener {
        void loadRegion(Region region);
        void loadLeague(int id);
        void reloadMenu();
        Context getApplicationContext();
    }

}
