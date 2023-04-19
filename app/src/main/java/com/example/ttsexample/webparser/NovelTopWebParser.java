package com.example.ttsexample.webparser;

import com.example.ttsexample.JeanniusLogger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class NovelTopWebParser extends WebParser{
    @Override
    public StringBuffer getNextLink(Document doc) {
        JeanniusLogger.log("parsed", doc.toString());
        try {
            Element a = doc.getElementsByClass("nav-next").first().selectFirst("a");
            NexLink = new StringBuffer(a.attr("href"));
        } catch (NullPointerException nullPointerException){

        }
        return NexLink;
    }

    @Override
    public StringBuffer parse(Document doc) {
        List<Element> textbase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textbase);
    }

    @Override
    public StringBuffer getTitle(Document doc) {
        return super.getTitle(doc);
    }
}
