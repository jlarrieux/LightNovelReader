package com.jeannius.lightnovelreader.webparser;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EuropaIsACoolMoon extends WebParser{

    private static final String AUTHOR = "Author: 夜州 (Yashu)";
    private static final String Previous_Toc = "Previous | TOC | Raws | Next";

    public static final String Share = "Share this:";
    public static final String Twitter = "Twitter";
    public static final String FaceBook = "Facebook";
    public static final String Like_This = "Like this:";

    public static final String Like_Loading = "Like Loading... ";
    public EuropaIsACoolMoon(String host, Set<String> blocked) {
        super(host, blocked);
        unwanteds.addAll(Arrays.asList(AUTHOR, Previous_Toc, Share, Twitter, FaceBook, Like_This, Like_Loading));
    }

    @Override
    public String getNextLink(Document doc) {
        return linkSeeker(doc, "Next");
    }

    @Override
    public String getPreviousLink(Document doc) {
        return linkSeeker(doc, "Previous");
    }

    @Override
    protected String linkSeeker(Document doc, String keyWord) {
        StringBuffer result = new StringBuffer();
        List<Element> elements = doc.getElementsByClass("entry-content").select("p a");
        for(Element element: elements) {
            String text = element.text();
            if(text.contains(keyWord)){
                result.append(element.attr("href"));
            }
        }

        return result.toString();
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textBase);
    }

    @Override
    public String getTitle(Document doc) throws Exception {
        Element potentialTitle = doc.getElementsByClass("entry-meta").get(0);
        Element potentialTitleRefined = potentialTitle.selectFirst("a[rel=category tag]");
        return potentialTitleRefined.text();
    }

    @Override
    public String getChapterTitle(Document doc) throws Exception {
        return "";
    }
}
