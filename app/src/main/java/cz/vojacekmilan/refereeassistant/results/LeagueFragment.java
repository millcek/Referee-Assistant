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
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.Utils;

public class LeagueFragment extends Fragment {

    public static final String ID_LEAGUE = "id";
    private LeagueFragmentInteractionListener mListener;
    private int idLeague;
    private int round;
    private int lastRound;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TableLayout tableTableLayout;
    private TableLayout nextMatchesTableLayout;
    private TableLayout resultsTableLayout;
    private TextView updatedTextView;
    private TextView roundTextView;
    private ClubsAsyncTask clubsAsyncTask;
    private ImageButton nextRoundButton;
    private ImageButton prevRoundButton;
    private boolean isRefreshing;
    private SQLiteDatabase db;
    private DatabaseHelper databaseHelper;
    public CountDownLatch insertedInDbLatch;

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

    private void openDb() {
        databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        db = databaseHelper.getReadableDatabase();
    }

    private void closeDb() {
        db.close();//TODO nekde se nezavira db
        databaseHelper.close();
    }

    private void setNextButtonEnabled(boolean enabled) {
        nextRoundButton.setImageResource(enabled ? R.drawable.arrow_next : R.drawable.arrow_next_off);
    }

    private void setPrevButtonEnabled(boolean enabled) {
        prevRoundButton.setImageResource(enabled ? R.drawable.arrow_prev : R.drawable.arrow_prev_off);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        clubsAsyncTask = new ClubsAsyncTask();
        tableTableLayout = (TableLayout) view.findViewById(R.id.tableTableLayout);
        tableTableLayout.setColumnStretchable(1, true);
        nextMatchesTableLayout = (TableLayout) view.findViewById(R.id.nextMatchesTableLayout);
        resultsTableLayout = (TableLayout) view.findViewById(R.id.resultsTableLayout);
        resultsTableLayout.setColumnStretchable(1, true);
        updatedTextView = (TextView) view.findViewById(R.id.updatedTextView);
        roundTextView = (TextView) view.findViewById(R.id.roundTextView);
        prevRoundButton = (ImageButton) view.findViewById(R.id.prevRoundButton);
        prevRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevRound();
            }
        });
        nextRoundButton = (ImageButton) view.findViewById(R.id.nextRoundButton);
        nextRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextRound();
            }
        });
        insertedInDbLatch = new CountDownLatch(0);
        loadLeague();
        return view;
    }

    private void nextRound() {
        try {
            insertedInDbLatch.await();
            if (round < lastRound) {
                round++;
                loadResults();
                if (resultsTableLayout.getChildCount() == 1)
                    nextRound();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void prevRound() {
        try {
            insertedInDbLatch.await();
            if (round > 1) {
                round--;
                loadResults();
                if (resultsTableLayout.getChildCount() == 1)
                    prevRound();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private void loadClub(Club club) {
        try {
            insertedInDbLatch.await();
            openDb();
            Cursor cursor = db.rawQuery(String.format("SELECT _id FROM clubs WHERE name = '%s' AND id_leagues = %d", club.getName(), idLeague), null);
            int id = -1;
            if (cursor != null && cursor.moveToNext())
                id = cursor.getInt(0);
            cursor.close();
            closeDb();
            mListener.loadClub(id);
        } catch (InterruptedException e) {
            closeDb();
            e.printStackTrace();
        }
    }

    public interface LeagueFragmentInteractionListener {
        public Context getApplicationContext();

        void loadClub(int id);

        void loadLeague(int id);

        void loadRegion(int id);
    }

    private void loadResults() {
        resultsTableLayout.removeAllViews();
        openDb();
        Cursor cursor = db.rawQuery("SELECT round, id_clubs_home, id_clubs_away, home_score, away_score, home_score_half, away_score_half, note FROM results WHERE id_leagues = " + idLeague + " AND round = " + round, null);
        List<Result> results = new LinkedList<>();
        while (cursor.moveToNext()) {
            Result result = new Result();
            result.setRound(cursor.getInt(0));
            Cursor homeCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(1), null);
            if (homeCursor.moveToNext())
                result.setHome(homeCursor.getString(0));
            homeCursor.close();
            Cursor awayCursor = db.rawQuery("SELECT name FROM clubs WHERE _id = " + cursor.getInt(2), null);
            if (awayCursor.moveToNext())
                result.setAway(awayCursor.getString(0));
            awayCursor.close();
            result.setScore(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6));
            result.setNote(cursor.getString(7));
            results.add(result);
        }
        cursor.close();
        closeDb();
        makeResultsTable(results);
    }

    private void loadLeague() {
        tableTableLayout.removeAllViews();
        openDb();
        Cursor cursor = db.rawQuery("SELECT _id, name, winnings, draws, losses, scored_goals, received_goals, points_truth FROM clubs WHERE id_leagues = " + idLeague, null);
        if (cursor.getCount() == 0) {
            Toast.makeText(mListener.getApplicationContext(), "Soutěž dosud nebyla stažena, stahuje se", Toast.LENGTH_SHORT).show();
            cursor.close();
            closeDb();
            updateLeague(idLeague);
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
            makeTable(clubs);
            final int finalId = idLeague;
            cursor = db.rawQuery("SELECT strftime('%s',updated), strftime('%s','now') FROM leagues WHERE _id = " + idLeague, null);
            if (cursor.moveToNext())
                setUpdate(cursor.getInt(0), cursor.getInt(1));

            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateLeague(finalId);
                }
            });
            cursor = db.rawQuery("SELECT MAX(round) FROM results WHERE id_leagues = " + idLeague, null);
            if (cursor.moveToNext()) {
                lastRound = cursor.getInt(0);
                round = cursor.getInt(0);
            }
            cursor.close();
            closeDb();
            loadResults();
            loadNextMatches(idLeague);
        }
    }

    private void loadNextMatches(int idLeague) {
        openDb();
        Cursor cursor = db.rawQuery("SELECT id_clubs_home, id_clubs_away, datetime, field FROM next_matches WHERE id_leagues = " + idLeague + " ORDER BY datetime", null);
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
        closeDb();
        makeNextMatchesTable(nextMatches);
    }

    private void makeNextMatchesTable(List<NextMatch> nextMatches) {
        try {
            nextMatchesTableLayout.removeAllViews();
            if (nextMatches.size() == 0) {
                nextMatchesTableLayout.addView(newTextTableRow("Příští zápasy nejsou k dispozici"));
                return;
            }
            Collections.sort(nextMatches, new NextMatchComparator());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpdate(int dateTime, int nowDateTime) {
        Date updatedDate = new Date((long) dateTime * 1000);
        Calendar updatedCalendar = Calendar.getInstance();
        updatedCalendar.setTime(updatedDate);

        Calendar now = Calendar.getInstance();
        now.setTime(new Date((long) nowDateTime * 1000));

        int year = now.get(Calendar.YEAR) - updatedCalendar.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) - updatedCalendar.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH) - updatedCalendar.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormat = null;
        if (year == 0)
            dateFormat = (month == 0 && day == 0) ? new SimpleDateFormat("HH:mm") : new SimpleDateFormat("dd.MM. HH:mm");
        else
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        updatedTextView.setText("Aktualizováno " + dateFormat.format(updatedDate));
    }

    private void updateLeague(int id) {
        try {
            isRefreshing = true;
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(isRefreshing);
                }
            });
            clubsAsyncTask = new ClubsAsyncTask();
            clubsAsyncTask.execute(id);
            int time = (int) (System.currentTimeMillis() / 1000);
            setUpdate(time, time);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mListener.getApplicationContext(), "Stala se chyba", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView newTableTextView(String text) {
        TextView textView = new TextView(mListener.getApplicationContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        if (text != null)
            textView.setText(text.replace("&quot;", "\""));
        textView.setPadding(10, 10, 10, 10);
        textView.setTextAppearance(mListener.getApplicationContext(), android.R.style.TextAppearance_Small);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    private void makeResultsTable(final List<Result> results) {
        try {
            setNextButtonEnabled(round < lastRound);
            setPrevButtonEnabled(round > 1);
            resultsTableLayout.removeAllViews();
            if (results.size() == 0) {
                resultsTableLayout.addView(newTextTableRow("Výsledky nejsou k dispozici"));
                return;
            }
            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            roundTextView.setText(results.get(0).getRound() + ". kolo");
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
                final TableRow tableRow = new TableRow(mListener.getApplicationContext());
                tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
                TextView homeTextView = newTableTextView((result.getHome() != null) ? result.getHome().replace("&quot;", "\"") : "");
                TextView awayTextView = newTableTextView((result.getAway() != null) ? result.getAway().replace("&quot;", "\"") : "");
                if (result.getHomeScore() > result.getAwayScore())
                    homeTextView.setTypeface(null, Typeface.BOLD);
                else if (result.getHomeScore() < result.getAwayScore())
                    awayTextView.setTypeface(null, Typeface.BOLD);
                tableRow.addView(homeTextView);
                tableRow.addView(awayTextView);
                tableRow.addView(newTableTextView(result.getScore()));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TableRow newTextTableRow(String text) {
        TableRow tableRow = new TableRow(mListener.getApplicationContext());
        tableRow.addView(newTableTextView(text));
        return tableRow;
    }

    private void makeTable(List<Club> clubs) {
        try {
            tableTableLayout.removeAllViews();
            if (clubs.size() == 0) {
                tableTableLayout.addView(newTextTableRow("Tabulka není k dispozici"));
                return;
            }
            Collections.sort(clubs, new ClubComparator());
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

            int i = 1;
            for (final Club club : clubs) {
                int[] columnValues = club.getColumnValues();
                TableRow tableRow = new TableRow(mListener.getApplicationContext());
                tableRow.setBackgroundColor(Color.parseColor((i % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
                tableRow.addView(newTableTextView(String.valueOf(i)));
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
                        if (club.getId() > 0)
                            mListener.loadClub(club.getId());
                        else
                            loadClub(club);
                    }
                });
                tableTableLayout.addView(tableRow);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (clubsAsyncTask != null && !clubsAsyncTask.isDoInBackgroundComplete())
            clubsAsyncTask.cancel(true);
        super.onDestroy();
    }

    private class ClubsAsyncTask extends AsyncTask<Integer, String, League> {
        private int idLeague;
        private boolean doInBackgroundComplete;

        @Override
        protected League doInBackground(Integer... params) {
            try {
                this.doInBackgroundComplete = false;
                idLeague = params[0];
                openDb();
                Log.i("LeagueFragment start", new SimpleDateFormat("mm:ss:SSS").format(new Date(System.currentTimeMillis())));
                League l = League.getLeague(db, idLeague);
                closeDb();
                return l;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final League league) {
            try {
                super.onPostExecute(league);
                this.doInBackgroundComplete = true;
                swipeRefreshLayout.setRefreshing(false);
                if (league == null) {
                    Toast.makeText(mListener.getApplicationContext(), "Nelze navázat připojení k internetu", Toast.LENGTH_SHORT).show();//TODO osetrit, na telefonu nefunguje
                    return;
                }
                List<Result> results = league.getRoundResults(league.getLastRound());
                Log.i("result.size()", String.valueOf(results.size()));
                if (results.size() > 0) {
                    round = league.getLastRound();
                    lastRound = round;
                    makeResultsTable(results);
                }
                makeNextMatchesTable(league.getNextMatches());
                makeTable(league.getClubs());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        insertedInDbLatch = new CountDownLatch(1);
                        openDb();
                        league.updateClubsAndResults(db, idLeague);
                        closeDb();
                        insertedInDbLatch.countDown();
                    }
                }).start();
                isRefreshing = false;
                swipeRefreshLayout.setRefreshing(false);
            } catch (Exception e) {
                Toast.makeText(mListener.getApplicationContext(), "Stala se chyba", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        public boolean isDoInBackgroundComplete() {
            return doInBackgroundComplete;
        }
    }

    private class ClubComparator implements Comparator<Club> {
        @Override
        public int compare(Club lhs, Club rhs) {
            return lhs.compare(rhs);
        }
    }

    private class NextMatchComparator implements Comparator<NextMatch> {

        @Override
        public int compare(NextMatch lhs, NextMatch rhs) {
            return (lhs.getDatetime().getTime() < rhs.getDatetime().getTime()) ? -1 : ((lhs.getDatetime().getTime() == rhs.getDatetime().getTime()) ? 0 : 1);
        }
    }
}
