package cz.upol.vojami04.refereeassistant.tests;

import java.io.Serializable;

/**
 * Created by milan on 27.12.14.
 */
public class TwoDSerializable implements Serializable {
    private static final long serialVersionUID = 1L;
    String[][] array;
    private static TwoDSerializable singletonObject;

    public static TwoDSerializable getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new TwoDSerializable();
        }
        return singletonObject;
    }

    public void setArray(String[][] array) {
        this.array = array;
    }

    public String[][] getArray() {
        return array;
    }
}
