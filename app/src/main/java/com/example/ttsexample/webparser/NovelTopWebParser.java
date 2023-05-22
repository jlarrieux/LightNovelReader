package com.example.ttsexample.webparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class NovelTopWebParser extends WebParser {
    public NovelTopWebParser(String host) {
        super(host);
    }

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
        StringBuffer result = new StringBuffer("");

        try {
            Element element = doc.getElementsByClass(keyword).first().selectFirst("a");
            result.append(element.attr("href"));
        } catch (NullPointerException nullPointerException) {
        }
        return result;
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textbase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textbase);
    }

    @Override
    public StringBuffer getTitle(Document doc) throws Exception {
        String meta = doc.select("link[rel=canonical]").get(0).attr("href");
        StringBuffer result = parseMetaDescription(meta, WebParser.NOVEL_TOP + "/novel", WebParser.DEFAULT_DELIMITER);
        return addHost(result);
    }
}
