package com.example.ttsexample.webparser;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;

public class MTLReaderWebParser extends WebParser{


    public MTLReaderWebParser(String host) {
        super(host);
    }

    @Override
    public String getNextLink(Document doc) {
        return linkSeeker(doc, "Next");
    }

    @Override
    public String getPreviousLink(Document doc) {
        return linkSeeker(doc, "Back");
    }

    protected String linkSeeker(Document doc, String keyWord){
        StringBuffer result = new StringBuffer("");
        List<Element> elements = doc.select("h4:has(a)");
        for (Element element: elements) {
            Element a = element.selectFirst("a");
            StringBuffer text = new StringBuffer(a. toString());
            if(a.toString().contains(keyWord)){
                result = new StringBuffer(a.attr("href"));
                break;
            }
        }

        return result.toString();
    }

    @Override
    public String getTitle(Document doc) throws Exception {
        String rest = doc.select("meta[name=description]").get(0).attr("content");
        StringBuffer title = parseRawTitle(rest, "-");
        return title.toString();
    }

    @Override
    public String getChapterTitle(Document doc) throws Exception {
        return "";
    }
}
