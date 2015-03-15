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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseFillerAsyncTask;
import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.RegionFragment;

import static cz.vojacekmilan.refereeassistant.Utils.getCleanTagNodes;
import static cz.vojacekmilan.refereeassistant.Utils.getSerializedHtml;

public class LeagueFragment extends Fragment {

    public static final String ID_LEAGUE = "id";
    private LeagueFragmentInteractionListener mListener;
    private int idLeague;
    private LinearLayout linearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
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
        if (getArguments() != null) {
            idLeague = getArguments().getInt(ID_LEAGUE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_league, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setEnabled(false);
        clubsAsyncTask = new ClubsAsyncTask();
        linearLayout = (LinearLayout) view.findViewById(R.id.layout);
        loadLeague(idLeague);
        return view;
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

        void loadLeague(int id);

        void loadRegion(int id);
    }

    private void loadLeague(int id) {
        Log.i("milda", "load " + id);
        DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT _id, rank, name, winnings, draws, losses, scored_goals, received_goals, points_truth FROM clubs WHERE id_leagues = " + id + " ORDER BY rank", null);
        if (cursor.getCount() == 0) {
            Toast.makeText(mListener.getApplicationContext(), "Soutěž dosud nebyla stažena, stahuje se", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(true);
            clubsAsyncTask = new ClubsAsyncTask();
            clubsAsyncTask.execute(id);
        } else {
            Cursor leagueNameCursor = db.rawQuery("SELECT name FROM leagues WHERE _id = " + id, null);
            if (leagueNameCursor.moveToNext())
                super.getActivity().setTitle(leagueNameCursor.getString(0));
            leagueNameCursor.close();

            List<Club> clubs = new LinkedList<>();
            while (cursor.moveToNext())
                clubs.add(new Club(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8)));

            linearLayout.removeAllViews();

            cursor.close();
            final int finalId = id;

            cursor = db.rawQuery("SELECT id_regions, strftime('%s',updated), strftime('%s','now') FROM leagues WHERE _id = " + id, null);
            if (cursor.moveToNext()) {
                Date updatedDate = new Date((long) cursor.getInt(1) * 1000);
                Calendar updatedCalendar = Calendar.getInstance();
                updatedCalendar.setTime(updatedDate);

                Calendar now = Calendar.getInstance();
                now.setTime(new Date((long) cursor.getInt(2) * 1000));

                String updated;
                int[] date = {
                        now.get(Calendar.YEAR) - updatedCalendar.get(Calendar.YEAR),
                        now.get(Calendar.MONTH) - updatedCalendar.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH) - updatedCalendar.get(Calendar.DAY_OF_MONTH),
                        now.get(Calendar.HOUR) - updatedCalendar.get(Calendar.HOUR),
                        now.get(Calendar.MINUTE) - updatedCalendar.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND) - updatedCalendar.get(Calendar.SECOND)
                };
                char[] dateFormat = {'r', 'm', 'd', 'h', 'm', 's'};

                StringBuilder updatedStringBuilder = new StringBuilder();
                boolean toWrite = false;
                for (int i = 0; i < date.length; i++) {
                    if (date[i] != 0 && !toWrite)
                        toWrite = true;
                    if (toWrite)
                        updatedStringBuilder.append(Math.abs(date[i])).append(dateFormat[i]).append(" ");
                }
                updated = updatedStringBuilder.toString().trim();

                TextView updatedTextView = new TextView(mListener.getApplicationContext());
                updatedTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                updatedTextView.setTextAppearance(mListener.getApplicationContext(), android.R.style.TextAppearance_Small);
                updatedTextView.setTextColor(Color.BLACK);
                updatedTextView.setText("Aktualizováno " + (updated.length() == 0 ? "nyní" : "před " + updated));
                linearLayout.addView(updatedTextView);

                makeTable(clubs);

                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setEnabled(true);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateLeague(finalId);
                    }
                });
            }
        }

        cursor.close();
        db.close();
        databaseHelper.close();
    }

    private void updateLeague(int id) {
        Log.i("milda", "update " + id);
        clubsAsyncTask = new ClubsAsyncTask();
        clubsAsyncTask.execute(id);
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

    private void makeTable(List<Club> clubs) {
        TextView titleTextView = new TextView(mListener.getApplicationContext());
        titleTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleTextView.setTextAppearance(mListener.getApplicationContext(), android.R.style.TextAppearance_Medium);
        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setText("Výsledky");
        linearLayout.addView(titleTextView);
        TableLayout tableTableLayout = new TableLayout(mListener.getApplicationContext());
        tableTableLayout.setColumnStretchable(1, true);
        TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        //region TableRow header
        TableRow headTableRow = new TableRow(mListener.getApplicationContext());
        String[] columnNames = new String[]{"#", "Klub", "Z", "V", "R", "P", "S", "B", "(Prav)"};
        for (String s : columnNames)
            headTableRow.addView(newTableTextView(s));
        headTableRow.setLayoutParams(tableLayoutParams);
        tableTableLayout.setLayoutParams(tableLayoutParams);
        tableTableLayout.addView(headTableRow);
        //endregion
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
                    Toast.makeText(mListener.getApplicationContext(), "Kliknul jsi na klub #" + club.getId(), Toast.LENGTH_SHORT).show();//TODO ukazat vsechny vysledky klubu - v novem fragmentu
                }
            });
            tableTableLayout.addView(tableRow);
            i++;
        }
        linearLayout.addView(tableTableLayout);
    }

    @Override
    public void onDestroy() {
        if (clubsAsyncTask != null)
            clubsAsyncTask.cancel(true);
        super.onDestroy();
    }

    private class ClubsAsyncTask extends AsyncTask<Integer, String, List<Club>> {
        private final String DEFAULT_FORMAT = "%rank %name %empty %wins %draws %losses %score %empty %empty %pointstruth";
        private int idLeague;

        @Override
        protected List<Club> doInBackground(Integer... params) {
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
                return getClubs(url);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Club> clubs) {
            super.onPostExecute(clubs);
            swipeRefreshLayout.setRefreshing(false);
            if (clubs == null) {
                Toast.makeText(mListener.getApplicationContext(), "Nelze navázat připojení k internetu", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseHelper databaseHelper = new DatabaseHelper(mListener.getApplicationContext(), RegionFragment.DB_NAME);
            databaseHelper.openDataBase();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            if (clubs.size() == 0) {
                Toast.makeText(mListener.getApplicationContext(), "Vyskytl se problém, soutěž není dostupná", Toast.LENGTH_SHORT).show();
                Cursor cursor = db.rawQuery("SELECT id_regions FROM leagues WHERE _id = " + idLeague, null);
                int regionId = 0;
                if (cursor.moveToNext())
                    regionId = cursor.getInt(0);
                cursor.close();
                db.execSQL("DELETE FROM leagues WHERE _id = " + idLeague);
                mListener.loadRegion(regionId);
            } else {
                db.execSQL("UPDATE leagues SET updated = datetime('now') WHERE _id = " + idLeague);
                db.execSQL("DELETE FROM clubs WHERE id_leagues = " + idLeague);
                for (Club club : clubs)
                    db.execSQL(club.getSqlInsert(idLeague));
                mListener.loadLeague(idLeague);
            }
            db.close();
            databaseHelper.close();
        }

        private List<Club> getClubs(String url) throws MalformedURLException, XPatherException {
            return getClubs(url, DEFAULT_FORMAT);
        }

        private List<Club> getClubs(String url, String format) throws MalformedURLException, XPatherException {
            List<Club> clubs = new LinkedList<>();
            TagNode root = getCleanTagNodes(new URL(url), DatabaseFillerAsyncTask.CHARSET);
            root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[2]", "utf-8"));
            String htmlTable = getSerializedHtml(root, "//tr[@bgcolor='#f8f8f8']") + "\n" + getSerializedHtml(root, "//tr[@bgcolor='#ffffff']");
            while (htmlTable.length() > 0) {
                if (!htmlTable.contains("</tr>"))
                    break;
                String row = htmlTable.substring(0, htmlTable.indexOf("</tr>"));
                htmlTable = htmlTable.substring(htmlTable.indexOf("</tr>") + 5);
                String rowFormat = format;
                Club newClub = new Club();
                while (rowFormat.contains("%") && row.contains("</td>")) {
                    rowFormat = rowFormat.substring(rowFormat.indexOf("%") + 1);
                    String actual;
                    String node = row.substring(0, row.indexOf("</td>"));
                    node = node.replaceAll("<.*?>", "").replaceAll("\n", " ").trim();
                    row = row.substring(row.indexOf("</td>") + 5);
                    if (rowFormat.contains("%"))
                        actual = rowFormat.substring(0, rowFormat.indexOf("%")).trim().toLowerCase();
                    else
                        actual = rowFormat.trim().toLowerCase();
                    switch (actual) {
                        case "rank":
                            newClub.setRank(Integer.valueOf(node.replace(".", "").trim()));
                            break;
                        case "name":
                            newClub.setName(node);
                            break;
                        case "wins":
                            newClub.setWinnings(Integer.valueOf(node));
                            break;
                        case "draws":
                            newClub.setDraws(Integer.valueOf(node));
                            break;
                        case "losses":
                            newClub.setLosses(Integer.valueOf(node));
                            break;
                        case "score":
                            if (node.contains(":")) {
                                newClub.setScoredGoals(Integer.valueOf(node.substring(0, node.indexOf(":")).trim()));
                                newClub.setReceivedGoals(Integer.valueOf(node.substring(1 + node.indexOf(":")).trim()));
                            }
                            break;
                        case "pointstruth":
                            newClub.setPointsTruth(Integer.valueOf(node.replace("(", "").replace(")", "").trim()));
                            break;
                    }
                }
                clubs.add(newClub);
            }
            return clubs;
        }
    }

}
