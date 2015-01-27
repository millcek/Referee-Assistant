package cz.upol.vojami04.refereeassistant.tests;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Question implements Parcelable {

    private String text;
    private Answer[] answers;

    public Question() {
    }

    public Question(Parcel in) {
        readFromParcel(in);
    }

    public void setText(String text) {
        this.text = text;
    }

    public Answer[] getAnswers() {
        return answers;
    }

    public void setAnswers(Answer[] answers) {
        this.answers = answers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(text);
        destination.writeTypedArray(answers, flags);
    }

    public void readFromParcel(Parcel in) {
        text = in.readString();
        Object[] array = in.createTypedArray(Answer.CREATOR);
        if (array != null)
            answers = Arrays.copyOf(array, array.length, Answer[].class);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    public Answer getAnswer(int index) {
        return answers[index];
    }

    public void setAnswer(int index, Answer answer) {
        answers[index] = answer;
    }

    @Override
    public String toString() {
        return text;
    }

    public int getCorrectAnswerIndex() {
        for (int i = 0; i < answers.length; i++)
            if (answers[i].isCorrect())
                return i;
        return -1;
    }

    public int getUsersAnswerIndex() {
        for (int i = 0; i < answers.length; i++)
            if (answers[i].isUsers())
                return i;
        return -1;
    }

    public List<Integer> getUsersAnswersList() {
        List<Integer> usersAnswers = new ArrayList<>();
        for (int i = 0; i < answers.length; i++)
            if (answers[i].isUsers())
                usersAnswers.add(i);
        return usersAnswers;
    }

    private Answer getCorrectAnswer() {
        for (Answer a : answers)
            if (a.isCorrect())
                return a;
        return null;
    }

    public boolean isCorrectlyAnswered() {
        Answer a = getCorrectAnswer();
        if (a == null)
            return false;
        return a.isUsers();
    }

    public void mixUpAnswers() {
        Answer[] mixedUpAnswers = new Answer[answers.length];
        int random = new Random().nextInt(answers.length);
        for (int i = 0; i < answers.length; i++)
            mixedUpAnswers[i] = answers[(i + random) % answers.length];
        answers = mixedUpAnswers;
    }
}