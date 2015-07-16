package cz.vojacekmilan.refereeassistant.results;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.Utils;

public class LeagueFragment extends Fragment {

    public static final String ID_LEAGUE = "id";
    private OnFragmentInteractionListener mListener;
    private int idLeague;
    private int round;
    private int lastRound;

    private SwipeRefreshLayout swipeRefreshLayout;
    private TableLayout tableTableLayout;

    private TextView updatedTextView;
    private TextView roundTextView;

    private ClubsAsyncTask clubsAsyncTask;

    private ImageButton buttonNext;
    private ImageButton buttonPrev;

    private ListView listViewResults;
    private List<Result> results;
    private ResultAdapter resultsAdapter;

    private ListView listViewNextMatches;
    private List<NextMatchAdapter.NextMatch> nextMatches;
    private NextMatchAdapter nextMatchesAdapter;

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
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setRefreshing(false);
        clubsAsyncTask = new ClubsAsyncTask();
        tableTableLayout = (TableLayout) view.findViewById(R.id.table_layout_table);
        tableTableLayout.setColumnStretchable(1, true);

        listViewNextMatches = (ListView) view.findViewById(R.id.list_view_next_matches);
        nextMatches = new LinkedList<>();
        nextMatchesAdapter = new NextMatchAdapter(getActivity(), R.layout.result_list_item, nextMatches);
        listViewNextMatches.setAdapter(nextMatchesAdapter);

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

        updatedTextView = (TextView) view.findViewById(R.id.text_view_updated);
        roundTextView = (TextView) view.findViewById(R.id.text_view_round);
        buttonPrev = (ImageButton) view.findViewById(R.id.button_prev_round);
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevRound();
            }
        });
        buttonNext = (ImageButton) view.findViewById(R.id.button_next_round);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextRound();
            }
        });
        loadLeague();
        return view;
    }

    private void setNextButtonEnabled(boolean enabled) {
        buttonNext.setColorFilter(enabled ? getResources().getColor(R.color.accent) :
                getResources().getColor(R.color.accent_light), PorterDuff.Mode.SRC_ATOP);
    }

    private void setPrevButtonEnabled(boolean enabled) {
        buttonPrev.setColorFilter(enabled ? getResources().getColor(R.color.accent) :
                getResources().getColor(R.color.accent_light), PorterDuff.Mode.SRC_ATOP);
    }

    private void nextRound() {
        if (round < lastRound) {
            round++;
            loadResults();
            if (results.size() == 0)
                nextRound();
        }
    }

    private void prevRound() {
        if (round > 1) {
            round--;
            loadResults();
            if (results.size() == 0)
                prevRound();
        }
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

    private void loadClub(Club club) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery(String.format("SELECT _id FROM clubs WHERE name = '%s' AND id_leagues = %d", club.getName(), idLeague));
        int id = -1;
        if (cursor.moveToNext())
            id = cursor.getInt(0);
        cursor.close();
        databaseHelper.close();
        mListener.loadClub(id);
    }

    public interface OnFragmentInteractionListener {

        void loadClub(int id);

        void loadLeague(int id);

        void loadRegion(int id);
    }


    private void loadResults() {
        results.clear();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT round, (SELECT name FROM clubs " +
                "WHERE id_clubs_home=clubs._id), (SELECT name FROM clubs WHERE id_clubs_away=clubs._id), " +
                "home_score, away_score, home_score_half, away_score_half, note FROM results " +
                "WHERE id_leagues = " + idLeague + " AND round = " + round, null);
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
        fillListViewResults();
    }

    private void loadLeague() {
        tableTableLayout.removeAllViews();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT _id, name, winnings, draws, losses, " +
                "scored_goals, received_goals, points_truth FROM clubs WHERE id_leagues = " + idLeague, null);
        if (cursor.getCount() == 0) {
            Toast.makeText(getActivity(), "Soutěž dosud nebyla stažena, stahuje se", Toast.LENGTH_SHORT).show();
            cursor.close();
            databaseHelper.close();
            updateLeague(idLeague);
            return;
        }
        List<Club> clubs = new LinkedList<>();
        int i = 1;
        while (cursor.moveToNext()) {
            clubs.add(new Club(cursor.getInt(0), i, cursor.getString(1), cursor.getInt(2),
                    cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7)));
            i++;
        }
        final int finalId = idLeague;
        cursor = databaseHelper.rawQuery("SELECT strftime('%s',updated), strftime('%s','now') " +
                "FROM leagues WHERE _id = " + idLeague, null);
        if (cursor.moveToNext())
            setUpdate(cursor.getInt(0), cursor.getInt(1));

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateLeague(finalId);
            }
        });
        cursor = databaseHelper.rawQuery("SELECT MAX(round) FROM results WHERE id_leagues = "
                + idLeague, null);
        if (cursor.moveToNext()) {
            lastRound = cursor.getInt(0);
            round = cursor.getInt(0);
        }
        cursor.close();
        databaseHelper.close();
        loadResults();
        loadNextMatches(idLeague);
        makeTable(clubs);
    }

    private void loadNextMatches(int idLeague) {
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT id_clubs_home, id_clubs_away, datetime, field " +
                "FROM next_matches WHERE id_leagues = " + idLeague + " ORDER BY datetime", null);
        nextMatches.clear();
        while (cursor.moveToNext()) {
            NextMatchAdapter.NextMatch nextMatch = new NextMatchAdapter.NextMatch();
            nextMatch.setIdClubsHome(cursor.getInt(0));
            nextMatch.setIdClubsAway(cursor.getInt(1));
            nextMatch.setIdLeagues(idLeague);
            nextMatch.findClubs(databaseHelper.getReadableDatabase());
            nextMatch.setDatetime(new Date(((long) cursor.getInt(2)) * 1000));
            nextMatch.setField(cursor.getString(3));
            nextMatches.add(nextMatch);
        }
        cursor.close();
        databaseHelper.close();
        fillListViewNextMatches();
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
        updatedTextView.setText(String.format("%s %s", getResources().getString(R.string.updated),
                dateFormat.format(updatedDate)));
    }

    private void updateLeague(int id) {
        swipeRefreshLayout.setRefreshing(true);
        clubsAsyncTask = new ClubsAsyncTask();
        clubsAsyncTask.execute(id);
        int time = (int) (System.currentTimeMillis() / 1000);
        setUpdate(time, time);
//        Toast.makeText(getActivity(), "Stala se chyba", Toast.LENGTH_SHORT).show();
    }

    private TextView newTableTextView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        if (text != null)
            textView.setText(text.replace("&quot;", "\""));
        int dp = (int) getResources().getDimension(R.dimen.default_padding_min);
        textView.setPadding(dp, dp, dp, dp);
        textView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
