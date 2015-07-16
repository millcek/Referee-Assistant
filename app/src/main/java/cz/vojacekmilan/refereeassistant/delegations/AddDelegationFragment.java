package cz.vojacekmilan.refereeassistant.delegations;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.vojacekmilan.refereeassistant.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDelegationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class AddDelegationFragment extends Fragment {

    private AutoCompleteTextView textViewName;
    private TextView textViewError;
    private OnFragmentInteractionListener mListener;
    private CheckBox[] checkBoxes;

    public AddDelegationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_delegation, container, false);
        textViewName = (AutoCompleteTextView) view.findViewById(R.id.auto_complete_text_view_name);
        textViewError = (TextView) view.findViewById(R.id.text_view_error);
        Button button = (Button) view.findViewById(R.id.button);
        checkBoxes = new CheckBox[]{
                (CheckBox) view.findViewById(R.id.checkbox_hr),
                (CheckBox) view.findViewById(R.id.checkbox_ar1),
                (CheckBox) view.findViewById(R.id.checkbox_ar2),
                (CheckBox) view.findViewById(R.id.checkbox_r4),
                (CheckBox) view.findViewById(R.id.checkbox_ds),
                (CheckBox) view.findViewById(R.id.checkbox_td)}
        ;
        button.setOnClickListener(new ButtonOnClickListener());
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

    public static Fragment newInstance(int idRegion) {
        return new AddDelegationFragment();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean isCheckedAny() {
        for (CheckBox checkBox : checkBoxes)
            if (checkBox.isChecked())
                return true;
        return false;
    }

    private class ButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            StringBuilder builder = new StringBuilder();
            if (textViewName.getText().toString().trim().isEmpty())
                builder.append("Jméno je povinné.\n");
            if (!isCheckedAny())
                builder.append("Musíte vybrat alespoň 1 pozici.");
            if (builder.toString().isEmpty()) {
                //TODO pridat do db a odkazat na home s aktualizaci
                //INSERT INTO subscribed_delegations (id_regions, name, hr, ar1, ar2, r4, td, ds) VALUES (81, "Vojáček", 1, 1 ,1, 1, 0, 0)
            } else {
                textViewError.setText(builder.toString());
                textViewError.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    public interface OnFragmentInteractionListener {
    }

}
