package com.example.ttsexample;


import static com.example.ttsexample.SaverLoaderUtils.loadFromLocal;
import static com.example.ttsexample.SaverLoaderUtils.loadNovelMapFromLocal;
import static com.example.ttsexample.SaverLoaderUtils.saveLocally;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.DialogFragment;

import com.example.ttsexample.DialogFragment.NovelDialogFragment;
import com.example.ttsexample.DialogFragment.ParserDialogFragment;
import com.example.ttsexample.databinding.ActivityMainBinding;
import com.example.ttsexample.webparser.EuropaIsACoolMoon;
import com.example.ttsexample.webparser.InfiniteNovelTranslationWebParser;
import com.example.ttsexample.webparser.LightNovelReaderWebParser;
import com.example.ttsexample.webparser.MTLReaderWebParser;
import com.example.ttsexample.webparser.NovelTopWebParser;
import com.example.ttsexample.webparser.RoyalRoadWebParser;
import com.example.ttsexample.webparser.WebParser;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_LINK_FILE_NAME = "currentLinkFileName";
    private static final String NEXT_LINK_FILE_NAME = "nextLinkFileName";
    private static final String PREVIOUS_LINK_FILE_NAME = "previousLinkFileName";
    private static final String NOVEL_MAP_FILE_NAME = "novelMapFileName";

    private String test = "https://noveltop.net/novel/birth-of-the-demonic-sword/chapter-2227-2227-respect/";

    private ActivityMainBinding binding;
    private Button speakButton, nextButton, previousButton;
    private EditText urlEditText;
    private EditText fullTextEditText;
    private StringBuffer currentLink= new StringBuffer("");
    private Map<String, String> novelMap = new HashMap<>();


    TextToSpeech t1;
    StringBuffer temp;
    StringBuffer nextLink = new StringBuffer();
    StringBuffer previousLink =  new StringBuffer();
    StringBuffer title = new StringBuffer();
    TtsUtteranceListener ttsUtteranceListener;
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            JeanniusLogger.log(result.toString());
        }
    });

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_overflow, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.parserList:
                showParsers();
                break;
            case R.id.novelList:
                showNovels();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Private methods

    private void init() {
        ttsUtteranceListener = new TtsUtteranceListener();
        urlEditText = findViewById(R.id.url);
        fullTextEditText = findViewById(R.id.fullText);
        speakButton = findViewById(R.id.button);
        nextButton = findViewById(R.id.next);
        previousButton = findViewById(R.id.previous);
        t1 = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setOnUtteranceProgressListener(ttsUtteranceListener);
                    t1.setSpeechRate(1.7f);
                }
            }
        });

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTextFromWeb();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNext();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executePrevious();
            }
        });

        currentLink = new StringBuffer(loadFromLocal(CURRENT_LINK_FILE_NAME, getApplicationContext()));
        urlEditText.setText(currentLink);
        nextLink = new StringBuffer(loadFromLocal(NEXT_LINK_FILE_NAME, getApplicationContext()));
        previousLink = new StringBuffer(loadFromLocal(PREVIOUS_LINK_FILE_NAME, getApplicationContext()));
        novelMap = loadNovelMapFromLocal(NOVEL_MAP_FILE_NAME, getApplicationContext());
    }

    private String getTextFromWeb() {
        String url =  urlEditText.getText().toString();
//        String url = "https://europaisacoolmoon.wordpress.com/2021/12/04/chapter-2-reincarnation/";
        StringBuffer result = new StringBuffer("");
        if (url.isEmpty()) {
            toastUser("URL cannot be empty");
            return result.toString();
        }
        if (!isValidURL(url)) {
            JeanniusLogger.log("NOT VALID", url);
            toastUser(String.format("%s is not a valid URL", url));
            return result.toString();
        }

        Request request = new Request.Builder().url(url).build();
        CallBackFuture future = new CallBackFuture();
        client.newCall(request).enqueue(future);
        try {
            Response response = future.get();
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    JeanniusLogger.log(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                StringBuffer value = new StringBuffer(responseBody.string());
                Document doc = Jsoup.parse(value.toString());
                doc.select("script").remove();

                currentLink = new StringBuffer(url);
                saveLocally(url, CURRENT_LINK_FILE_NAME, getApplicationContext());
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
                        toastUser(message);
                        throw new Exception(message);
                }
                nextLink = webParser.getNextLink(doc);
                if(nextLink != null && !nextLink.toString().isEmpty()){
                    JeanniusLogger.log("Jeannius next link not empty: "+ nextLink);
                    saveLocally(nextLink.toString(), NEXT_LINK_FILE_NAME, getApplicationContext());
                } else {
                    JeanniusLogger.log("jeannius!!! next link is empty");
                }

                previousLink = webParser.getPreviousLink(doc);
                if(previousLink != null && !previousLink.toString().isEmpty()){
                    JeanniusLogger.log("Jeannius previous link not empty: "+ previousLink);
                    saveLocally(previousLink.toString(), PREVIOUS_LINK_FILE_NAME, getApplicationContext());
                } else {
                    JeanniusLogger.log("jeannius!!! previous link is empty");
                }

                title = webParser.getTitle(doc);
                if(title != null && !title.toString().isEmpty()) {
                    JeanniusLogger.log("Jeannius title not empty: "+ title);
                    saveTitleCurrentLink(title.toString(), currentLink.toString());
                } else {
                    JeanniusLogger.log("jeannius!!! title is empty");
                }

                System.out.println(doc);
                JeanniusLogger.log("\n\n\n");
                temp = webParser.parseDocument(doc);
                JeanniusLogger.log(temp);
                fullTextEditText.setText(temp.toString());

                Intent callIntent = new Intent();
                callIntent.setPackage("com.hyperionics.avar");
                callIntent.setAction(Intent.ACTION_SEND);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                callIntent.putExtra(Intent.EXTRA_TEXT, temp.toString());
                callIntent.setType("text/plain");

                startForResult.launch(callIntent, ActivityOptionsCompat.makeTaskLaunchBehind());

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
        return result.toString();
    }

    private void toastUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getUrlHost(String red){
        try {
            URL url = new URL(red);
            return url.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            toastUser("error while getting baseurl");
        }
        return "";
    }

    private void executeNext() {
        if (nextLink == null || nextLink.length() == 0) {
            toastUser("No next link");
        } else {
            urlEditText.setText(nextLink.toString());
            JeanniusLogger.log(nextLink.toString());
            getTextFromWeb();
        }
    }

    private void executePrevious(){
        if(previousLink == null || previousLink.length() == 0) {
            toastUser("No previous link");
        } else {
            urlEditText.setText(previousLink.toString());
            JeanniusLogger.log(previousLink.toString());
            getTextFromWeb();
        }
    }

    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showParsers(){
        DialogFragment newFragment = new ParserDialogFragment();
        newFragment.show(getSupportFragmentManager(), "Parsers");
    }

    private void showNovels(){
        DialogFragment newFragment = new NovelDialogFragment(NOVEL_MAP_FILE_NAME, urlEditText, fullTextEditText);
        newFragment.show(getSupportFragmentManager(), "Novels");
    }

    private void saveTitleCurrentLink(String title, String currentLink){
        novelMap.put(title, currentLink);
        saveLocally(novelMap, NOVEL_MAP_FILE_NAME, getApplicationContext());
    }

}


