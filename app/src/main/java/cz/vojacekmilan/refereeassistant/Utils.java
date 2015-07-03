package cz.vojacekmilan.refereeassistant;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyHtmlSerializer;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by milan on 3.2.15.
 */
public class Utils {
    private static CleanerProperties props = new CleanerProperties();

    static {
        props.setTranslateSpecialEntities(true);
        props.setTransResCharsToNCR(false);
        props.setOmitComments(true);
        props.setOmitXmlDeclaration(true);
    }

    public static void showPopup(Context context, View view, String text) {
        if (text == null || text.replaceAll("//s+", "").length() == 0) return;
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_text_view, null, false);
        final PopupWindow pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        TextView textView = ((TextView) layout.findViewById(R.id.text_view));
        textView.setText(text.replace(": ", ":").replace(":", ": ").replaceAll("\n", "").replace(";", "\n"));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });
        pw.showAtLocation(view, Gravity.CENTER, 0, 0);
//        pw.showAsDropDown(view);
    }

    public static TagNode getCleanTagNodes(String str) {
        return getCleanTagNodes(str, "utf-8");
    }

    public static TagNode getCleanTagNodes(String str, String charset) {
        props.setCharset(charset);
        return new HtmlCleaner(props).clean(str);
    }

    public static TagNode getCleanTagNodes(URL url) {
        return getCleanTagNodes(url, "utf-8");
    }

    public static TagNode getCleanTagNodes(URL url, String charset) {
        props.setCharset(charset);
        try {
            return new HtmlCleaner(props).clean(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getFileFromXpath(String url, String xpath, String fileName) {
        try {
            return getFileFromXpath(new URL(url), xpath, fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getFileFromXpath(URL url, String xpath, String fileName) {
        File f = new File(fileName);
        try {
            URL fileUrl = new URL(url.getProtocol(), url.getHost(), getStringXpathFromUrl(url, xpath));
            InputStream in = fileUrl.openConnection().getInputStream();
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[512];
            while (true) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
            }
            in.close();
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static File getFile(String url) {
        return getFile(url, "tempFile");
    }

    public static File getFile(String url, String fileName) {
        try {
            return getFile(new URL(url), fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getFile(URL url, String fileName) {
        try {
            File f = new File(fileName);
            InputStream in = url.openConnection().getInputStream();
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[512];
            while (true) {
                int len = in.read(buf);
                if (len == -1) {
                    break;
                }
                fos.write(buf, 0, len);
            }
            in.close();
            fos.flush();
            fos.close();
            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getWebPage(URL url) {
        InputStream is = null;
        String line;
        StringBuilder out = new StringBuilder();
        try {
            is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
                out.append(line);
            return out.toString();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static String getTextFromFile(File f) {
        try {
            StringBuilder out = new StringBuilder();
            Scanner scanner = new Scanner(f);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                out.append(line);
            }
            scanner.close();
            return out.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringXpathFromUrl(String url, String xpath) {
        try {
            return getStringXpathFromUrl(new URL(url), xpath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringXpathFromUrl(URL url, String xpath) {
        try {
            return getStringXpath(new HtmlCleaner(props).clean(url), xpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringXpath(TagNode nodes, String xpath) {
        try {
            return nodes.evaluateXPath(xpath)[0].toString();
        } catch (XPatherException e) {
            throw new RuntimeException("getStringXPath xpath " + xpath, e);
        }
    }

    public static String getSerializedHtml(TagNode node, String xpath, String charset) {
        props.setCharset(charset);
        PrettyHtmlSerializer htmlSerializer = new PrettyHtmlSerializer(props);
        if (xpath != null)
            try {
                Object[] nodes = node.evaluateXPath(xpath);
                StringBuilder outStringBuilder = new StringBuilder();
                for (Object o : nodes) {
                    TagNode t = (TagNode) o;
                    outStringBuilder.append(htmlSerializer.getAsString(t, charset));
                }
                return outStringBuilder.toString();
            } catch (XPatherException e) {
                e.printStackTrace();
            }
        return htmlSerializer.getAsString(node, charset);
    }

    public static String getSerializedHtml(TagNode node, String xpath) {
        return getSerializedHtml(node, xpath, "utf-8");
    }

    public static String getSerializedHtml(TagNode node) {
        return getSerializedHtml(node, "utf-8");
    }

    public static int subStringCount(String string, String subString) {
        int count = 0;
        while (string.contains(subString)) {
            string = string.substring(string.indexOf(subString) + 1);
            count++;
        }
        return count;
    }

}
