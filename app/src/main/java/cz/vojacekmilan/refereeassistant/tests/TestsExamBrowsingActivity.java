package cz.vojacekmilan.refereeassistant.tests;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;

import cz.vojacekmilan.refereeassistant.R;

public class TestsExamBrowsingActivity extends ActionBarActivity {
    private Question[] questions;
    private int index;
    Button prevButton;
    Button nextButton;
    TextView[] answersTextView;
    TextView questionTextView;
    ScrollView scrollView;
    View fillerView;

    private void redraw() {
        scrollView.pageScroll(View.FOCUS_UP);
        prevButton.setEnabled(index != 0);
        nextButton.setEnabled(index != questions.length - 1);
        Question q = questions[index];
        questionTextView.setText(String.format("[%d/%d] %s", index + 1, questions.length, q));

        for (int i = 0; i < answersTextView.length; i++) {
            answersTextView[i].setBackgroundColor(0);
            if (q.getAnswers().length > i) {
                answersTextView[i].setText(q.getAnswer(i).toString());
                answersTextView[i].setVisibility(View.VISIBLE);
                answersTextView[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                answersTextView[i].setVisibility(View.INVISIBLE);
                answersTextView[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            }
        }

        if (q.isCorrectlyAnswered())
            answersTextView[q.getCorrectAnswerIndex()].setBackgroundColor(getResources().getColor(R.color.right_answer));
        else {
            if (q.getCorrectAnswerIndex() != -1)
                answersTextView[q.getCorrectAnswerIndex()].setBackgroundColor(getResources().getColor(R.color.unanswered));
            if (q.getUsersAnswerIndex() != -1)
                answersTextView[q.getUsersAnswerIndex()].setBackgroundColor(getResources().getColor(R.color.wrong_answer));
        }
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

    private void initialize(){
        nextButton = (Button) findViewById(R.id.nextButton);
        prevButton = (Button) findViewById(R.id.prevButton);
        answersTextView = new TextView[4];
        answersTextView[0] = (TextView) findViewById(R.id.textViewAnswer1);
        answersTextView[1] = (TextView) findViewById(R.id.textViewAnswer2);
        answersTextView[2] = (TextView) findViewById(R.id.textViewAnswer3);
        answersTextView[3] = (TextView) findViewById(R.id.textViewAnswer4);
        questionTextView = (TextView) findViewById(R.id.questionTextView);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        fillerView = findViewById(R.id.fillerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_browsing);

        initialize();

        SwipeDetector swipeDetector = new SwipeDetector() {
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

        questionTextView.setOnTouchListener(swipeDetector);
        fillerView.setOnTouchListener(swipeDetector);
        for (TextView tv : answersTextView)
            tv.setOnTouchListener(swipeDetector);

        Bundle b = getIntent().getExtras();

        Parcelable[] parcelableArray = b.getParcelableArray(TestsFragment.QUESTIONS);
        if (parcelableArray != null)
            questions = Arrays.copyOf(parcelableArray, parcelableArray.length, Question[].class);

        index = 0;
        redraw();
    }
}
