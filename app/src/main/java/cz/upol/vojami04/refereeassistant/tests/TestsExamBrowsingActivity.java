package cz.upol.vojami04.refereeassistant.tests;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cz.upol.vojami04.refereeassistant.R;


public class TestsExamBrowsingActivity extends ActionBarActivity {
    private String[] questions;
    private String[][] answers;
    private int[] rightAnswers;
    private int[] userAnswers;
    private int index;
    Button prevButton;
    Button nextButton;
    TextView[] answersTextView;
    TextView questionTextView;
    ScrollView scrollView;
    View fillerView;

    private void redraw() {
        prevButton.setEnabled(index != 0);
        nextButton.setEnabled(index != questions.length - 1);
        questionTextView.setText(String.format("[%d/%d] %s", index + 1, questions.length, questions[index]));
        for (int i = 0; i < answersTextView.length; i++) {
            answersTextView[i].setBackgroundColor(0);
            if (answers[index].length > i) {
                answersTextView[i].setText(answers[index][i]);
                answersTextView[i].setVisibility(View.VISIBLE);
                answersTextView[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                answersTextView[i].setVisibility(View.INVISIBLE);
                answersTextView[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
        }
        if (userAnswers[index] >= 0 && userAnswers[index] < answers[index].length)
            answersTextView[userAnswers[index]].setBackgroundColor(getResources().getColor(R.color.wrong_answer));
        if (rightAnswers[index] >= 0 && rightAnswers[index] < answers[index].length)
            if (userAnswers[index] != rightAnswers[index])
                answersTextView[rightAnswers[index]].setBackgroundColor(getResources().getColor(R.color.unanswered));
            else
                answersTextView[rightAnswers[index]].setBackgroundColor(getResources().getColor(R.color.right_answer));
        scrollView.pageScroll(View.FOCUS_UP);

    }

    public void nextQuestion(View view) {
        if (index < questions.length - 1) {
            index++;
            redraw();
        }
    }

    public void prevQuestion(View view) {
        if (index > 0) {
            index--;
            redraw();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_browsing);

        nextButton = (Button) findViewById(R.id.nextButton);
        prevButton = (Button) findViewById(R.id.previousButton);
        answersTextView = new TextView[4];
        answersTextView[0] = (TextView) findViewById(R.id.textViewAnswer1);
        answersTextView[1] = (TextView) findViewById(R.id.textViewAnswer2);
        answersTextView[2] = (TextView) findViewById(R.id.textViewAnswer3);
        answersTextView[3] = (TextView) findViewById(R.id.textViewAnswer4);
        questionTextView = (TextView) findViewById(R.id.questionTextView);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        fillerView = findViewById(R.id.fillerView);

        ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector() {
            @Override
            void onUpSwipe() {
            }

            @Override
            void onDownSwipe() {
            }

            @Override
            void onRightSwipe() {
                prevQuestion(null);
            }

            @Override
            void onLeftSwipe() {
                nextQuestion(null);
            }

            @Override
            void onClick() {
            }
        };
        questionTextView.setOnTouchListener(activitySwipeDetector);
        fillerView.setOnTouchListener(activitySwipeDetector);
        for (int i = 0; i < answersTextView.length; i++)
            answersTextView[i].setOnTouchListener(activitySwipeDetector);

        Bundle b = getIntent().getExtras();
        TwoDSerializable s = TwoDSerializable.getSingletonObject();
        answers = s.getArray();
        questions = b.getStringArray(TestsActivity.QUESTIONS);
        rightAnswers = b.getIntArray(TestsActivity.RIGHT_ANSWERS);
        userAnswers = b.getIntArray(TestsActivity.USER_ANSWERS);
        index = 0;
        redraw();
    }
}
