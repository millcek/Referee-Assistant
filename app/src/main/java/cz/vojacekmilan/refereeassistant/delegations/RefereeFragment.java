package cz.vojacekmilan.refereeassistant.delegations;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;
import cz.vojacekmilan.refereeassistant.results.RegionFragment;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class RefereeFragment extends Fragment implements ListView.OnItemClickListener, FloatingActionButton.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private AbsListView mListView;
    private ListAdapter mAdapter;
    private List<RefereeAdapter.RefereeItem> refereeItems;

    public static RefereeFragment newInstance() {
        return new RefereeFragment();
    }

    public RefereeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_referee, container, false);
        refereeItems = new LinkedList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), RegionFragment.DB_NAME);
        Cursor cursor = databaseHelper.rawQuery("SELECT _id, name, (SELECT name FROM regions WHERE regions._id = subscribed_delegations.id_regions) FROM subscribed_delegations");
        while (cursor.moveToNext())
            refereeItems.add(new RefereeAdapter.RefereeItem(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
        cursor.close();
        databaseHelper.close();
        mListView = (AbsListView) view.findViewById(R.id.list_view);
        mAdapter = new RefereeAdapter(getActivity(),
                R.layout.referee_list_item, refereeItems);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        view.findViewById(R.id.floating_action_button).setOnClickListener(this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener)
            mListener.editReferee(refereeItems.get(position).getId());
    }

    @Override
    public void onClick(View v) {
        mListener.selectRegionForDelegation();
    }

    public interface OnFragmentInteractionListener {
        void selectRegionForDelegation();

        void editReferee(int id);
    }

}
