package cz.vojacekmilan.refereeassistant;

import android.os.AsyncTask;
import android.util.Log;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import cz.vojacekmilan.refereeassistant.results.League;
import cz.vojacekmilan.refereeassistant.results.Region;

import static cz.vojacekmilan.refereeassistant.Utils.getCleanTagNodes;
import static cz.vojacekmilan.refereeassistant.Utils.getSerializedHtml;
import static cz.vojacekmilan.refereeassistant.Utils.getStringXpath;
import static cz.vojacekmilan.refereeassistant.Utils.subStringCount;

/**
 * Created by milan on 15.3.15.
 */
public class DatabaseFillerAsyncTask extends AsyncTask<String, String, String> {
    public static final String CHARSET = "iso-8859-2";

    private List<String> visitedLinks;
    private List<Region> regions;
    private List<League> leagues;

    @Override
    protected String doInBackground(String... params) {
        regions = new LinkedList<>();
        leagues = new LinkedList<>();
        try {
            String startUrlString = "http://nv.fotbal.cz/domaci-souteze/index.php";
            TagNode root = getMenu(getCleanTagNodes(new URL(startUrlString), CHARSET));
            String xpath = "//li/a/@href";
            visitedLinks = new LinkedList<>();
            for (Object o : root.evaluateXPath(xpath)) {
                String s = String.format("http://nv.fotbal.cz%s", o.toString());
                if (!visitedLinks.contains(s) && s.contains("/domaci-souteze/")) {
                    visitedLinks.add(s);
                    if (s.contains("/kao/") || s.contains("/souteze-mladeze/"))
                        this.regions.add(getRegion(s));//TODO nahazet do db
                    else
                        this.leagues.add(getLeague(s));
                }
            }
            return leagues.toString();
        } catch (MalformedURLException | XPatherException e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    protected void onPostExecute(String s) {
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    private TagNode getMenu(TagNode root) {
        String xpath = "//div[@class='lm_rounded']/div[@class='lm_rounded-tl']/div[@class='lm_rounded-tr']/div[@class='lm_rounded-bl']/div[@class='lm_rounded-br']/ul[@class='lm']";
        return getCleanTagNodes(getSerializedHtml(root, xpath, "utf-8"));
    }

    private Region getRegion(String url) throws MalformedURLException, XPatherException {

        TagNode root = getCleanTagNodes(new URL(url), CHARSET);
        LinkedList<Region> subRegions = new LinkedList<>();
        LinkedList<League> leagues = new LinkedList<>();

        if (url.contains("/souteze-mladeze/")) {
            if (subStringCount(url, "/") < 6)
                subRegions.addAll(getRegions(getMenu(root), "//li/ul/li/a/@href"));
            else
                leagues.addAll(getLeagues(root, url, "//*[@idLeague=\"maincontainer\"]/table/tbody/tr/td[2]/a/@href"));
        } else if (url.contains("/kao/")) {
            if (subStringCount(url, "/") < 6)
                subRegions.addAll(getRegions(getMenu(root), "//li/ul/li/a/@href"));
            else {
                leagues.addAll(getLeagues(root, url, "//*[@idLeague=\"maincontainer\"]/table/tbody/tr/td[2]//table[@width='470']//a/@href"));
                subRegions.addAll(getRegions(getMenu(root), "//li/ul/li/ul/li/a/@href"));
            }
        }
        String name = getStringXpath(root, "//title/text()");
        if (name.contains(" - "))
            name = name.substring(name.indexOf(" - ") + 3);
        publishProgress(name);
        return new Region(name, subRegions, leagues);
    }

    private List<League> getLeagues(TagNode root, String url, String xpath) throws XPatherException, MalformedURLException {
        List<League> leagues = new LinkedList<>();
        for (Object o : root.evaluateXPath(xpath)) {
            String s = String.format("%s%s", url.substring(0, url.lastIndexOf("/") + 1), o.toString());
            if (!visitedLinks.contains(s) && s.contains("/domaci-souteze/")) {
                visitedLinks.add(s);
                leagues.add(getLeague(s));
            }
        }
        return leagues;
    }

    private List<Region> getRegions(TagNode root, String xpath) throws XPatherException, MalformedURLException {
        List<Region> regions = new LinkedList<>();
        List<String> links = new LinkedList<>();
        for (Object o : root.evaluateXPath(xpath)) {
            String s = String.format("http://nv.fotbal.cz%s", o.toString());
            if (!visitedLinks.contains(s) && s.contains("/domaci-souteze/")) {
                visitedLinks.add(s);
                links.add(s);
            }
        }
        for (String s : links)
            regions.add(getRegion(s));
        return regions;
    }

    private League getLeague(String url) throws MalformedURLException, XPatherException {
        TagNode root = getCleanTagNodes(new URL(url), CHARSET);
        if (!url.contains("/souteze.asp?soutez=")) {
            for (Object o : getMenu(root).evaluateXPath("//li/ul/li/a/@href")) {
                String s = String.format("http://nv.fotbal.cz%s", o.toString());
                if (s.contains("/souteze.asp?soutez=")) {
                    if (s.contains("&amp;"))
                        s = s.substring(0, s.indexOf("&amp;"));
                    visitedLinks.add(s);
                    return getLeague(s);
                }
            }
            return null;
        }
        String name = getStringXpath(root, "//*[@idLeague=\"maincontainer\"]/table/tbody/tr/td[2]/div/h4/text()");
        return new League(name, url, null, null);
    }

}