//        textView.setTextColor(Color.BLACK);
        return textView;
    }

    private void fillListViewResults() {
        setNextButtonEnabled(round < lastRound);
        setPrevButtonEnabled(round > 1);
        if (results.size() == 0) {
            swipeRefreshLayout.findViewById(R.id.text_view_no_results).setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        ((TextView) swipeRefreshLayout.findViewById(R.id.text_view_no_results)).setHeight(0);
        roundTextView.setText(results.get(0).getRound() + ". kolo");
        resultsAdapter.notifyDataSetChanged();
        Utils.ListUtils.setDynamicHeight(listViewResults);
    }

    private void fillListViewNextMatches() {
        if (nextMatches.size() == 0) {
            swipeRefreshLayout.findViewById(R.id.text_view_no_next_matches).setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        ((TextView) swipeRefreshLayout.findViewById(R.id.text_view_no_next_matches)).setHeight(0);
        nextMatchesAdapter.notifyDataSetChanged();
        Utils.ListUtils.setDynamicHeight(listViewNextMatches);
    }

    private void makeTable(List<Club> clubs) {
        swipeRefreshLayout.setRefreshing(false);
        tableTableLayout.removeAllViews();
        if (clubs.size() == 0) {
            swipeRefreshLayout.findViewById(R.id.text_view_unavailable_table).setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        ((TextView) swipeRefreshLayout.findViewById(R.id.text_view_unavailable_table)).setHeight(0);
        Collections.sort(clubs, new ClubComparator());
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow headTableRow = new TableRow(getActivity());
        String[] columnNames = new String[]{"#", "Klub", "B", "Z", "V", "R", "P", "S", "(Prav)"};
        for (String s : columnNames) {
            TextView textView = newTableTextView(s);
            textView.setTypeface(null, Typeface.ITALIC);
            headTableRow.addView(textView);
        }
        headTableRow.setLayoutParams(tableLayoutParams);
        tableTableLayout.addView(headTableRow);
        int i = 1;
        for (final Club club : clubs) {
            tableTableLayout.addView(newTableRow(club, i, tableLayoutParams));
            i++;
        }
    }

    private TableRow newTableRow(final Club club, int orderNumber, TableLayout.LayoutParams tableLayoutParams) {
        int[] columnValues = club.getColumnValues();
        TableRow tableRow = new TableRow(getActivity());
//            tableRow.setBackgroundColor(Color.parseColor((orderNumber % 2 == 0) ? "#FFFFFF" : "#F0F0F0"));
        tableRow.addView(newTableTextView(String.valueOf(orderNumber)));
        tableRow.addView(newTableTextView(club.getName().replace("&quot;", "\"")));
        TextView pointsTextView = newTableTextView(String.valueOf(club.getPoints()));
        pointsTextView.setTypeface(null, Typeface.BOLD);
//        pointsTextView.setTextColor(Color.BLACK);
        tableRow.addView(pointsTextView);
        for (int value : columnValues)
            tableRow.addView(newTableTextView(String.valueOf(value)));
        tableRow.addView(newTableTextView(club.getScore()));
        tableRow.addView(newTableTextView(String.valueOf(club.getPointsTruth())));
        tableRow.setLayoutParams(tableLayoutParams);
        tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDestroy();
                if (club.getId() > 0)
                    mListener.loadClub(club.getId());
                else
                    loadClub(club);
            }
        });
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            tableRow.setBackground(getResources().getDrawable(R.drawable.white_clickable));
        return tableRow;
    }

    @Override
    public void onDestroy() {
        if (clubsAsyncTask != null)
            clubsAsyncTask.cancel(true);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.removeAllViews();
        }
        super.onDestroy();
    }

    private void updateResults(List<Result> results) {
        this.results = results;
        fillListViewResults();
    }

    private void updateNextMatches(List<NextMatchAdapter.NextMatch> nextMatches) {
        this.nextMatches = nextMatches;
        fillListViewNextMatches();
    }

    private class ClubsAsyncTask extends AsyncTask<Integer, String, League> {
        private int idLeague;

        @Override
        protected League doInBackground(Integer... params) {
            try {
                idLeague = params[0];
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
                League l = League.getLeague(databaseHelper.getWritableDatabase(), idLeague);
                databaseHelper.close();
                return l;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final League league) {
            try {
                super.onPostExecute(league);
                if (league == null) {
                    Toast.makeText(getActivity(), "Nelze navázat připojení k internetu", Toast.LENGTH_SHORT).show();//TODO osetrit, na telefonu nefunguje
                    return;
                }
                List<Result> results = league.getRoundResults(league.getLastRound());
                if (results.size() > 0) {
                    round = league.getLastRound();
                    lastRound = round;
                    updateResults(results);
                }
                updateNextMatches(league.getNextMatches());
                makeTable(league.getClubs());
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
                league.updateClubsAndResults(databaseHelper.getWritableDatabase(), idLeague);
                databaseHelper.close();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Stala se chyba", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private class ClubComparator implements Comparator<Club> {
        @Override
        public int compare(Club lhs, Club rhs) {
            return lhs.compare(rhs);
        }
    }

    private class NextMatchComparator implements Comparator<NextMatchAdapter.NextMatch> {

        @Override
        public int compare(NextMatchAdapter.NextMatch lhs, NextMatchAdapter.NextMatch rhs) {
            return (lhs.getDatetime().getTime() < rhs.getDatetime().getTime()) ? -1 : ((lhs.getDatetime().getTime() == rhs.getDatetime().getTime()) ? 0 : 1);
        }
    }
}
