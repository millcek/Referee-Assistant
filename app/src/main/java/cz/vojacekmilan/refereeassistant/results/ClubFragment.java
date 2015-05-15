package cz.vojacekmilan.refereeassistant.results;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.Utils;


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
                    + " must implement OnFragmentInteractionListener");
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
        Cursor cursor = db.rawQuery("SELECT round, id_clubs_home, id_clubs_away, home_score, away_score, home_score_half, away_score_half, note FROM results WHERE id_clubs_home = " + idClub + " OR id_clubs_away = " + idClub, null);
        List<Result> results = new LinkedList<>();

        while (cursor.moveToNext()) {
            Result result = new Result();
            result.setRound(cursor.getInt(0));
            Cursor homeCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(1), null);
            if (homeCursor.moveToNext()) {
                result.setHome(homeCursor.getString(0));
                result.setIdHome(cursor.getInt(1));
            }
            homeCursor.close();
            Cursor awayCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(2), null);
            if (awayCursor.moveToNext()) {
                result.setAway(awayCursor.getString(0));
                result.setIdAway(cursor.getInt(2));
            }
            awayCursor.close();
            result.setScore(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
            result.setNote(cursor.getString(7));
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
        TableRow headTableRow = new TableRow(mListener.getApplicationContext());
        String[] columnNames = new String[]{"", "Domácí", "Hosté", "Výsledek"};
        for (String s : columnNames) {
            TextView textView = newTableTextView(s);
            textView.setTypeface(null, Typeface.ITALIC);
            headTableRow.addView(textView);
        }
        headTableRow.setLayoutParams(tableLayoutParams);
        resultsTableLayout.addView(headTableRow);
        int i = 0;
        for (final Result result : results) {
            final TableRow tableRow = new TableRow(mListener.getApplicationContext());
            tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
            int homeScore, awayScore;
            if (result.getIdHome() == idClub) {
                homeScore = result.getHomeScore();
                awayScore = result.getAwayScore();
            } else {
                awayScore = result.getHomeScore();
                homeScore = result.getAwayScore();
            }
            ImageView imageView = new ImageView(mListener.getApplicationContext());
            if (homeScore > awayScore)
                imageView.setImageResource(R.drawable.icon_win);
            else if (homeScore < awayScore)
                imageView.setImageResource(R.drawable.icon_lose);
            else
                imageView.setImageResource(R.drawable.icon_draw);
            tableRow.setGravity(Gravity.CENTER);
            tableRow.addView(imageView);
            tableRow.addView(newTableTextView(result.getHome().replace("&quot;", "\"")));
            tableRow.addView(newTableTextView(result.getAway().replace("&quot;", "\"")));

            TextView resultTextView = newTableTextView(result.getScore());
            resultTextView.setTypeface(null, Typeface.BOLD);
            tableRow.addView(resultTextView);
            tableRow.setLayoutParams(tableLayoutParams);
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.showPopup(getActivity(), tableRow, result.getNote());
                }
            });
            resultsTableLayout.addView(tableRow);
            i++;
        }
    }

}
