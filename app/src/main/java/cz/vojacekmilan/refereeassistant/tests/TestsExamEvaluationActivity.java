package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;

import cz.vojacekmilan.refereeassistant.R;


public class TestsExamEvaluationActivity extends ActionBarActivity {

    private Question[] questions;
    private int wrongAnswersCount;
    Button buttonWrongQuestions;
    Button buttonAllQuestions;
    int minutes;

    public void startBrowsingAllQuestions(View view) {
        Intent intent = new Intent(this, TestsExamBrowsingActivity.class);
        intent.putExtra(TestsActivity.QUESTIONS, questions);
        startActivity(intent);
    }

    public void startBrowsingWrongQuestions(View view) {
        Question[] wrongQuestions = new Question[wrongAnswersCount];
        int index = 0;
        for (Question q : questions)
            if (!q.isCorrectlyAnswered()) {
                wrongQuestions[index] = q;
                index++;
            }
        Intent intent = new Intent(this, TestsExamBrowsingActivity.class);
        intent.putExtra(TestsActivity.QUESTIONS, wrongQuestions);
        startActivity(intent);
    }

    public void startExam(View view) {
        Intent intent = new Intent(this, TestsExamActivity.class);
        intent.putExtra(TestsActivity.QUESTIONS_COUNT, questions.length);
        intent.putExtra(TestsActivity.TIME, minutes);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_evaluation);

        buttonAllQuestions = (Button) findViewById(R.id.buttonAllQuestions);
        buttonWrongQuestions = (Button) findViewById(R.id.buttonWrongQuestions);
        TextView textViewErrorsCount = (TextView) findViewById(R.id.textViewErrorsCount);

        Bundle b = getIntent().getExtras();
        Parcelable[] parcelableArray = b.getParcelableArray(TestsActivity.QUESTIONS);
        if (parcelableArray != null)
            questions = Arrays.copyOf(parcelableArray, parcelableArray.length, Question[].class);
        minutes = b.getInt(TestsActivity.TIME);

        wrongAnswersCount = 0;
        for (Question q : questions)
            if (!q.isCorrectlyAnswered())
                wrongAnswersCount++;

        float percentage = 100 / questions.length * wrongAnswersCount;
        if (percentage > 0) {
            textViewErrorsCount.setText(String.format("%d (~%d%%)", wrongAnswersCount, Math.round(percentage)));
            textViewErrorsCount.setTextColor(Color.RED);
        } else {
            textViewErrorsCount.setText(String.format("%d", wrongAnswersCount));
            textViewErrorsCount.setTextColor(getResources().getColor(R.color.right_answer));
            buttonWrongQuestions.setEnabled(false);
        }
    }
}
