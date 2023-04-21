package com.example.ttsexample.webparser;

import com.example.ttsexample.JeanniusLogger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class NovelTopWebParser extends WebParser{
    @Override
    public StringBuffer getNextLink(Document doc) {
        return linkSeeker(doc, "nav-next");
    }

    @Override
    public StringBuffer getPreviousLink(Document doc) {
        return linkSeeker(doc, "nav-previous");
    }

    @Override
    protected StringBuffer linkSeeker(Document doc, String keyword) {
        Element element = doc.getElementsByClass(keyword).first().selectFirst("a");
        return new StringBuffer(element.attr("href"));
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textbase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textbase);
    }

    @Override
    public StringBuffer getTitle(Document doc) throws Exception {
        String meta = doc.select("link[rel=canonical]").get(0).attr("href");
        StringBuffer result = parseMetaDescription(meta, WebParser.NOVEL_TOP + "/novel");
        JeanniusLogger.log("potential title: ", result.toString());
        return result;
    }
}
