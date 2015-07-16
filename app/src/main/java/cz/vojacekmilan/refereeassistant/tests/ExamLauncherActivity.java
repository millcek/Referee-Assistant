package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.NumberPicker;

import cz.vojacekmilan.refereeassistant.R;


public class ExamLauncherActivity extends AppCompatActivity {

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
        questionsCountNumberPicker.setValue(40);

        timeNumberPicker.setMaxValue(50);
        timeNumberPicker.setMinValue(1);
        timeNumberPicker.setValue(20);
    }

    public void startExam(View view) {
        Intent intent = new Intent(this, ExamActivity.class);
        intent.putExtra(Tests.QUESTIONS_COUNT, questionsCountNumberPicker.getValue());
        intent.putExtra(Tests.TIME, timeNumberPicker.getValue());
        startActivity(intent);
        finish();
    }

}
