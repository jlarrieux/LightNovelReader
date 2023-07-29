package com.jeannius.lightnovelreader;


import com.jeannius.lightnovelreader.webparser.EuropaIsACoolMoon;
import com.jeannius.lightnovelreader.webparser.FreeWebNovel;
import com.jeannius.lightnovelreader.webparser.InfiniteNovelTranslationWebParser;
import com.jeannius.lightnovelreader.webparser.LightNovelReaderWebParser;
import com.jeannius.lightnovelreader.webparser.MTLReaderWebParser;
import com.jeannius.lightnovelreader.webparser.NovelTopWebParser;
import com.jeannius.lightnovelreader.webparser.RoyalRoadWebParser;
import com.jeannius.lightnovelreader.webparser.WebParser;
import com.jeannius.lightnovelreader.webparser.WebParserResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class URLHandler {

    public CompletableFuture<WebParserResponse> handleURL(String url) {
        Request request = new Request.Builder().url(url).build();

        // set timeouts when creating the OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // set the connection timeout
                .writeTimeout(10, TimeUnit.SECONDS) // set the write timeout
                .readTimeout(30, TimeUnit.SECONDS) // set the read timeout
                .build();

        CompletableFuture<WebParserResponse> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    StringBuffer value = new StringBuffer(responseBody.string());
                    Document doc = Jsoup.parse(value.toString());
                    doc.select("script").remove();
                    System.out.println(doc);

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
                        case WebParser.INN_READ:
                        case WebParser.FREE_WEBNOVEL:
                            webParser = new FreeWebNovel(host);
                            break;
                        default:
                            String message = String.format("No Jeannius parser found for url: %s",host);
                            throw new Exception(message);
                    }
                    String nextLink = webParser.getNextLink(doc);

                    String previousLink = webParser.getPreviousLink(doc);

                    String title = webParser.getTitle(doc);

                    String chapterTitle = webParser.getChapterTitle(doc);

                    StringBuffer temp = webParser.parseDocument(doc);

                    WebParserResponse webParserResponse = new WebParserResponse(previousLink, nextLink, temp, title, webParser.getHost(), chapterTitle);
                    future.complete(webParserResponse);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
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
