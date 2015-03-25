package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.RegionFragment;

public class LeagueFragment extends Fragment {

    public static final String ID_LEAGUE = "id";
    private LeagueFragmentInteractionListener mListener;
    private int idLeague;
    private int round;
    private int lastRound;
    //    private LinearLayout linearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;//TODO pridat pristi zapasy
    private TableLayout tableTableLayout;
    private TableLayout nextMatchesTableLayout;
    private TableLayout resultsTableLayout;
    private TextView updatedTextView;
    private TextView roundTextView;
    private ClubsAsyncTask clubsAsyncTask;

    public static LeagueFragment newInstance(int idLeague) {
        LeagueFragment fragment = new LeagueFragment();
        Bundle args = new Bundle();
        args.putInt(ID_LEAGUE, idLeague);
        fragment.setArguments(args);
        return fragment;
    }

    public LeagueFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            idLeague = getArguments().getInt(ID_LEAGUE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);
        clubsAsyncTask = new ClubsAsyncTask();
        tableTableLayout = (TableLayout) view.findViewById(R.id.tableTableLayout);
        nextMatchesTableLayout = (TableLayout) view.findViewById(R.id.nextMatchesTableLayout);
        resultsTableLayout = (TableLayout) view.findViewById(R.id.resultsTableLayout);
        updatedTextView = (TextView) view.findViewById(R.id.updatedTextView);
        roundTextView = (TextView) view.findViewById(R.id.roundTextView);
        view.findViewById(R.id.nextRoundButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextRound();
            }
        });
        view.findViewById(R.id.prevRoundButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevRound();
            }
        });
        loadLeague();
        return view;
    }

    private void nextRound() {
        if (round < lastRound) {
            round++;
            loadResults();
            if (resultsTableLayout.getChildCount() == 1)
                nextRound();
        }
    }

    private void prevRound() {
        if (round > 1) {
            round--;
            loadResults();
            if (resultsTableLayout.getChildCount() == 1)
                prevRound();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LeagueFragmentInteractionListener) activity;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface LeagueFragmentInteractionListener {
        public Context getApplicationContext();

        void loadClub(int id);

        void loadLeague(int id);

        void loadRegion(int id);
    }

    private void loadResults() {
        if (round < 1) return;
        roundTextView.setText(round + ". kolo");
        resultsTableLayout.removeAllViews();
        DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT round, id_clubs_home, id_clubs_away, home_score, away_score, home_score_half, away_score_half, note FROM results WHERE id_leagues = " + idLeague + " AND round = " + round, null);
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
            result.setNote(cursor.getString(7));//TODO nejak ukazat poznamku
            results.add(result);
        }
        makeResultsTable(results);
        cursor.close();
        db.close();
        databaseHelper.close();
    }

    private void loadLeague() {
        tableTableLayout.removeAllViews();
        DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, name, winnings, draws, losses, scored_goals, received_goals, points_truth FROM clubs WHERE id_leagues = " + idLeague + " ORDER BY (winnings * 3 + draws) DESC, scored_goals DESC", null);
        if (cursor.getCount() == 0) {
            Toast.makeText(mListener.getApplicationContext(), "Soutěž dosud nebyla stažena, stahuje se", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(true);
            clubsAsyncTask = new ClubsAsyncTask();
            clubsAsyncTask.execute(idLeague);
        } else {
            Cursor leagueNameCursor = db.rawQuery("SELECT name FROM leagues WHERE _id = " + idLeague, null);
            if (leagueNameCursor.moveToNext())
                super.getActivity().setTitle(leagueNameCursor.getString(0));
            leagueNameCursor.close();
            List<Club> clubs = new LinkedList<>();
            int i = 1;
            while (cursor.moveToNext()) {
                clubs.add(new Club(cursor.getInt(0), i, cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7)));
                i++;
            }
            cursor.close();
            makeTable(clubs);
            final int finalId = idLeague;
            cursor.close();
            cursor = db.rawQuery("SELECT strftime('%s',updated), strftime('%s','now') FROM leagues WHERE _id = " + idLeague, null);
            if (cursor.moveToNext())
                setUpdate(cursor.getInt(0), cursor.getInt(1));
            cursor.close();

            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateLeague(finalId);
                }
            });
        }
        cursor.close();
        cursor = db.rawQuery("SELECT MAX(round) FROM results WHERE id_leagues = " + idLeague, null);
        if (cursor.moveToNext()) {
            lastRound = cursor.getInt(0);
            round = cursor.getInt(0);
            loadResults();
        }
        cursor.close();
        cursor = db.rawQuery("SELECT id_clubs_home, id_clubs_away, datetime, field FROM next_matches WHERE id_leagues = " + idLeague, null);
        List<NextMatch> nextMatches = new ArrayList<>();
        while (cursor.moveToNext()) {
            NextMatch nextMatch = new NextMatch();
            nextMatch.setIdClubsHome(cursor.getInt(0));
            nextMatch.setIdClubsAway(cursor.getInt(1));
            nextMatch.setIdLeagues(idLeague);
            nextMatch.findClubs(db);
            nextMatch.setDatetime(new Date(((long) cursor.getInt(2)) * 1000));
            nextMatch.setField(cursor.getString(3));
            nextMatches.add(nextMatch);
        }
        cursor.close();
        db.close();
        databaseHelper.close();
        makeNextMatchesTable(nextMatches);
    }

    private void makeNextMatchesTable(List<NextMatch> nextMatches) {
        nextMatchesTableLayout.removeAllViews();
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow headTableRow = new TableRow(mListener.getApplicationContext());
        String[] columnNames = new String[]{"Domácí", "Hosté", "Termín", "Hřiště"};
        for (String s : columnNames) {
            TextView textView = newTableTextView(s);
            textView.setTypeface(null, Typeface.ITALIC);
            headTableRow.addView(textView);
        }
        headTableRow.setLayoutParams(tableLayoutParams);
        nextMatchesTableLayout.addView(headTableRow);

        int i = 0;
        for (NextMatch nextMatch : nextMatches) {
            TableRow tableRow = new TableRow(mListener.getApplicationContext());
            tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
            tableRow.addView(newTableTextView(nextMatch.getClubsHome()));
            tableRow.addView(newTableTextView(nextMatch.getClubsAway()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
            tableRow.addView(newTableTextView(dateFormat.format(nextMatch.getDatetime())));
            tableRow.addView(newTableTextView(nextMatch.getField()));
            nextMatchesTableLayout.addView(tableRow);
            i++;
        }
    }

    private void setUpdate(int dateTime, int nowDateTime) {
        Date updatedDate = new Date((long) dateTime * 1000);
        Calendar updatedCalendar = Calendar.getInstance();
        updatedCalendar.setTime(updatedDate);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date((long) nowDateTime * 1000));

        String updated;
        int year = now.get(Calendar.YEAR) - updatedCalendar.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) - updatedCalendar.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH) - updatedCalendar.get(Calendar.DAY_OF_MONTH);

        if (year == 0) {
            if (month == 0 && day == 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                updated = "v " + dateFormat.format(updatedDate);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm");
                updated = dateFormat.format(updatedDate);
            }
        }else{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            updated = dateFormat.format(updatedDate);
        }

        updatedTextView.setText("Aktualizováno " + updated);
    }

    private void updateLeague(int id) {
        clubsAsyncTask = new ClubsAsyncTask();
        clubsAsyncTask.execute(id);
    }

    private TextView newTableTextView(String text) {
        TextView textView = new TextView(mListener.getApplicationContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setText(text.replace("&quot;", "\""));
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
        String[] columnNames = new String[]{"Domácí", "Hosté", "Výsledek"};
        for (String s : columnNames) {
            TextView textView = newTableTextView(s);
            textView.setTypeface(null, Typeface.ITALIC);
            headTableRow.addView(textView);
        }
        headTableRow.setLayoutParams(tableLayoutParams);
        resultsTableLayout.addView(headTableRow);
        int i = 0;
        for (final Result result : results) {
            TableRow tableRow = new TableRow(mListener.getApplicationContext());
            tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
            TextView homeTextView = newTableTextView(result.getHome().getName().replace("&quot;", "\""));
            TextView awayTextView = newTableTextView(result.getAway().getName().replace("&quot;", "\""));
            if (result.getHomeScore() > result.getAwayScore())
                homeTextView.setTypeface(null, Typeface.BOLD);
            else if (result.getHomeScore() < result.getAwayScore())
                awayTextView.setTypeface(null, Typeface.BOLD);
            tableRow.addView(homeTextView);
            tableRow.addView(awayTextView);
            tableRow.addView(newTableTextView(result.getScore()));
            tableRow.setLayoutParams(tableLayoutParams);
            resultsTableLayout.addView(tableRow);
            i++;
        }
    }

    private void makeTable(List<Club> clubs) {
        tableTableLayout.setColumnStretchable(1, true);
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

        TableRow headTableRow = new TableRow(mListener.getApplicationContext());
        String[] columnNames = new String[]{"#", "Klub", "Z", "V", "R", "P", "S", "B", "(Prav)"};//TODO z nastaveni brat sloupce ktere se maji zobrazit
        for (String s : columnNames) {
            TextView textView = newTableTextView(s);
            textView.setTypeface(null, Typeface.ITALIC);
            headTableRow.addView(textView);
        }
        headTableRow.setLayoutParams(tableLayoutParams);
        tableTableLayout.addView(headTableRow);

        int i = 0;
        for (final Club club : clubs) {
            int[] columnValues = club.getColumnValues();
            TableRow tableRow = new TableRow(mListener.getApplicationContext());
            tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
            tableRow.addView(newTableTextView(String.valueOf(club.getRank())));
            tableRow.addView(newTableTextView(club.getName().replace("&quot;", "\"")));
            for (int value : columnValues)
                tableRow.addView(newTableTextView(String.valueOf(value)));
            tableRow.addView(newTableTextView(club.getScore()));
            TextView pointsTextView = newTableTextView(String.valueOf(club.getPoints()));
            pointsTextView.setTypeface(null, Typeface.BOLD);
            pointsTextView.setTextColor(Color.BLACK);
            tableRow.addView(pointsTextView);
            tableRow.addView(newTableTextView(String.valueOf(club.getPointsTruth())));
            tableRow.setLayoutParams(tableLayoutParams);
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.loadClub(club.getId());
                }
            });
            tableTableLayout.addView(tableRow);
            i++;
        }
    }

    @Override
    public void onDestroy() {
        if (clubsAsyncTask != null)
            clubsAsyncTask.cancel(true);
        super.onDestroy();
    }

    private class ClubsAsyncTask extends AsyncTask<Integer, String, League> {
        private int idLeague;

        @Override
        protected League doInBackground(Integer... params) {
            idLeague = params[0];
            try {
                DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
                databaseHelper.openDataBase();
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor leagueCursor = db.rawQuery("SELECT url FROM leagues WHERE _id = " + idLeague, null);
                String url = null;
                if (leagueCursor.moveToNext())
                    url = leagueCursor.getString(0);
                leagueCursor.close();
                db.close();
                databaseHelper.close();
                return League.getLeague(url);
            } catch (Exception e) {
                Log.w("ClubsAsyncTask", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(League league) {
            super.onPostExecute(league);
            swipeRefreshLayout.setRefreshing(false);
            if (league == null) {
                Toast.makeText(mListener.getApplicationContext(), "Nelze navázat připojení k internetu", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
            databaseHelper.openDataBase();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            league.updateClubsAndResults(db, idLeague);
            loadLeague();
            db.close();
            databaseHelper.close();
        }
    }

}
