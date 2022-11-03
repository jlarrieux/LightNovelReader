package com.example.ttsexample;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.text.HtmlCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.ttsexample.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {


    private static final String CURRENT_LINK_FILE_NAME = "currentLinkFileName";
    private static final String NEXT_LINK_FILE_NAME = "nextLinkFileName";
    private static final String SPONSORED_CONTENT = "Sponsored Content";
    private static final String SPONSORED_CONTENT2 = "SPONSORED CONTENT";
    private static final String FIND_AUTHORIZED = "Find authorized novels in Webnovel，faster updates, better experience，Please click for visiting.";
    private static final String FIND_AUTHORIZED2 = "Find authorized novels in Webnovel，faster updates, better experience，Please click www.webnovel.com for visiting.";

    private ActivityMainBinding binding;
    private Button speakButton, nextButton;
    private EditText urlEditText;
    private EditText fullTextEditText;
    private StringBuffer currentLink= new StringBuffer("");
    private static int REQUEST_CODE = 1981;

    TextToSpeech t1;
    StringBuffer temp;
    StringBuffer nextLink = new StringBuffer();
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

    private void init() {
        ttsUtteranceListener = new TtsUtteranceListener();
        urlEditText = findViewById(R.id.url);
        fullTextEditText = findViewById(R.id.fullText);
        speakButton = findViewById(R.id.button);
        nextButton = findViewById(R.id.next);
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
                JeanniusLogger.log("BUTTON CLICK!!!!");
                getTextFromWeb();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNext();
            }
        });
        urlEditText.setText(loadFromLocal(CURRENT_LINK_FILE_NAME));
        nextLink = new StringBuffer(loadFromLocal(NEXT_LINK_FILE_NAME));
    }

    private String getTextFromWeb() {
        String url = urlEditText.getText().toString();
        StringBuffer result = new StringBuffer("");
        if (url.isEmpty()) {
            toastUser("URL cannot be empty");
            return result.toString();
        }
        if (!isValidURL(url)) {
            toastUser("Not a valid url");
            return result.toString();
        }

        String baseurl = new URL(url);

        Request request = new Request.Builder().url(url).build();
        CallBackFuture future = new CallBackFuture();
        client.newCall(request).enqueue(future);
        try {
            Response response = future.get();
            try (ResponseBody responseBody = response.body()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                StringBuffer value = new StringBuffer(responseBody.string());
                Document doc = Jsoup.parse(value.toString());
                List<Element> elements = doc.getElementsByClass("cm-button");
                currentLink = new StringBuffer(url);
                saveLocally(url, CURRENT_LINK_FILE_NAME);
                saveLocally(nextLink.toString(), NEXT_LINK_FILE_NAME);
                setNextLink(elements);
                List<Element> textBase = doc.getElementsByClass("text-base");
                temp = new StringBuffer(HtmlCompat.fromHtml(textBase.get(0).toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                temp = removeUnwanted(temp);
                fullTextEditText.setText(temp.toString());
                Intent callIntent = new Intent();
                callIntent.setAction(Intent.ACTION_SEND);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                callIntent.putExtra(Intent.EXTRA_TEXT, temp.toString());
                callIntent.setType("text/plain");

                startForResult.launch(callIntent, ActivityOptionsCompat.makeTaskLaunchBehind());


            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private void saveLocally(String value, String filename){
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(value.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private StringBuilder loadFromLocal(String filename){
        try (FileInputStream fis = getApplicationContext().openFileInput(CURRENT_LINK_FILE_NAME); InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8); BufferedReader reader = new BufferedReader(inputStreamReader)) {
            StringBuilder stringBuilder = new StringBuilder(reader.readLine());
            JeanniusLogger.log(stringBuilder.toString());
            return stringBuilder;

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return new StringBuilder("");
    }

    private void toastUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private StringBuffer removeUnwanted(StringBuffer temp) {
        temp = new StringBuffer(temp.toString().replace(SPONSORED_CONTENT, ""));
        temp = new StringBuffer(temp.toString().replace(SPONSORED_CONTENT2, ""));
        temp = new StringBuffer(temp.toString().replace(FIND_AUTHORIZED, ""));
        temp = new StringBuffer(temp.toString().replace(FIND_AUTHORIZED2, ""));
        return temp;
    }

    private void executeNext() {
        if (nextLink.length() == 0) {
            toastUser("No next link");
        } else {
            urlEditText.setText(nextLink.toString());
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

    private void setNextLink(List<Element> elements) {
        for (Element element : elements) {
            StringBuffer text = new StringBuffer(element.text());
            if (text.toString().contains("NEXT")) {
                nextLink = new StringBuffer(element.attr("href"));
                break;
            }
        }
    }



}


