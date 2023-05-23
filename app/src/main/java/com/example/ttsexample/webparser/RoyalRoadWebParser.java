package com.example.ttsexample.webparser;


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
    public StringBuilder getNextLink(Document doc) {
        return linkSeeker(doc, "Next");
    }

    @Override
    public StringBuilder getPreviousLink(Document doc) {
        return linkSeeker(doc, "Previous");
    }

    @Override
    protected StringBuilder linkSeeker(Document doc, String keyWord) {
        List<Element> elements = doc.getElementsByClass("btn-primary");
        StringBuilder result = new StringBuilder();
        for (Element element : elements) {
            StringBuilder text = new StringBuilder(element.text());
            if (text.toString().contains(keyWord) && element.hasAttr("href")) {
                result = new StringBuilder("https://"+host);
                result.append(element.attr("href"));
                break;
            }
        }
        return result;
    }

    @Override
    public StringBuilder getTitle(Document doc) {
        StringBuilder result = new StringBuilder();
        Element formElement = doc.getElementsByClass("follow-author-form").get(0);
        Element inputElement = formElement.getElementsByTag("input").get(0);
        StringBuilder value = new StringBuilder(inputElement.attr("value"));
        Pattern pattern = Pattern.compile("\\/[fiction]+\\/\\d+\\/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value.toString());
        if(matcher.find()) {
            StringBuilder newVal = new StringBuilder(matcher.replaceFirst(""));
            int index = newVal.indexOf("/");
            result.append(newVal.substring(0, index));
        }
        this.title = new StringBuilder(result);
        return this.title;
    }

    @Override
    public StringBuilder getChapterTitle(Document doc) {
        Element head = doc.getElementsByTag("head").first();
        Element potentialTitle = head.getElementsByTag("title").first();
        String title = potentialTitle.text().split("-")[0].trim();
        this.chapterTitle = new StringBuilder(title);

        return this.chapterTitle;
    }
}
