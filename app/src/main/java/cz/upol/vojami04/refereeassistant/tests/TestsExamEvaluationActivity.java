package cz.upol.vojami04.refereeassistant.tests;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cz.upol.vojami04.refereeassistant.R;


public class TestsExamEvaluationActivity extends ActionBarActivity {

    private String[] questions;
    private String[][] answers;
    private int[] rightAnswers;
    private int[] userAnswers;
    private int wrongAnswersCount;
    Button buttonWrongQuestions;
    Button buttonAllQuestions;
    int minutes;

    public void startBrowsingAllQuestions(View view) {
        Intent intent = new Intent(this, TestsExamBrowsingActivity.class);
        TwoDSerializable s = TwoDSerializable.getSingletonObject();
        s.setArray(answers);
        intent.putExtra(TestsActivity.QUESTIONS, questions);
        intent.putExtra(TestsActivity.USER_ANSWERS, userAnswers);
        intent.putExtra(TestsActivity.RIGHT_ANSWERS, rightAnswers);
        startActivity(intent);
    }

    public void startBrowsingWrongQuestions(View view) {
        String[] wrongQuestions = new String[wrongAnswersCount];
        String[][] wrongAnswers = new String[wrongAnswersCount][];
        int[] wrongUserAnswers = new int[wrongAnswersCount];
        int[] wrongRightAnswers = new int[wrongAnswersCount];
        int index = 0;
        for (int i = 0; i < questions.length; i++)
            if (userAnswers[i] != rightAnswers[i]) {
                wrongQuestions[index] = questions[i];
                wrongAnswers[index] = answers[i];
                wrongUserAnswers[index] = userAnswers[i];
                wrongRightAnswers[index] = rightAnswers[i];
                index++;
            }

        Intent intent = new Intent(this, TestsExamBrowsingActivity.class);
        TwoDSerializable s = TwoDSerializable.getSingletonObject();
        s.setArray(wrongAnswers);
        intent.putExtra(TestsActivity.QUESTIONS, wrongQuestions);
        intent.putExtra(TestsActivity.USER_ANSWERS, wrongUserAnswers);
        intent.putExtra(TestsActivity.RIGHT_ANSWERS, wrongRightAnswers);
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
        TwoDSerializable s = TwoDSerializable.getSingletonObject();
        answers = s.getArray();
        questions = b.getStringArray(TestsActivity.QUESTIONS);
        rightAnswers = b.getIntArray(TestsActivity.RIGHT_ANSWERS);
        userAnswers = b.getIntArray(TestsActivity.USER_ANSWERS);
        minutes = b.getInt(TestsActivity.TIME);

        wrongAnswersCount = 0;
        for (int i = 0; i < userAnswers.length; i++)
            if (userAnswers[i] != rightAnswers[i])
                wrongAnswersCount++;

        int percentage = 100 / questions.length * wrongAnswersCount;
        if (percentage > 0) {
            textViewErrorsCount.setText(String.format("%d (%d%%)", wrongAnswersCount, percentage));
            textViewErrorsCount.setTextColor(Color.RED);
        } else {
            textViewErrorsCount.setText(String.format("%d", wrongAnswersCount));
            textViewErrorsCount.setTextColor(getResources().getColor(R.color.right_answer));
            buttonWrongQuestions.setEnabled(false);
        }

    }
}
