package com.example.ttsexample;


import com.example.ttsexample.webparser.EuropaIsACoolMoon;
import com.example.ttsexample.webparser.InfiniteNovelTranslationWebParser;
import com.example.ttsexample.webparser.LightNovelReaderWebParser;
import com.example.ttsexample.webparser.MTLReaderWebParser;
import com.example.ttsexample.webparser.NovelTopWebParser;
import com.example.ttsexample.webparser.RoyalRoadWebParser;
import com.example.ttsexample.webparser.WebParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class URLHandler {

    public static class Response{
        StringBuffer prev, next, text, title;

        public Response(StringBuffer prev, StringBuffer next, StringBuffer text, StringBuffer title){
            this.prev = prev;
            this.next = next;
            this.text = text;
            this.title = title;
        }

        public String toString(){
            return String.format("{ \n\tprev: %s,\n\tTitle: %s\n\tnext: %s\n\ttext: \n\t%s }", prev, title, next, text);
        }

    }

    public static Response handleURL(String url){
        Request request = new Request.Builder().url(url).build();
        CallBackFuture future = new CallBackFuture();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(future);
        try {
            okhttp3.Response response = future.get();
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
//                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                    JeanniusLogger.log(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                }
                StringBuffer value = new StringBuffer(responseBody.string());
                Document doc = Jsoup.parse(value.toString());
                doc.select("script").remove();

                 StringBuffer currentLink = new StringBuffer(url);
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
                StringBuffer nextLink = webParser.getNextLink(doc);

                StringBuffer previousLink = webParser.getPreviousLink(doc);

                StringBuffer title = webParser.getTitle(doc);


//                System.out.println(doc);
//                JeanniusLogger.log("\n\n\n");
                StringBuffer temp = webParser.parseDocument(doc);
//                JeanniusLogger.log(temp);
//
//                Intent callIntent = new Intent();
//                callIntent.setPackage("com.hyperionics.avar");
//                callIntent.setAction(Intent.ACTION_SEND);
//                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//                callIntent.putExtra(Intent.EXTRA_TEXT, temp.toString());
//                callIntent.setType("text/plain");
                return new Response(previousLink, nextLink, temp, title);

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

    private static String getUrlHost(String red){
        try {
            URL url = new URL(red);
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
