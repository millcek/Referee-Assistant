package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;

import cz.vojacekmilan.refereeassistant.R;


public class ExamEvaluationActivity extends AppCompatActivity {

    private Question[] questions;
    private int wrongAnswersCount;
    Button buttonWrongQuestions;
    Button buttonAllQuestions;
    int minutes;

    public void startBrowsingAllQuestions(View view) {
        Intent intent = new Intent(this, ExamBrowsingActivity.class);
        intent.putExtra(Tests.QUESTIONS, questions);
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
        Intent intent = new Intent(this, ExamBrowsingActivity.class);
        intent.putExtra(Tests.QUESTIONS, wrongQuestions);
        startActivity(intent);
    }

    public void startExam(View view) {
        Intent intent = new Intent(this, ExamActivity.class);
        intent.putExtra(Tests.QUESTIONS_COUNT, questions.length);
        intent.putExtra(Tests.TIME, minutes);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_evaluation);

        buttonAllQuestions = (Button) findViewById(R.id.buttonAllQuestions);
        buttonWrongQuestions = (Button) findViewById(R.id.buttonWrongQuestions);
        TextView textViewErrorsCount = (TextView) findViewById(R.id.text_view_errors_count);

        Bundle b = getIntent().getExtras();
        Parcelable[] parcelableArray = b.getParcelableArray(Tests.QUESTIONS);
        if (parcelableArray != null)
            questions = Arrays.copyOf(parcelableArray, parcelableArray.length, Question[].class);
        minutes = b.getInt(Tests.TIME);

        wrongAnswersCount = 0;
        for (Question q : questions)
            if (!q.isCorrectlyAnswered())
                wrongAnswersCount++;

        float percentage = (((float)100)/questions.length*wrongAnswersCount);
        if (percentage > 0) {
            textViewErrorsCount.setText(String.format("%d (~%s %%)", wrongAnswersCount,
                    new DecimalFormat("##.0").format(percentage)));
            textViewErrorsCount.setTextColor(Color.RED);
        } else {
            textViewErrorsCount.setText(String.format("%d", wrongAnswersCount));
            textViewErrorsCount.setTextColor(getResources().getColor(R.color.right_answer));
            buttonWrongQuestions.setEnabled(false);
        }
    }
}
