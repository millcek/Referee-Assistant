package cz.upol.vojami04.refereeassistant.tests;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import cz.upol.vojami04.refereeassistant.R;


public class TestsBrowsingActivity extends ActionBarActivity {
    // TODO
//    nacte se vzdy urcity pocet otazek (50), musi se pamatovat vsechny pro prohlizeni zpet a i vsechny odpovedi
    // nejdriv dodelat tridu pro otazky :-))
    private int pos;
    private int wrongCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests_browsing);
    }

    private void redraw() {

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

    public void saveAnswer(View view) {
    }

    public void nextQuestion(View view) {
    }

    public void prevQuestion(View view) {
    }
}
