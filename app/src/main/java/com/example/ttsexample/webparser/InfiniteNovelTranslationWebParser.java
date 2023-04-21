package com.example.ttsexample.webparser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfiniteNovelTranslationWebParser extends WebParser {
    private static final String THIS_CHAPTER_IS = "This chapter is the edited version of another translation. You can find the original chapter post here.";
    private static final String PLEASE_NOTICE = "Please notice that this chapter has NOT been re-translated but only edited in order to improve the readability.";
    private static final String PLEASE_BE_SURE = "Please be sure to thank the translator in the link provided above, if you wish to express your gratitude.";
    private static final String THIS_CHAPTER_WAS = "This chapter was merely moved to this blog as the original blog has been inactive for quite a while.";
    private static final String BY_NO_RIGHTS = "By no rights Infinite Novel Translations claims ownership of the translation.";
    private static final String EDITORS = "Editors: Loxy, Sage, Shasu";
    private static final String FOOTER = "Glossary | Next Chapter –>";

    public InfiniteNovelTranslationWebParser(){
        unwanteds.addAll(Arrays.asList(THIS_CHAPTER_IS, PLEASE_NOTICE, PLEASE_BE_SURE, THIS_CHAPTER_WAS, BY_NO_RIGHTS, EDITORS, FOOTER));
    }

    @Override
    public StringBuffer getNextLink(Document doc) {
        List<Element> elements = doc.select("p a");
        NexLink = new StringBuffer(substitute(elements, "Next"));
        return NexLink;
    }

    @Override
    public StringBuffer getPreviousLink(Document doc) {
        List<Element> elements = doc.select("p a");
        PreviousLink = new StringBuffer(substitute(elements, "Previous"));
        return PreviousLink;
    }

    private StringBuffer substitute(List<Element> elements, String keyWord) {
        StringBuffer result = new StringBuffer("");
        for(Element element : elements) {
            StringBuffer text = new StringBuffer(element.text());
            if(text.toString().contains(keyWord) && element.hasAttr("href")){
                String link = element.attr("href").replace("http", "https");
                result.append(link);
            }
        }
        return result;
    }

    @Override
    public StringBuffer getTitle(Document doc) throws Exception {
        String metaDescription = doc.select("meta[name=description]").get(0).attr("content");
        return parseMetaDescription(metaDescription);
    }

    private StringBuffer parseMetaDescription(String metaDescription) throws Exception {
        Pattern pattern = Pattern.compile(WebParser.INFINITE_TRANSLATIONS + "/");
        Matcher matcher = pattern.matcher(metaDescription);
        StringBuffer result = new StringBuffer();

        if(matcher.find()){
            String rest = metaDescription.substring(matcher.end());
            String rawTitle = rest.substring(0, rest.indexOf("/"));
            result.append(rawTitle.replace("-", " "));
        } else {
            throw new Exception("Unable to parse metadescription: "+ metaDescription);
        }
        return result;
    }

    @Override
    public StringBuffer parseDocument(Document doc) {
        List<Element> textBase = doc.getElementsByClass("entry-content");
        return this.handleParsing(textBase);
    }
}

