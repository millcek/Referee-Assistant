package cz.upol.vojami04.refereeassistant.tests;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.upol.vojami04.refereeassistant.R;
//TODO
//vyhodnoceni (nextQuestion)

public class TestsExamActivity extends ActionBarActivity {
    private final String TAG = "milda";

    private String[] questions;
    private String[][] answers;
    private int[] rightAnswers;
    private int[] userAnswers;
    private int index;
    Button buttonPrev;
    Button buttonNext;
    RadioGroup radioGroupAnswers;
    RadioButton[] radioButtons;
    TextView textViewQuestion;

    private void redraw() {

        buttonPrev.setEnabled(index != 0);
        buttonNext.setText(getString((index == questions.length - 1) ? R.string.evaluate : (R.string.next)));

        textViewQuestion.setText(questions[index]);
        radioGroupAnswers.clearCheck();
        for (int i = 0; i < radioButtons.length; i++) {
            if (userAnswers[index] == i)
                radioButtons[i].setChecked(true);
            if (answers[index].length > i) {
                radioButtons[i].setText(answers[index][i]);
                radioButtons[i].setVisibility(View.VISIBLE);
            } else
                radioButtons[i].setVisibility(View.INVISIBLE);
        }
    }

    public void nextQuestion(View view) {
        if (index == questions.length - 1) {
            Intent intent = new Intent(this, TestsExamEvaluationActivity.class);
            TwoDSerializable s = TwoDSerializable.getSingletonObject();
            s.setArray(answers);
            intent.putExtra(TestsActivity.QUESTIONS, questions);
            intent.putExtra(TestsActivity.USER_ANSWERS, userAnswers);
            intent.putExtra(TestsActivity.RIGHT_ANSWERS, rightAnswers);
            startActivity(intent);
            return;
        }
        index++;
        redraw();
    }

    public void prevQuestion(View view) {
        if (index == 0)
            return;
        index--;
        redraw();
    }

    public void saveAnswer(View view) {
        for (int i = 0; i < radioButtons.length; i++)
            if (radioButtons[i].isChecked()) {
                userAnswers[index] = i;
                break;
            }
    }

    private void generateQuestions(int questionCount) {
        // paralelizovat
        DataBaseHelper myDbHelper = new DataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        myDbHelper.openDataBase();

        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        int[] randomValues = new int[questionCount];
        questions = new String[questionCount];
        answers = new String[questionCount][];
        rightAnswers = new int[questionCount];
        userAnswers = new int[questionCount];
        Random random = new Random();
        int n;
        StringBuilder queryStringBuilder = new StringBuilder();

        for (int i = 0; i < questionCount; i++) {
            userAnswers[i] = -1;
            do n = random.nextInt(1200) + 1;
            while (Arrays.asList(randomValues).contains(n));
            randomValues[i] = n;
            queryStringBuilder.append(" _id=").append(n).append((i != questionCount - 1) ? " OR" : "");
        }

        String query = queryStringBuilder.toString();

        Cursor questionsCursor = db.rawQuery("SELECT text FROM questions WHERE" + query, null);
        int i = 0;
        while (questionsCursor.moveToNext()) {
            questions[i] = questionsCursor.getString(0);
            i++;
        }
        questionsCursor.close();
        Cursor answersCursor = db.rawQuery(String.format("SELECT id_questions, text, correct FROM answers WHERE%s", query.replace("_id", "id_questions")), null);
        i = 0;
        int last = -1;
        List<String> listAnswers = new LinkedList<>();
        while (answersCursor.moveToNext()) {
            n = answersCursor.getInt(0);
            if (n != last) {
                if (last != -1) {
                    answers[i] = new String[listAnswers.size()];
                    for (int j = 0; j < listAnswers.size(); j++)
                        answers[i][j] = listAnswers.get(j);
                    listAnswers = new LinkedList<>();
                    i++;
                }
                last = n;
            }
            if (answersCursor.getInt(2) == 1)
                rightAnswers[i] = listAnswers.size();
            listAnswers.add(answersCursor.getString(1));
        }
        answers[i] = new String[listAnswers.size()];
        for (int j = 0; j < listAnswers.size(); j++)
            answers[i][j] = listAnswers.get(j);
        answersCursor.close();
        db.close();
        myDbHelper.close();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        redraw();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam);

        buttonNext = (Button) findViewById(R.id.buttonNext);
        buttonPrev = (Button) findViewById(R.id.buttonPrevious);
        radioGroupAnswers = (RadioGroup) findViewById(R.id.radioGroupAnswers);
        radioButtons = new RadioButton[4];
        radioButtons[0] = (RadioButton) findViewById(R.id.radioButton1);
        radioButtons[1] = (RadioButton) findViewById(R.id.radioButton2);
        radioButtons[2] = (RadioButton) findViewById(R.id.radioButton3);
        radioButtons[3] = (RadioButton) findViewById(R.id.radioButton4);
        textViewQuestion = (TextView) findViewById(R.id.textViewQuestion);

        generateQuestions(getIntent().getExtras().getInt(TestsActivity.QUESTIONS_COUNT));

        index = 0;
        redraw();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tests_exam, menu);
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
