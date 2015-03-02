package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import cz.vojacekmilan.refereeassistant.R;


public class TestsActivity extends ActionBarActivity {

    public static final String QUESTIONS = "questions";
    public static final String QUESTIONS_COUNT = "questions_count";
    public static final String TIME = "time";
    public static final int DB_QUESTIONS_COUNT = 1200;
    private SQLiteDatabase testsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);
    }

    public void startTestsExam(View view) {
        Intent intent = new Intent(this, TestsExamLauncherActivity.class);
        startActivity(intent);
    }

    public void startTestsBrowsing(View view) {
        Intent intent = new Intent(this, TestsPracticeActivity.class);
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
