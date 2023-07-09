package com.example.ttsexample.webparser;

import com.example.ttsexample.JeanniusLogger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class FreeWebNovel extends WebParser {
    public FreeWebNovel(String host) {
        super(host);
    }

    @Override
    public String getNextLink(Document doc) {
        return linkSeeker(doc, "Next Chapter");
    }

    @Override
    public String getPreviousLink(Document doc) {
        return linkSeeker(doc, "Prev Chapter");
    }

    @Override
    protected String linkSeeker(Document doc, String keyWord) {
        Element element = doc.getElementsByClass("ul-list7").first();
        List<Element> listElements = element.getElementsByTag("li");
        StringBuffer result = new StringBuffer();
        for (Element li : listElements) {
            StringBuffer text = new StringBuffer(li.text());
            if (text.toString().contains(keyWord)) {
                result = new StringBuffer(HTTPS + host);
                result.append(li.getElementsByTag("a").first().attr("href"));
                JeanniusLogger.log("result so far: ", result.toString());
                break;
            }
        }
        return result.toString();
    }

    @Override
    public String getTitle(Document doc) throws Exception {
        return localSearch(doc, "og:novel:novel_name");
    }

    private String localSearch(Document doc, String property){
        Element head = doc.getElementsByTag("head").first();
        List<Element> metaElements = head.getElementsByTag("meta");
        StringBuffer result = new StringBuffer();
        for (Element meta : metaElements) {
            if (meta.hasAttr("property") &&
                    meta.attr("property").equals(property) &&
                    meta.hasAttr("content")) {
                result = new StringBuffer(meta.attr("content"));
                break;
            }
        }
        return result.toString();
    }

    @Override
    public String getChapterTitle(Document doc) throws Exception {
        String chapterTitle = localSearch(doc, "og:novel:chapter_name");
        JeanniusLogger.log(" %%% chapter title: ", chapterTitle);
        return null;
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass("txt");
        return this.handleParsing(textBase);
    }
}
