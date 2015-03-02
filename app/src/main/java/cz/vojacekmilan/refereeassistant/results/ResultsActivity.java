package cz.vojacekmilan.refereeassistant.results;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebView;

import org.htmlcleaner.XPatherException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Scanner;

import cz.vojacekmilan.refereeassistant.R;

public class ResultsActivity extends ActionBarActivity {

    private WebView tableWebView;
    private WebView currentResultsWebView;
    private WebView nextMatchesWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        tableWebView = (WebView) findViewById(R.id.tableWebView);
        currentResultsWebView = (WebView) findViewById(R.id.currentResultsWebView);
        nextMatchesWebView = (WebView) findViewById(R.id.nextMatchesWebView);
//        new DatabaseFillerAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new DatabaseFillerRunnable("DatabaseFiller Thread").start();
    }

    private String getTextFromFile(File f) throws IOException {
        StringBuilder textStringBuilder = new StringBuilder();
        FileInputStream fis = new FileInputStream(f);
        Scanner sc = new Scanner(fis);
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            textStringBuilder.append(s);
        }
        sc.close();
        fis.close();
        return textStringBuilder.toString();
    }

    private class DatabaseFillerRunnable implements Runnable {
        private Thread t;
        private String threadName;

        DatabaseFillerRunnable(String threadName) {
            this.threadName = threadName;
        }

        public void run() {
            Results results = new Results();
            try {
                results.refreshAll();
                Log.i("milda", results.getLeagues().toString());
            } catch (MalformedURLException | XPatherException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            if (t == null) {
                t = new Thread(this, threadName);
                t.start();
            }
        }
    }

    private class DatabaseFillerAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Results results = new Results();
            try {
                results.refreshAll();
                return results.getLeagues().toString();
            } catch (MalformedURLException | XPatherException e) {
                e.printStackTrace();
            }
            return "ahoj";
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i("milda", s);
        }
    }

}
