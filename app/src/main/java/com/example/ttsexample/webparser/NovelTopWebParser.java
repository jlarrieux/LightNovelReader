package com.example.ttsexample.webparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class NovelTopWebParser extends WebParser {
    public NovelTopWebParser(String host) {
        super(host);
    }

    @Override
    public StringBuilder getNextLink(Document doc) {
        return linkSeeker(doc, "nav-next");
    }

    @Override
    public StringBuilder getPreviousLink(Document doc) {
        return linkSeeker(doc, "nav-previous");
    }

    @Override
    protected StringBuilder linkSeeker(Document doc, String keyword) {
        StringBuilder result = new StringBuilder("");

        try {
            Element element = doc.getElementsByClass(keyword).first().selectFirst("a");
            result.append(element.attr("href"));
        } catch (NullPointerException nullPointerException) {
        }
        return result;
    }

    @Override
    public StringBuilder parseDocument(Document doc) {
        List<Element> textbase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textbase);
    }

    @Override
    public StringBuilder getTitle(Document doc) throws Exception {
        String meta = doc.select("link[rel=canonical]").get(0).attr("href");
        StringBuilder result = parseMetaDescription(meta, WebParser.NOVEL_TOP + "/novel", WebParser.DEFAULT_DELIMITER);
        return result;
    }

    @Override
    public StringBuilder getChapterTitle(Document doc) throws Exception {
        return new StringBuilder();
    }
}
