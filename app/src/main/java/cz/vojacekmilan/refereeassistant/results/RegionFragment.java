package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.ListUtils;
import cz.vojacekmilan.refereeassistant.R;

public class RegionFragment extends Fragment {
    public static final String DB_NAME = "results";
    public static final String ID_REGION = "id_region";
    private OnFragmentInteractionListener mListener;

    private LeagueAdapter leagueAdapter;
    private List<LeagueItem> leagues;
    private RegionAdapter regionAdapter;
    private List<RegionItem> regions;
    private int idRegion;

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
        regions = new LinkedList<>();
        leagues = new LinkedList<>();
        regionAdapter = new RegionAdapter(getActivity(), R.layout.icon_list_item, regions);
        leagueAdapter = new LeagueAdapter(getActivity(), R.layout.icon_list_item, leagues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        ListView regionListView = (ListView) view.findViewById(R.id.list_regions);
        ListView leagueListView = (ListView) view.findViewById(R.id.list_leagues);

        regionListView.setAdapter(regionAdapter);
        leagueListView.setAdapter(leagueAdapter);

        regionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    mListener.loadRegion(regions.get(position).getId());
                }
            }
        });
        leagueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    mListener.loadLeague(leagues.get(position).getId());
                }
            }
        });
        leagueListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), DB_NAME);
                    databaseHelper.openDataBase();
                    SQLiteDatabase db = databaseHelper.getReadableDatabase();
                    leagues.get(position).setFavourite(!leagues.get(position).isFavourite());
                    db.execSQL("UPDATE leagues SET favourite = " + (leagues.get(position).isFavourite() ? 1 : 0) + " WHERE _id = " + leagues.get(position).getId());
                    db.close();
                    databaseHelper.close();
                    Toast.makeText(mListener.getApplicationContext(), "soutěž " + leagues.get(position).getText() + (leagues.get(position).isFavourite() ? " přidána do oblíbených" : " odebrána z oblíbených"), Toast.LENGTH_SHORT).show();
                    leagueAdapter.notifyDataSetChanged();
                    mListener.reloadMenu();
                }
                return true;
            }
        });
        regions.clear();
        leagues.clear();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, name FROM regions WHERE id_regions = " + idRegion, null);
        while (cursor.moveToNext())
            regions.add(new RegionItem(cursor.getInt(0), R.drawable.ic_region, cursor.getString(1)));//TODO ikona
        cursor.close();
        cursor = db.rawQuery("SELECT _id, name, favourite FROM leagues WHERE id_regions = " + idRegion, null);
        while (cursor.moveToNext())
            leagues.add(new LeagueItem(cursor.getInt(0), R.drawable.ic_results, cursor.getString(1), cursor.getInt(2) == 1));//TODO ikona
        cursor.close();
        db.close();
        databaseHelper.close();
        Log.i("leagues", Arrays.toString(leagues.toArray()));
        Log.i("regions", Arrays.toString(regions.toArray()));
        regionAdapter.notifyDataSetChanged();
        leagueAdapter.notifyDataSetChanged();
        if (leagues.size() == 0)
            ((TextView) view.findViewById(R.id.text_view_leagues)).setHeight(0);
        if (regions.size() == 0)
            ((TextView) view.findViewById(R.id.text_view_regions)).setHeight(0);
        ListUtils.setDynamicHeight(regionListView);
        ListUtils.setDynamicHeight(leagueListView);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void loadRegion(int id);

        void loadLeague(int id);

        void reloadMenu();

        Context getApplicationContext();
    }

}
