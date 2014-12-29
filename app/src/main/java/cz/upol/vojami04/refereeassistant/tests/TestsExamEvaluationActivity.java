package cz.upol.vojami04.refereeassistant.tests;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

        wrongAnswersCount = 0;
        for (int i = 0; i < userAnswers.length; i++)
            if (userAnswers[i] != rightAnswers[i])
                wrongAnswersCount++;

        textViewErrorsCount.setText(String.valueOf(wrongAnswersCount));
        if (wrongAnswersCount == 0) {
            textViewErrorsCount.setTextColor(Color.GREEN);
            buttonWrongQuestions.setEnabled(false);
        } else
            textViewErrorsCount.setTextColor(Color.RED);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tests_exam_evaluation, menu);
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
