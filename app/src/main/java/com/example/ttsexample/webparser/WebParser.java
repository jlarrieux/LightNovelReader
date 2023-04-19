package com.example.ttsexample.webparser;

import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class WebParser {
    public static final String LIGHT_NOVEL_READER = "lightnovelreader.me";
    public static final String ROYAL_ROAD = "www.royalroad.com";
    public static final String MLT_READER = "mtlreader.com";
    public static final String NOVEL_TOP = "noveltop.net";
    public StringBuilder title = new StringBuilder();

    protected List<String> unwanteds = new ArrayList<>();
    protected String CHAPTER_CONTENT_CLASS = "chapter-content" ;
    protected StringBuffer NexLink;
    protected String TITLE_CONTENT_TAG = "title";

    public StringBuffer parse(Document doc) {
        List<Element> textBase = doc.getElementsByClass(CHAPTER_CONTENT_CLASS);
        return this.handleParsing(textBase);
    }

    protected StringBuffer handleParsing(List<Element> textBase) {
        StringBuffer text = new StringBuffer(HtmlCompat.fromHtml(textBase.get(0).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        text = removeUnwanted(text);
        return text;
    }

    protected StringBuffer removeUnwanted(StringBuffer text) {
        for(String unwant: unwanteds){
            text = new StringBuffer(text.toString().replace(unwant, ""));
        }
        return text;
    }

    public abstract StringBuffer getNextLink(Document doc);

    public StringBuffer getTitle(Document doc) {
        StringBuffer result = new StringBuffer();
        Element title = doc.selectFirst(TITLE_CONTENT_TAG);
        if(title != null) {
            result.append(title.text());
        }
        return result;
    }

    public static CharSequence[] getParserList(){
        return List.of(LIGHT_NOVEL_READER, ROYAL_ROAD, MLT_READER, NOVEL_TOP).toArray(new CharSequence[0]);
    }

}
