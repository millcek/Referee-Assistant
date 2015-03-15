package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.NumberPicker;

import cz.vojacekmilan.refereeassistant.R;


public class TestsExamLauncherActivity extends ActionBarActivity {

    NumberPicker questionsCountNumberPicker;
    NumberPicker timeNumberPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_launcher);

        questionsCountNumberPicker = (NumberPicker) findViewById(R.id.questionsCountNumberPicker);
        timeNumberPicker = (NumberPicker) findViewById(R.id.timeNumberPicker);

        questionsCountNumberPicker.setMaxValue(50);
        questionsCountNumberPicker.setMinValue(5);
        questionsCountNumberPicker.setValue(5);

        timeNumberPicker.setMaxValue(50);
        timeNumberPicker.setMinValue(1);
        timeNumberPicker.setValue(1);
    }

    public void startExam(View view){
        Intent intent = new Intent(this, TestsExamActivity.class);
        intent.putExtra(TestsFragment.QUESTIONS_COUNT, questionsCountNumberPicker.getValue());
        intent.putExtra(TestsFragment.TIME, timeNumberPicker.getValue());
        startActivity(intent);
        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_tests_exam_launcher, menu);
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
