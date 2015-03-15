package cz.vojacekmilan.refereeassistant.results;

import android.util.Log;

import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static cz.vojacekmilan.refereeassistant.Utils.getCleanTagNodes;
import static cz.vojacekmilan.refereeassistant.Utils.getSerializedHtml;
import static cz.vojacekmilan.refereeassistant.Utils.getStringXpath;
import static cz.vojacekmilan.refereeassistant.Utils.subStringCount;

/**
 * Created by milan on 17.2.15.
 */
public class Results {
    private static final String CHARSET = "iso-8859-2";

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

    public League getClubs(League league) throws MalformedURLException, XPatherException {
        league.setClubs(getClubs(String.format("%s&show=Aktual", league.getUrlString())));
        return league;
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
        Log.i("milda","region "+ name);
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

    private List<Club> getClubs(String url) throws MalformedURLException, XPatherException {
        List<Club> clubs = new LinkedList<>();
        TagNode root = getCleanTagNodes(new URL(url), CHARSET);
        root = getCleanTagNodes(getSerializedHtml(root, "//*[@id=\"maincontainer\"]/table/tbody/tr/td[2]/div//table[2]", "utf-8"));
        String htmlTable = getSerializedHtml(root, "//tr[@bgcolor='#f8f8f8']") + "\n" + getSerializedHtml(root, "//tr[@bgcolor='#ffffff']");
        String format = "%empty %name %empty %wins %draws %losses %score %empty %empty %pointstruth";
        while (htmlTable.length() > 0) {
            if (!htmlTable.contains("</tr>"))
                break;
            String row = htmlTable.substring(0, htmlTable.indexOf("</tr>"));
            htmlTable = htmlTable.substring(htmlTable.indexOf("</tr>") + 5);
            String rowFormat = format;
            Club newClub = new Club();
            while (rowFormat.contains("%") && row.contains("</td>")) {
                rowFormat = rowFormat.substring(rowFormat.indexOf("%") + 1);
                String actual;
                String node = row.substring(0, row.indexOf("</td>"));
                node = node.replaceAll("<.*?>", "").replaceAll("\n", " ").trim();
                row = row.substring(row.indexOf("</td>") + 5);
                if (rowFormat.contains("%"))
                    actual = rowFormat.substring(0, rowFormat.indexOf("%")).trim().toLowerCase();
                else
                    actual = rowFormat.trim().toLowerCase();
                switch (actual) {
                    case "name":
                        newClub.setName(node);
                        break;
                    case "wins":
                        newClub.setWinnings(Integer.valueOf(node));
                        break;
                    case "draws":
                        newClub.setDraws(Integer.valueOf(node));
                        break;
                    case "losses":
                        newClub.setLosses(Integer.valueOf(node));
                        break;
                    case "score":
                        if (node.contains(":")) {
                            newClub.setScoredGoals(Integer.valueOf(node.substring(0, node.indexOf(":")).trim()));
                            newClub.setReceivedGoals(Integer.valueOf(node.substring(1 + node.indexOf(":")).trim()));
                        }
                        break;
                    case "pointstruth":
                        newClub.setPointsTruth(Integer.valueOf(node.replace("(", "").replace(")", "").trim()));
                        break;
                }
            }
            clubs.add(newClub);
        }
        return clubs;
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
