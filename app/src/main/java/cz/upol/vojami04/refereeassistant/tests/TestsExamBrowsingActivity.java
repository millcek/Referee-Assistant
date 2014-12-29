package cz.upol.vojami04.refereeassistant.tests;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cz.upol.vojami04.refereeassistant.R;


public class TestsExamBrowsingActivity extends ActionBarActivity {
    private String[] questions;
    private String[][] answers;
    private int[] rightAnswers;
    private int[] userAnswers;
    private int index;
    Button buttonPrev;
    Button buttonNext;
    TextView[] textViewAnswers;
    TextView textViewQuestion;

    private final String TAG = "milda";

    private void redraw() {
        Log.i(TAG,"zacatek redraw");
        buttonPrev.setEnabled(index != 0);
        buttonNext.setEnabled(index != questions.length - 1);
        textViewQuestion.setText(questions[index]);
        for (int i = 0; i < textViewAnswers.length; i++) {
            textViewAnswers[i].setBackgroundColor(0);
            if (answers[index].length > i) {
                textViewAnswers[i].setText(answers[index][i]);
                textViewAnswers[i].setVisibility(View.VISIBLE);
            } else
                textViewAnswers[i].setVisibility(View.INVISIBLE);
        }
        if (userAnswers[index] >= 0 && userAnswers[index] < answers[index].length)
            textViewAnswers[userAnswers[index]].setBackgroundColor(Color.RED);
        if (rightAnswers[index] >= 0 && rightAnswers[index] < answers[index].length)
            if (userAnswers[index] < 0)
                textViewAnswers[rightAnswers[index]].setBackgroundColor(Color.YELLOW);
            else
                textViewAnswers[rightAnswers[index]].setBackgroundColor(Color.GREEN);
    }

    public void nextQuestion(View view) {
        index++;
        redraw();
    }

    public void prevQuestion(View view) {
        index--;
        redraw();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"browsing oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_browsing);

        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrevious);
        textViewAnswers = new TextView[4];
        textViewAnswers[0] = (TextView) findViewById(R.id.textViewAnswer1);
        textViewAnswers[1] = (TextView) findViewById(R.id.textViewAnswer2);
        textViewAnswers[2] = (TextView) findViewById(R.id.textViewAnswer3);
        textViewAnswers[3] = (TextView) findViewById(R.id.textViewAnswer4);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);
Log.i(TAG, "nacteni prvku");
        Bundle b = getIntent().getExtras();
        TwoDSerializable s = TwoDSerializable.getSingletonObject();
        answers = s.getArray();
        questions = b.getStringArray(TestsActivity.QUESTIONS);
        rightAnswers = b.getIntArray(TestsActivity.RIGHT_ANSWERS);
        userAnswers = b.getIntArray(TestsActivity.USER_ANSWERS);
        index = 0;
        redraw();
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
