package com.example.ttsexample.webparser;

public class WebParserResponse {
    public String prev, next, title, host, chapterTitle;
    public StringBuffer text;

    public WebParserResponse(String prev, String next, StringBuffer text,
                             String title, String host, String chapterTitle) {
        this.prev = prev;
        this.next = next;
        this.text = text;
        this.title = title;
        this.host = host;
        this.chapterTitle = chapterTitle;
    }

    public String getTitleAndHost() {
        return String.format("%s - %s", title, host);
    }

    public String toString() {
        return String.format("{ \n\tprev: %s,\n\tTitle: %s\n\tnext: %s\n\ttext: \n\t%s }", prev, title, next, text);
    }

}
