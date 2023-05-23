package com.example.ttsexample.webparser;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class MTLReaderWebParser extends WebParser{


    public MTLReaderWebParser(String host) {
        super(host);
    }

    @Override
    public StringBuilder getNextLink(Document doc) {
        return linkSeeker(doc, "Next");
    }

    @Override
    public StringBuilder getPreviousLink(Document doc) {
        return linkSeeker(doc, "Back");
    }

    protected StringBuilder linkSeeker(Document doc, String keyWord){
        StringBuilder result = new StringBuilder("");
        List<Element> elements = doc.select("h4:has(a)");
        for (Element element: elements) {
            Element a = element.selectFirst("a");
            StringBuilder text = new StringBuilder(a. toString());
            if(a.toString().contains(keyWord)){
                result = new StringBuilder(a.attr("href"));
                break;
            }
        }

        return result;
    }

    @Override
    public StringBuilder getTitle(Document doc) throws Exception {
        String rest = doc.select("meta[name=description]").get(0).attr("content");
        StringBuilder title = parseRawTitle(rest, "-");
        return title;
    }

    @Override
    public StringBuilder getChapterTitle(Document doc) throws Exception {
        return new StringBuilder();
    }
}
