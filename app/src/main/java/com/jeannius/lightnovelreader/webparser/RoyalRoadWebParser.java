package com.jeannius.lightnovelreader.webparser;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoyalRoadWebParser extends WebParser{

    public RoyalRoadWebParser(String host){
        super(host);
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
        List<Element> elements = doc.getElementsByClass("btn-primary");
        StringBuffer result = new StringBuffer();
        for (Element element : elements) {
            StringBuffer text = new StringBuffer(element.text());
            if (text.toString().contains(keyWord) && element.hasAttr("href")) {
                result = new StringBuffer(HTTPS +host);
                result.append(element.attr("href"));
                break;
            }
        }
        return result.toString();
    }

    @Override
    public String getTitle(Document doc) {
        StringBuffer result = new StringBuffer();
        Element formElement = doc.getElementsByClass("follow-author-form").get(0);
        Element inputElement = formElement.getElementsByTag("input").get(0);
        StringBuffer value = new StringBuffer(inputElement.attr("value"));
        Pattern pattern = Pattern.compile("\\/[fiction]+\\/\\d+\\/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value.toString());
        if(matcher.find()) {
            StringBuffer newVal = new StringBuffer(matcher.replaceFirst(""));
            int index = newVal.indexOf("/");
            result.append(newVal.substring(0, index));
        }
        this.title = result.toString();
        return this.title;
    }

    @Override
    public String getChapterTitle(Document doc) {
        Element head = doc.getElementsByTag("head").first();
        Element potentialTitle = head.getElementsByTag("title").first();
        String title = potentialTitle.text().split("-")[0].trim();
        this.chapterTitle = title;

        return this.chapterTitle;
    }
}
