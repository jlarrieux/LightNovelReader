package com.example.ttsexample;


import com.example.ttsexample.webparser.EuropaIsACoolMoon;
import com.example.ttsexample.webparser.InfiniteNovelTranslationWebParser;
import com.example.ttsexample.webparser.LightNovelReaderWebParser;
import com.example.ttsexample.webparser.MTLReaderWebParser;
import com.example.ttsexample.webparser.NovelTopWebParser;
import com.example.ttsexample.webparser.RoyalRoadWebParser;
import com.example.ttsexample.webparser.WebParser;
import com.example.ttsexample.webparser.WebParserResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

public class URLHandler {

    public WebParserResponse handleURL(String url){
        Request request = new Request.Builder().url(url).build();
        CallBackFuture future = new CallBackFuture();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(future);
        try {
            okhttp3.Response response = future.get();
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                StringBuffer value = new StringBuffer(responseBody.string());
                Document doc = Jsoup.parse(value.toString());
                doc.select("script").remove();
//                System.out.println(doc);

                String host = getUrlHost(url);
                WebParser webParser;
                switch(host) {
                    case WebParser.LIGHT_NOVEL_READER:
                    case WebParser.LIGHT_NOVEL_READER2:
                        webParser = new LightNovelReaderWebParser(host);
                        break;
                    case WebParser.ROYAL_ROAD:
                        webParser = new RoyalRoadWebParser(host);
                        break;
                    case WebParser.MLT_READER:
                        webParser = new MTLReaderWebParser(host);
                        break;
                    case WebParser.NOVEL_TOP:
                        webParser = new NovelTopWebParser(host);
                        break;
                    case WebParser.INFINITE_TRANSLATIONS:
                        webParser = new InfiniteNovelTranslationWebParser(host);
                        break;
                    case WebParser.EUROPA_IS_A_COOL_M0ON:
                        webParser = new EuropaIsACoolMoon(host);
                        break;
                    default:
                        String message = String.format("No Jeannius parser found for url: %s",host);
                        throw new Exception(message);
                }
                String nextLink = webParser.getNextLink(doc);

                String previousLink = webParser.getPreviousLink(doc);

                String title = webParser.getTitle(doc);

                StringBuffer temp = webParser.parseDocument(doc);
                String chapterTitle = webParser.getChapterTitle(doc);

                return new WebParserResponse(previousLink, nextLink, temp, title, webParser.getHost(), chapterTitle);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        throw new Error("Unknown error");
    }

    private String getUrlHost(String red){
        try {
            URL url = new URL(red);
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void saveAll(){

    }
}
