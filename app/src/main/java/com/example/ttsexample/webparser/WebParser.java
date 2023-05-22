package com.example.ttsexample.webparser;

import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public abstract class WebParser {
    public static final String LIGHT_NOVEL_READER = "lightnovelreader.me";
    public static final String LIGHT_NOVEL_READER2 = "lnreader.org";
    public static final String ROYAL_ROAD = "www.royalroad.com";
    public static final String MLT_READER = "mtlreader.com";
    public static final String NOVEL_TOP = "noveltop.net";
    public static final String INFINITE_TRANSLATIONS = "infinitenoveltranslations.net";

    public static final String EUROPA_IS_A_COOL_M0ON = "europaisacoolmoon.wordpress.com";

    public static final String DEFAULT_DELIMITER = "/";
    public StringBuilder title = new StringBuilder();
    protected String host;

    protected List<String> unwanteds = new ArrayList<>();
    protected String CHAPTER_CONTENT_CLASS = "chapter-content";
    protected String TITLE_CONTENT_TAG = "title";

    protected WebParser(String host) {
        this.host = host;
    }

    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass(CHAPTER_CONTENT_CLASS);
        return this.handleParsing(textBase);
    }

    protected StringBuffer handleParsing(List<Element> textBase) {
        System.out.printf("\n\n\ntextBase: \n%s\n\n\n", textBase.toString());
        StringBuffer text = new StringBuffer(HtmlCompat.fromHtml(textBase.get(0).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        text = removeUnwanted(text);
        return text;
    }

    protected StringBuffer removeUnwanted(StringBuffer text) {
        for (String unwant : unwanteds) {
            text = new StringBuffer(text.toString().replace(unwant, ""));
        }
        return text;
    }

    protected StringBuffer parseMetaDescription(String metaDescription, String patternPrefix, String stopper) throws Exception {
        Pattern pattern = Pattern.compile(patternPrefix + DEFAULT_DELIMITER);
        Matcher matcher = pattern.matcher(metaDescription);
        StringBuffer result = new StringBuffer();

        if (matcher.find()) {
            String rest = metaDescription.substring(matcher.end());
            result = parseRawTitle(rest, stopper);
        } else {
            throw new Exception("Unable to parse metadescription: " + metaDescription);
        }
        return result;
    }

    protected StringBuffer parseRawTitle(String rest, String stopper) {
        String rawTitle = rest.substring(0, rest.indexOf(stopper));
        return new StringBuffer(rawTitle.replace("-", " "));
    }

    public abstract StringBuffer getNextLink(Document doc);

    public abstract StringBuffer getPreviousLink(Document doc);

    protected abstract StringBuffer linkSeeker(Document doc, String keyWord);

    public abstract StringBuffer getTitle(Document doc) throws Exception;

    public static CharSequence[] getParserList() {
        return List.of(LIGHT_NOVEL_READER, ROYAL_ROAD, MLT_READER, NOVEL_TOP, INFINITE_TRANSLATIONS, EUROPA_IS_A_COOL_M0ON).toArray(new CharSequence[0]);
    }

    public StringBuffer addHost(StringBuffer result) {
        return result.append(" - " + host);
    }

}
