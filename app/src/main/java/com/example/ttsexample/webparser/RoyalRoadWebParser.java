package com.example.ttsexample.webparser;

import com.example.ttsexample.JeanniusLogger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoyalRoadWebParser extends WebParser{
    private String host = "";
    private StringBuffer tempNext = new StringBuffer();

    public RoyalRoadWebParser(String host){
        this.host = host;
    }

    @Override
    public StringBuffer getNextLink(Document doc) {
        List<Element> elements = doc.getElementsByClass("btn-primary");
        NexLink = new StringBuffer("");
        for (Element element : elements) {
            StringBuffer text = new StringBuffer(element.text());
            if (text.toString().contains("Next") && element.hasAttr("href")) {
                NexLink = new StringBuffer("https://"+host);
                tempNext.append(element.attr("href"));
                NexLink.append(tempNext);
                break;
            }
        }
        return NexLink;
    }

    @Override
    public StringBuffer getTitle(Document doc) {
        Element formElement = doc.getElementsByClass("follow-author-form").get(0);
        Element inputElement = formElement.getElementsByTag("input").get(0);
        StringBuffer value = new StringBuffer(inputElement.attr("value"));
        Pattern pattern = Pattern.compile("\\/[fiction]+\\/\\d+\\/", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value.toString());
        StringBuffer result = new StringBuffer();
        if(matcher.find()) {
            StringBuffer newVal = new StringBuffer(matcher.replaceFirst(""));
            int index = newVal.indexOf("/");
//            JeanniusLogger.log("rest", newVal.toString());
//            JeanniusLogger.log("index", String.valueOf(index));
//            JeanniusLogger.log("title", newVal.substring(0, index));
            result.append(newVal.substring(0, index));
        }
        return result;
    }
}
