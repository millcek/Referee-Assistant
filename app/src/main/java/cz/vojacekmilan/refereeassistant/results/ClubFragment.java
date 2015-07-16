package cz.vojacekmilan.refereeassistant.results;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.Utils;


public class ClubFragment extends Fragment {
    private static final String ID = "id";
    private int idClub;
    private OnFragmentInteractionListener mListener;

    private ListView listViewResults;
    private List<Result> results;
    private ResultAdapter resultsAdapter;

    public static ClubFragment newInstance(int id) {
        ClubFragment fragment = new ClubFragment();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public ClubFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idClub = getArguments().getInt(ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_club, container, false);
        listViewResults = (ListView) view.findViewById(R.id.list_view_results);
        results = new LinkedList<>();
        resultsAdapter = new ResultAdapter(getActivity(), R.layout.result_list_item, results);
        listViewResults.setAdapter(resultsAdapter);
        listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Utils.showPopup(getActivity(), listViewResults, results.get(position).getNote().isEmpty() ?
                        getResources().getString(R.string.no_note) : results.get(position).getNote());
            }
        });
        loadResults();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
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

    private void loadResults() {
        results.clear();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT round, (SELECT name FROM clubs " +
                "WHERE id_clubs_home=clubs._id), (SELECT name FROM clubs WHERE id_clubs_away=clubs._id), " +
                "home_score, away_score, home_score_half, away_score_half, note FROM results " +
                "WHERE id_clubs_home = " + idClub + " OR id_clubs_away = " + idClub, null);
        while (cursor.moveToNext()) {
            Result result = new Result();
            result.setRound(cursor.getInt(0));
            result.setHome(cursor.getString(1).replace("&quot;", "\""));
            result.setAway(cursor.getString(2).replace("&quot;", "\""));
            result.setScore(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
            result.setNote(cursor.getString(7));
            results.add(result);
        }
        cursor.close();
        databaseHelper.close();
        resultsAdapter.notifyDataSetChanged();
    }

}
