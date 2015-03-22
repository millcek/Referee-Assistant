package cz.vojacekmilan.refereeassistant;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.results.Club;
import cz.vojacekmilan.refereeassistant.results.Result;


public class ClubFragment extends Fragment {
    private static final String ID = "id";
    private int idClub;
    private ClubFragmentInteractionListener mListener;
    private TableLayout resultsTableLayout;

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
        resultsTableLayout = (TableLayout) view.findViewById(R.id.resultsTableLayout);
        loadResults();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ClubFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");//TODO obarvit radky tabulky podle toho kdo vyhral
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ClubFragmentInteractionListener {
        public Context getApplicationContext();
    }

    private void loadResults() {
        resultsTableLayout.removeAllViews();
        DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT round, id_clubs_home, id_clubs_away, home_score, away_score, home_score_half, away_score_half FROM results WHERE id_clubs_home = " + idClub + " OR id_clubs_away = " + idClub, null);
        List<Result> results = new LinkedList<>();

        while (cursor.moveToNext()) {
            Result result = new Result();
            result.setRound(cursor.getInt(0));
            Cursor homeCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(1), null);
            if (homeCursor.moveToNext())
                result.setHome(new Club(homeCursor.getString(0)));
            homeCursor.close();
            Cursor awayCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(2), null);
            if (awayCursor.moveToNext())
                result.setAway(new Club(awayCursor.getString(0)));
            awayCursor.close();
            result.setScore(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
            results.add(result);
        }
        makeResultsTable(results);
        cursor.close();
        db.close();
        databaseHelper.close();
    }

    private TextView newTableTextView(String text) {
        TextView textView = new TextView(mListener.getApplicationContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextAppearance(mListener.getApplicationContext(), android.R.style.TextAppearance_Small);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    private void makeResultsTable(List<Result> results) {
        resultsTableLayout.setColumnStretchable(1, true);
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        //region TableRow header
        TableRow headTableRow = new TableRow(mListener.getApplicationContext());
        String[] columnNames = new String[]{"Kolo", "Domácí", "Hosté", "Skóre"};//TODO z nastaveni brat sloupce ktere se maji zobrazit
        for (String s : columnNames)
            headTableRow.addView(newTableTextView(s));
        headTableRow.setLayoutParams(tableLayoutParams);
        resultsTableLayout.addView(headTableRow);
        //endregion
        int i = 0;
        for (final Result result : results) {
            TableRow tableRow = new TableRow(mListener.getApplicationContext());
            tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
            tableRow.addView(newTableTextView(String.valueOf(result.getRound())));
            tableRow.addView(newTableTextView(result.getHome().getName().replace("&quot;", "\"")));
            tableRow.addView(newTableTextView(result.getAway().getName().replace("&quot;", "\"")));
            TextView resultTextView = newTableTextView(result.getScore());
            resultTextView.setTypeface(null, Typeface.BOLD);//TODO pridat tlacitka klubu a akce
            tableRow.addView(resultTextView);
            tableRow.setLayoutParams(tableLayoutParams);
            resultsTableLayout.addView(tableRow);
            i++;
        }
    }

}
