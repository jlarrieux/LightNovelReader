package com.example.ttsexample.webparser;

import com.example.ttsexample.JeanniusLogger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class MTLReaderWebParser extends WebParser{


    @Override
    public StringBuffer getNextLink(Document doc) {
        NexLink = new StringBuffer("");
        List<Element> elements = doc.select("h4:has(a)");
        Element title = doc.selectFirst("title");
        JeanniusLogger.log("title", title.text());
        for (Element element: elements) {
            Element a = element.selectFirst("a");
            StringBuffer text = new StringBuffer(a. toString());
            if(a.toString().contains("Next")){
                NexLink = new StringBuffer(a.attr("href"));
                break;
            }
        }
        JeanniusLogger.log("Found next", NexLink.toString());
        return NexLink;
    }

    @Override
    public StringBuffer getPreviousLink(Document doc) {
        return new StringBuffer("");
    }
}
