package com.example.ttsexample.webparser;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.util.Arrays;
import java.util.List;

public class EuropaIsACoolMoon extends WebParser{

    private static final String AUTHOR = "Author: 夜州 (Yashu)";
    private static final String Previous_Toc = "Previous | TOC | Raws | Next";

    public static final String Share = "Share this:";
    public static final String Twitter = "Twitter";
    public static final String FaceBook = "Facebook";
    public static final String Like_This = "Like this:";

    public static final String Like_Loading = "Like Loading... ";
    public EuropaIsACoolMoon(String host) {
        super(host);
        unwanteds.addAll(Arrays.asList(AUTHOR, Previous_Toc, Share, Twitter, FaceBook, Like_This, Like_Loading));
    }

    @Override
    public StringBuffer getNextLink(Document doc) {
        return linkSeeker(doc, "Next");
    }

    @Override
    public StringBuffer getPreviousLink(Document doc) {
        return linkSeeker(doc, "Previous");
    }

    @Override
    protected StringBuffer linkSeeker(Document doc, String keyWord) {
        StringBuffer result = new StringBuffer();
        List<Element> elements = doc.getElementsByClass("entry-content").select("p a");
        for(Element element: elements) {
            String text = element.text();
            if(text.contains(keyWord)){
                result.append(element.attr("href"));
            }
        }

        return result;
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textBase);
    }

    @Override
    public StringBuffer getTitle(Document doc) throws Exception {
        Element potentialTitle = doc.getElementsByClass("entry-meta").get(0);
        Element potentialTitleRefined = potentialTitle.selectFirst("a[rel=category tag]");
        return addHost(new StringBuffer(potentialTitleRefined.text()));
    }
}
