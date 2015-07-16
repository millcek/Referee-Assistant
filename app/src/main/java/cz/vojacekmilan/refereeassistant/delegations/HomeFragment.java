package cz.vojacekmilan.refereeassistant.delegations;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;

public class HomeFragment extends Fragment {
    OnFragmentInteractionListener mListener;
    ListView listView;
    List<DelegationAdapter.DelegationItem> delegations;
    DelegationAdapter delegationAdapter;
    TextView textViewNoDelegation;
    TextView textViewUpdated;
    FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        delegations = new LinkedList<>();
        delegationAdapter = new DelegationAdapter(getActivity(), R.layout.delegation_list_item, delegations);
        listView.setAdapter(delegationAdapter);
        textViewNoDelegation = (TextView) view.findViewById(R.id.text_view_no_delegation);
        textViewUpdated = (TextView) view.findViewById(R.id.text_view_updated);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewNoDelegation.setPadding(0, 0, 0, 0);
                mListener.editDelegations();
            }
        });
        fillListView();
        listView.setOnItemClickListener(new OnItemClickListener());
        return view;
    }

    private void fillListView() {
        delegations.clear();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM. HH:mm");
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT id_regions, name, hr, ar1, ar2, r4, td, ds, updated  FROM subscribed_delegations");
        while (cursor.moveToNext()) {
            String sql = getSqlQuery(cursor.getInt(0), cursor.getString(1), cursor.getInt(2) != 0, cursor.getInt(3) != 0, cursor.getInt(4) != 0, cursor.getInt(5) != 0, cursor.getInt(6) != 0, cursor.getInt(7) != 0);
            if (sql != null) {
                Cursor cursor1 = databaseHelper.rawQuery(sql);
                while (cursor1.moveToNext())
                    delegations.add(new DelegationAdapter.DelegationItem(cursor1.getString(10), cursor1.getString(0),
                            cursor1.getString(1), format.format(new Date(((long) cursor1.getInt(2)) * 1000)),
                            cursor1.getString(3), cursor1.getString(4), cursor1.getString(5),
                            cursor1.getString(6), cursor1.getString(7), cursor1.getString(8), cursor1.getString(9), cursor1.getInt(11)));
                cursor1.close();
            }
            textViewUpdated.setText(String.format("%s %s", getResources().getString(R.string.updated), format.format(new Date(1000 * ((long) cursor.getInt(8))))));
        }
        cursor.close();
        databaseHelper.close();
        textViewNoDelegation.setLayoutParams(delegations.size() == 0 ? new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT) : new LinearLayout.LayoutParams(0, 0));
        delegationAdapter.notifyDataSetChanged();
    }

    private String getSqlQuery(int idRegions, String name, boolean hr, boolean ar1, boolean ar2, boolean r4, boolean td, boolean ds) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(
                "SELECT (SELECT name FROM clubs WHERE _id = id_clubs_home), " +
                        "(SELECT name FROM clubs WHERE _id = id_clubs_away), " +
                        "datetime, hr, ar1, ar2, r4, ds, td, field, " +
                        "(SELECT name FROM leagues WHERE _id = id_leagues), id_leagues FROM delegations " +
                        "WHERE id_leagues = (SELECT _id FROM leagues WHERE id_regions = %d) AND (",
                idRegions));
        boolean prev = false;
        if (hr) {
            stringBuilder.append("hr = \"").append(name).append("\"");
            prev = true;
        }
        if (ar1) {
            stringBuilder.append(prev ? " OR " : "").append("ar1 = \"").append(name).append("\"");
            prev = true;
        }
        if (ar2) {
            stringBuilder.append(prev ? " OR " : "").append("ar2 = \"").append(name).append("\"");
            prev = true;
        }
        if (r4) {
            stringBuilder.append(prev ? " OR " : "").append("r4 = \"").append(name).append("\"");
            prev = true;
        }
        if (td) {
            stringBuilder.append(prev ? " OR " : "").append("td = \"").append(name).append("\"");
            prev = true;
        }
        if (ds)
            stringBuilder.append(prev ? " OR " : "").append("ds = \"").append(name).append("\"");
        stringBuilder.append(") AND datetime > strftime('%s','now')");
        return prev ? stringBuilder.toString() : null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (Exception e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static Fragment newInstance() {
        return new HomeFragment();
    }

    private class OnItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mListener.loadLeague(delegations.get(position).getIdLeague());
        }
    }

    public interface OnFragmentInteractionListener {
        void editDelegations();
        void loadLeague(int id);
    }
}
