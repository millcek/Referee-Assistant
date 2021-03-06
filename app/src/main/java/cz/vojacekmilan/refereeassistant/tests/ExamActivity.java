package cz.vojacekmilan.refereeassistant.tests;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
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

public class ExamActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener {
    private Question[] questions;
    private int index;
    private boolean stoppedTimer;

    ScrollView scrollView;
    Button previousButton;
    Button nextButton;
    RadioGroup answersRadioGroup;
    RadioButton[] answersRadioButton;
    TextView questionTextView;
    TextView timerTextView;

    private Handler customHandler = new Handler();
    private long startTime;
    private int minutes;

    private void redraw() {
        previousButton.setEnabled(index != 0);
        nextButton.setText(getString((index == questions.length - 1) ? R.string.evaluate : (R.string.next)));

        questionTextView.setText(String.format("[%d/%d] %s", index + 1, questions.length, questions[index]));
        answersRadioGroup.clearCheck();
        for (int i = 0; i < answersRadioButton.length; i++) {
            if (questions[index].getAnswers().length > i) {
                if (questions[index].getAnswer(i).isUsers())
                    answersRadioButton[i].setChecked(true);
                answersRadioButton[i].setText(questions[index].getAnswer(i).toString());
                answersRadioButton[i].setVisibility(View.VISIBLE);
                answersRadioButton[i].setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            } else {
                answersRadioButton[i].setVisibility(View.INVISIBLE);
                answersRadioButton[i].setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, 0));
            }
        }
        scrollView.pageScroll(View.FOCUS_UP);
    }

    public void nextQuestion(View view) {
        if (index == questions.length - 1) {
            Intent intent = new Intent(this, ExamEvaluationActivity.class);
            intent.putExtra(Tests.QUESTIONS, questions);
            intent.putExtra(Tests.TIME, minutes);
            stoppedTimer = true;
            startActivity(intent);
            finish();
        } else {
            index++;
            redraw();
        }
    }

    public void prevQuestion(View view) {
        if (index == 0) return;
        index--;
        redraw();
    }

    public void saveAnswer(View view) {
        for (int i = 0; i < answersRadioButton.length; i++) {
            if (i >= questions[index].getAnswers().length)
                return;
            questions[index].getAnswer(i).setUsers(answersRadioButton[i].isChecked());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stoppedTimer = true;
    }

    private void generateQuestions(int questionCount) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this, Tests.DB_NAME);
        databaseHelper.openDataBase();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        List<Integer> randomValues = new LinkedList<>();

        questions = new Question[questionCount];
        Random random = new Random();
        int n;
        StringBuilder queryStringBuilder = new StringBuilder();

        for (int i = 0; i < questionCount; i++) {
            questions[i] = new Question();
            do n = random.nextInt(Tests.DB_QUESTIONS_COUNT) + 1;
            while (randomValues.contains(n));
            randomValues.add(n);
            queryStringBuilder.append(" _id=").append(n).append((i != questionCount - 1) ? " OR" : "");
        }

        String query = queryStringBuilder.toString();

        synchronized (this) {
            Cursor questionsCursor = db.rawQuery("SELECT text FROM questions WHERE" + query, null);
            int i = 0;
            while (questionsCursor.moveToNext()) {
                questions[i].setText(questionsCursor.getString(0));
                i++;
            }
            questionsCursor.close();
        }

        synchronized (this) {
            Cursor answersCursor = db.rawQuery(String.format("SELECT id_questions, text, correct FROM answers WHERE%s", query.replace("_id", "id_questions")), null);
            int i = 0;
            int correct = 0;
            int last = -1;
            List<String> listAnswers = new LinkedList<>();
            while (answersCursor.moveToNext()) {
                n = answersCursor.getInt(0);
                if (n != last) {
                    if (last != -1) {
                        fillAnswers(i, listAnswers, correct);
                        listAnswers = new LinkedList<>();
                        i++;
                    }
                    last = n;
                }
                if (answersCursor.getInt(2) == 1)
                    correct = listAnswers.size();
                listAnswers.add(answersCursor.getString(1));
            }
            fillAnswers(i, listAnswers, correct);
            answersCursor.close();
        }

        db.close();
        databaseHelper.close();
    }

    private void fillAnswers(int index, List<String> listAnswers, int correct) {
        questions[index].setAnswers(new Answer[listAnswers.size()]);
        for (int j = 0; j < listAnswers.size(); j++) {
            questions[index].setAnswer(j, new Answer());
            questions[index].getAnswer(j).setText(listAnswers.get(j));
        }
        questions[index].getAnswer(correct).setCorrect(true);
        questions[index].mixUpAnswers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        previousButton = (Button) findViewById(R.id.prev_button);
        nextButton = (Button) findViewById(R.id.next_button);
        answersRadioGroup = (RadioGroup) findViewById(R.id.radio_group_answers);
        answersRadioButton = new RadioButton[4];
        questionTextView = (TextView) findViewById(R.id.text_view_question);
        timerTextView = (TextView) findViewById(R.id.text_view_timer);

        answersRadioButton[0] = (RadioButton) findViewById(R.id.radio_button_1);
        answersRadioButton[1] = (RadioButton) findViewById(R.id.radio_button_2);
        answersRadioButton[2] = (RadioButton) findViewById(R.id.radio_button_3);
        answersRadioButton[3] = (RadioButton) findViewById(R.id.radio_button_4);

        generateQuestions(getIntent().getExtras().getInt(Tests.QUESTIONS_COUNT));

        index = 0;
        redraw();

        minutes = getIntent().getExtras().getInt(Tests.TIME);
        startTime = SystemClock.uptimeMillis() + (minutes * 60 * 1000);
        customHandler.postDelayed(updateTimerThread, 0);
        stoppedTimer = false;
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            if (stoppedTimer)
                return;

            long timeInMilliseconds = startTime - SystemClock.uptimeMillis();

            if (timeInMilliseconds <= 0) {
                index = questions.length - 1;
                nextQuestion(null);
                finish();
                return;
            }

            int secs = (int) (timeInMilliseconds / 1000);
            timerTextView.setText(String.format("Do konce testu zbývá: %02d:%02d", secs / 60, secs % 60));
            customHandler.postDelayed(this, 0);
        }

    };

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i("gesture", "ondown");
        return false;
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
