package com.jeannius.lightnovelreader.webparser;


import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LightNovelReaderWebParser extends WebParser {
    private static final String SPONSORED_CONTENT = "Sponsored Content";
    private static final String SPONSORED_CONTENT2 = "SPONSORED CONTENT";
    private static final String FIND_AUTHORIZED = "Find authorized novels in Webnovel，faster updates, better experience，Please click for visiting.";
    private static final String FIND_AUTHORIZED2 = "Find authorized novels in Webnovel，faster updates, better experience，Please click www.webnovel.com for visiting.";
    private static final String IF_YOU_FIND = "If you find any errors ( broken links, non-standard content, etc.. ), Please let us know so we can fix it as soon as possible.";
    private static final String TIP = "Tip: You can use left, right, A and D keyboard keys to browse between chapters.";
    private static final String READ_FIRST = "Read f-irst at   l  i g h t-n o v el r e a-d e r . or g ";
    private static final String READ_FIRST2 = "Read first at l i g h t n o v e l r e a d e r . o r g";

    public LightNovelReaderWebParser(String host, Set<String> blocked){
        super(host, blocked);
        CHAPTER_CONTENT_CLASS = "text-base";
        unwanteds.addAll(Arrays.asList(SPONSORED_CONTENT, SPONSORED_CONTENT2, FIND_AUTHORIZED,
                FIND_AUTHORIZED2, IF_YOU_FIND, TIP, READ_FIRST, READ_FIRST2));
    }

    @Override
    public String getNextLink(Document doc) {
        return linkSeeker(doc, "NEXT");
    }

    @Override
    public String getPreviousLink(Document doc) {
        return linkSeeker(doc, "PREVIOUS");
    }

    @Override
    protected String linkSeeker(Document doc, String keyWord) {
        StringBuffer result = new StringBuffer();
        List<Element> elements = doc.getElementsByClass("cm-button");
        for (Element element : elements) {
            StringBuffer text = new StringBuffer(element.text());
            if (text.toString().contains(keyWord)) {
                result = new StringBuffer(element.attr("href"));
                break;
            }
        }
        return result.toString();
    }

    @Override
    public String getTitle(Document doc) throws Exception {
        String metaLink = doc.select("link[rel=alternate]").get(0).attr("href");
        StringBuffer result = parseMetaDescription(metaLink, LIGHT_NOVEL_READER2, DEFAULT_DELIMITER);
        return result.toString();
    }

    @Override
    public String getChapterTitle(Document doc) throws Exception {
        return "";
    }
}
