package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cz.vojacekmilan.refereeassistant.NavigationDrawerFragment;
import cz.vojacekmilan.refereeassistant.R;


public class TestsFragment extends Fragment {

    public static final String DB_NAME = "tests";
    public static final String QUESTIONS = "questions";
    public static final String QUESTIONS_COUNT = "questions_count";
    public static final String TIME = "time";
    public static final int DB_QUESTIONS_COUNT = 1200;

    public static TestsFragment newInstance() {
        return new TestsFragment();
    }

    public TestsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tests, container, false);
        Button buttonTestsBrowsing = (Button) view.findViewById(R.id.buttonTestsBrowsing);
        Button buttonTestsExam = (Button) view.findViewById(R.id.buttonTestsExam);
        buttonTestsBrowsing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTestsBrowsing();
            }
        });
        buttonTestsExam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTestsExam();
            }
        });
        return view;
    }

    public void startTestsExam() {
        Intent intent = new Intent(getActivity(), TestsExamLauncherActivity.class);
        startActivity(intent);
    }

    public void startTestsBrowsing() {
        Intent intent = new Intent(getActivity(), TestsPracticeActivity.class);
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_tests, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
