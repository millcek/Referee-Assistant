package cz.upol.vojami04.refereeassistant.tests;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import cz.upol.vojami04.refereeassistant.R;


public class TestsExamLauncherActivity extends ActionBarActivity {

    NumberPicker numberPickerQuestionsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_launcher);

        numberPickerQuestionsCount = (NumberPicker) findViewById(R.id.numberPickerQuestionsCount);
        numberPickerQuestionsCount.setMaxValue(50);
        numberPickerQuestionsCount.setMinValue(5);
        numberPickerQuestionsCount.setValue(40);
    }

    public void startExam(View view){
        Intent intent = new Intent(this, TestsExamActivity.class);
        intent.putExtra(TestsActivity.QUESTIONS_COUNT, numberPickerQuestionsCount.getValue());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tests_exam_launcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
