package cz.vojacekmilan.refereeassistant.tests;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.vojacekmilan.refereeassistant.DatabaseHelper;
import cz.vojacekmilan.refereeassistant.R;


public class PracticeActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private ScrollView scrollView;
    private Button nextButton;
    private Button prevButton;
    private RadioGroup answersRadioGroup;
    private RadioButton[] answersRadioButton;
    private TextView questionTextView;
    private Question question;
    private List<Integer> generatedQuestions;
    private View fillerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_practice);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        nextButton = (Button) findViewById(R.id.next_button);
        prevButton = (Button) findViewById(R.id.prev_button);
        answersRadioGroup = (RadioGroup) findViewById(R.id.radio_group_answers);
        answersRadioButton = new RadioButton[4];
        answersRadioButton[0] = (RadioButton) findViewById(R.id.radio_button_1);
        answersRadioButton[1] = (RadioButton) findViewById(R.id.radio_button_2);
        answersRadioButton[2] = (RadioButton) findViewById(R.id.radio_button_3);
        answersRadioButton[3] = (RadioButton) findViewById(R.id.radio_button_4);
        questionTextView = (TextView) findViewById(R.id.text_view_question);
        generatedQuestions = new LinkedList<>();
        fillerView = findViewById(R.id.fillerView);

//TODO gestures
        nextQuestion(null);
    }

    private void redraw() {
        questionTextView.setText(question.toString());
        answersRadioGroup.clearCheck();
        for (int i = 0; i < answersRadioButton.length; i++) {
            if (question.getAnswer(i) != null) {
                answersRadioButton[i].setText(question.getAnswer(i).toString());
                answersRadioButton[i].setVisibility(View.VISIBLE);
                answersRadioButton[i].setEnabled(true);
                answersRadioButton[i].setBackgroundColor(getResources().getColor(R.color.transparent));
                answersRadioButton[i].setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            } else {
                answersRadioButton[i].setVisibility(View.INVISIBLE);
                answersRadioButton[i].setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, 0));
            }
        }
        scrollView.pageScroll(View.FOCUS_UP);
        nextButton.setEnabled(false);
        prevButton.setEnabled(generatedQuestions.size() > 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tests_browsing, menu);
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

    private Question getRandomQuestion() {
        Random random = new Random();
        int randomInt;
        do {
            randomInt = random.nextInt(Tests.DB_QUESTIONS_COUNT);
        } while (generatedQuestions.contains(randomInt));
        generatedQuestions.add(randomInt);

        return getQuestionFromDB(randomInt);
    }

    private Question getQuestionFromDB(int id) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this, Tests.DB_NAME);
        databaseHelper.openDataBase();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        int maxLength = 50;
        if (generatedQuestions.size() > maxLength)
            generatedQuestions = generatedQuestions.subList(generatedQuestions.size() - maxLength, generatedQuestions.size());

        Cursor questionCursor = db.rawQuery(String.format("SELECT text FROM questions WHERE _id = %d", id), null);
        Question outputQuestion = new Question();
        if (questionCursor.moveToNext())
            outputQuestion.setText(questionCursor.getString(0));
        questionCursor.close();

        Cursor answersCursor = db.rawQuery(String.format("SELECT text, correct FROM answers WHERE id_questions = %d", id), null);
        while (answersCursor.moveToNext())
            outputQuestion.addAnswer(new Answer(answersCursor.getString(0), answersCursor.getInt(1) == 1));
        answersCursor.close();
        db.close();
        databaseHelper.close();
        return outputQuestion;
    }

    public void saveAnswer(View view) {
        for (int i = 0; i < answersRadioButton.length; i++) {
            if (answersRadioButton[i].isChecked()) {
                question.getAnswer(i).setUsers(true);
                if (question.getAnswer(i).isCorrect()) {
                    nextButton.setEnabled(true);
                    answersRadioButton[i].setBackgroundColor(getResources().getColor(R.color.right_answer));
                    for (RadioButton anAnswersRadioButton : answersRadioButton)
                        anAnswersRadioButton.setEnabled(false);
                } else {
                    answersRadioButton[i].setBackgroundColor(getResources().getColor(R.color.wrong_answer));
                    answersRadioButton[i].setEnabled(false);
                }
            }
        }
    }

    public void nextQuestion(View view) {
        if (question == null || question.isCorrectlyAnswered()) {
            question = getRandomQuestion();
            redraw();
        }
    }

    public void prevQuestion(View view) {
        if (generatedQuestions.size() > 1) {
            generatedQuestions = generatedQuestions.subList(0, generatedQuestions.size() - 1);
            question = getQuestionFromDB(generatedQuestions.get(generatedQuestions.size() - 1));
            redraw();
            answersRadioButton[question.getCorrectAnswerIndex()].setChecked(true);
            saveAnswer(null);
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i("gesture", "ondown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.i("gesture", "onshowpress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i("gesture", "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i("gesture", "onScroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i("gesture", "onLongPress");

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.i("gesture", "onFling");
        return false;
    }
}
