package com.example.ttsexample;


import static com.example.ttsexample.SaverLoaderUtils.loadFromLocal;
import static com.example.ttsexample.SaverLoaderUtils.loadNovelMapFromLocal;
import static com.example.ttsexample.SaverLoaderUtils.saveLocally;

import android.content.ActivityNotFoundException;
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

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// MUST USE STRINGBUFFER!!!
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
    String titleAndHost = new String();
    TtsUtteranceListener ttsUtteranceListener;
    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            JeanniusLogger.log(result.toString());
        }
    });


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

    private void getTextFromWeb() {
        String url =  urlEditText.getText().toString();
//        String url = "https://www.royalroad.com/fiction/22518/chrysalis/chapter/422108/the-conclusion-the-feast";
        if (url.isEmpty()) {
            toastUser("URL cannot be empty");
        }
        if (!isValidURL(url)) {
            JeanniusLogger.log("NOT VALID", url);
            toastUser(String.format("%s is not a valid URL", url));
        }

        URLHandler.Response response =  URLHandler.handleURL(url);
//        System.out.printf("response from Jeannius: %s", response);
        saveLocally(url, CURRENT_LINK_FILE_NAME, getApplicationContext());


        nextLink = response.next;
        if(nextLink != null && !nextLink.toString().isEmpty()){
            JeanniusLogger.log("Jeannius next link not empty: "+ nextLink);
            saveLocally(nextLink.toString(), NEXT_LINK_FILE_NAME, getApplicationContext());
        } else {
            JeanniusLogger.log("jeannius!!! next link is empty");
        }

        previousLink = response.prev;
        if(previousLink != null && !previousLink.toString().isEmpty()){
            JeanniusLogger.log("Jeannius previous link not empty: "+ previousLink);
            saveLocally(previousLink.toString(), PREVIOUS_LINK_FILE_NAME, getApplicationContext());
        } else {
            JeanniusLogger.log("jeannius!!! previous link is empty");
        }

        titleAndHost = response.getTitleAndHost();
        if(titleAndHost != null && !titleAndHost.isEmpty()) {
            JeanniusLogger.log("Jeannius title not empty: "+ titleAndHost);
            saveTitleCurrentLink(titleAndHost, currentLink.toString());
        } else {
            JeanniusLogger.log("jeannius!!! title is empty");
        }


        temp = response.text;
//        JeanniusLogger.log(temp);
        fullTextEditText.setText(temp.toString());

        Intent callIntent = new Intent();
        callIntent.setPackage("com.hyperionics.avar");
        callIntent.setAction(Intent.ACTION_SEND);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        callIntent.putExtra(Intent.EXTRA_TEXT, temp.toString());
        callIntent.setType("text/plain");

        try {
            startForResult.launch(callIntent, ActivityOptionsCompat.makeTaskLaunchBehind());
        }catch (ActivityNotFoundException e){
            toastUser(e.getMessage());
        }


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


