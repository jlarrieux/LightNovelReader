package com.jeannius.lightnovelreader.webparser;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

public abstract class WebParser {
    public static final String LIGHT_NOVEL_READER = "lightnovelreader.me";
    public static final String LIGHT_NOVEL_READER2 = "lnreader.org";
    public static final String ROYAL_ROAD = "www.royalroad.com";
    public static final String MLT_READER = "mtlreader.com";
    public static final String NOVEL_TOP = "noveltop.net";
    public static final String INFINITE_TRANSLATIONS = "infinitenoveltranslations.net";
    public static final String EUROPA_IS_A_COOL_M0ON = "europaisacoolmoon.wordpress.com";
    public static final String FREE_WEBNOVEL = "freewebnovel.com";
    public static final String DEFAULT_DELIMITER = "/";
    protected static final String HTTPS = "https://";
    public String title = "";
    public String chapterTitle = "";
    protected String host = "";

    protected List<String> unwanteds = new ArrayList<>();
    protected String CHAPTER_CONTENT_CLASS = "chapter-content";

    protected Set<String> blockedStrings;

    protected WebParser(String host, Set<String> blockedStrings) {
        this.host = host;
        this.blockedStrings = blockedStrings;
    }

    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass(CHAPTER_CONTENT_CLASS);
        return this.handleParsing(textBase);
    }

    protected StringBuffer handleParsing(List<Element> textBase) {
//        System.out.printf("\n\n\ntextBase: \n%s\n\n\n", textBase.toString());
        String readable = textBase.get(0).toString();
        Document jsoupDoc = Jsoup.parse(readable);
        Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);
        jsoupDoc.outputSettings(outputSettings);
        jsoupDoc.select("br").before("\\n");
        jsoupDoc.select("p").before("\\n");
        String str = jsoupDoc.html().replaceAll("\\\\n", "\n");
        String strWithNewLines = Jsoup.clean(str, "", Safelist.none(), outputSettings);
        StringBuffer text = new StringBuffer(strWithNewLines);
        text = removeUnwanted(text);
        return text;
    }

    protected StringBuffer removeUnwanted(StringBuffer text) {
        for (String unwant : unwanteds) {
            text = new StringBuffer(text.toString().replace(unwant, ""));
        }
        return removeBlockedStrings(text);
    }

    private StringBuffer removeBlockedStrings(StringBuffer text){
        for(String blocked: blockedStrings){
            text = new StringBuffer(text.toString().replace(blocked, ""));
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

    public abstract String getNextLink(Document doc);

    public abstract String getPreviousLink(Document doc);

    protected abstract String linkSeeker(Document doc, String keyWord);

    public abstract String getTitle(Document doc) throws Exception;

    public static CharSequence[] getParserList() {
        return List.of(LIGHT_NOVEL_READER, ROYAL_ROAD, MLT_READER, NOVEL_TOP, INFINITE_TRANSLATIONS,
                EUROPA_IS_A_COOL_M0ON, FREE_WEBNOVEL).toArray(new CharSequence[0]);
    }


    public String getHost() {
        return host;
    }

    public String getTitle() {
        return this.title;
    }

    public abstract String getChapterTitle(Document doc) throws Exception;

}
