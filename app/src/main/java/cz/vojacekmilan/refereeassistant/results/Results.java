package cz.vojacekmilan.refereeassistant.results;

import android.database.sqlite.SQLiteDatabase;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import static cz.vojacekmilan.refereeassistant.Utils.*;

/**
 * Created by milan on 17.2.15.
 */
public class Results {
    private static final Logger LOGGER = Logger.getLogger(Results.class.getName());
    public static final String CHARSET = "iso-8859-2";

    private List<String> visitedLinks;
    private List<Region> regions;
    private List<League> leagues;

    public Results() {
        regions = new LinkedList<>();
        leagues = new LinkedList<>();
    }

    public void refreshAll() throws MalformedURLException, XPatherException {
        String startUrlString = "http://nv.fotbal.cz/domaci-souteze/index.php";
        TagNode root = getMenu(getCleanTagNodes(new URL(startUrlString), CHARSET));
        String xpath = "//li/a/@href";
        visitedLinks = new LinkedList<>();
        for (Object o : root.evaluateXPath(xpath)) {
            String s = String.format("http://nv.fotbal.cz%s", o.toString());
            if (!visitedLinks.contains(s) && s.contains("/domaci-souteze/")) {
                visitedLinks.add(s);
                if (s.contains("/kao/") || s.contains("/souteze-mladeze/"))
                    this.regions.add(getRegion(s));
                else
                    this.leagues.add(getLeague(s));
            }
        }
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
                leagues.addAll(getLeagues(root, url, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/a/@href"));
        } else if (url.contains("/kao/")) {
            if (subStringCount(url, "/") < 6)
                subRegions.addAll(getRegions(getMenu(root), "//li/ul/li/a/@href"));
            else {
                leagues.addAll(getLeagues(root, url, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]//table[@width='470']//a/@href"));
                subRegions.addAll(getRegions(getMenu(root), "//li/ul/li/ul/li/a/@href"));
            }
        }
        String name = getStringXpath(root, "//title/text()");
        if (name.contains(" - "))
            name = name.substring(name.indexOf(" - ") + 3);
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
        String name = getStringXpath(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div/h4/text()");
        return new League(name, url, null, null);
    }





    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public List<League> getLeagues() {
        return leagues;
    }

    public void setLeagues(List<League> leagues) {
        this.leagues = leagues;
    }


}
