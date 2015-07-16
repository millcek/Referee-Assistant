package cz.vojacekmilan.refereeassistant.tests;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Arrays;

import cz.vojacekmilan.refereeassistant.R;

public class ExamBrowsingActivity extends AppCompatActivity {
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
        nextButton = (Button) findViewById(R.id.next_button);
        prevButton = (Button) findViewById(R.id.prev_button);
        answersTextView = new TextView[4];
        answersTextView[0] = (TextView) findViewById(R.id.text_view_answer_1);
        answersTextView[1] = (TextView) findViewById(R.id.text_view_answer_2);
        answersTextView[2] = (TextView) findViewById(R.id.text_view_answer_3);
        answersTextView[3] = (TextView) findViewById(R.id.text_view_answer_4);
        questionTextView = (TextView) findViewById(R.id.text_view_question);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        fillerView = findViewById(R.id.fillerView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_exam_browsing);

        initialize();

//TODO gestures
        Bundle b = getIntent().getExtras();

        Parcelable[] parcelableArray = b.getParcelableArray(Tests.QUESTIONS);
        if (parcelableArray != null)
            questions = Arrays.copyOf(parcelableArray, parcelableArray.length, Question[].class);

        index = 0;
        redraw();
    }
}
