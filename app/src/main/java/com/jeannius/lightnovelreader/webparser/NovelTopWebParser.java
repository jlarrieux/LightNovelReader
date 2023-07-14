package com.jeannius.lightnovelreader.webparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class NovelTopWebParser extends WebParser {
    public NovelTopWebParser(String host) {
        super(host);
    }

    @Override
    public String getNextLink(Document doc) {
        return linkSeeker(doc, "nav-next");
    }

    @Override
    public String getPreviousLink(Document doc) {
        return linkSeeker(doc, "nav-previous");
    }

    @Override
    protected String linkSeeker(Document doc, String keyword) {
        StringBuffer result = new StringBuffer("");

        try {
            Element element = doc.getElementsByClass(keyword).first().selectFirst("a");
            result.append(element.attr("href"));
        } catch (NullPointerException nullPointerException) {
        }
        return result.toString();
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textbase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textbase);
    }

    @Override
    public String getTitle(Document doc) throws Exception {
        String meta = doc.select("link[rel=canonical]").get(0).attr("href");
        StringBuffer result = parseMetaDescription(meta, NOVEL_TOP + "/novel", DEFAULT_DELIMITER);
        return result.toString();
    }

    @Override
    public String getChapterTitle(Document doc) throws Exception {
        return "";
    }
}
