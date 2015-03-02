package cz.vojacekmilan.refereeassistant.tests;

import android.os.Parcel;
import android.os.Parcelable;

public class Answer implements Parcelable {

    private String text;
    private boolean isCorrect;
    private boolean isUsers;

    public Answer() {
    }

    public Answer(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public Answer(Parcel in) {
        readFromParcel(in);
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public boolean isUsers() {
        return isUsers;
    }

    public void setUsers(boolean isUsers) {
        this.isUsers = isUsers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(text);
        destination.writeByte((byte) (isCorrect ? 1 : 0));
        destination.writeByte((byte) (isUsers ? 1 : 0));
    }

    public void readFromParcel(Parcel in) {
        text = in.readString();
        isCorrect = in.readByte() == 1;
        isUsers = in.readByte() == 1;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Answer createFromParcel(Parcel in) {
            return new Answer(in);
        }

        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };

    @Override
    public String toString() {
        return text;
    }
}
