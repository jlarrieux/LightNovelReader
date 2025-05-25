package com.jeannius.lightnovelreader.fragments;

import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadNovelMapFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadSetFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.saveLocally;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.jeannius.lightnovelreader.JeanniusLogger;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.TtsUtteranceListener;
import com.jeannius.lightnovelreader.URLHandler;
import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.model.Novel;
import com.jeannius.lightnovelreader.webparser.WebParserResponse;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ReaderFragment extends Fragment {
    
    private static final String CURRENT_LINK_FILE_NAME = "currentLinkFileName";
    private static final String NEXT_LINK_FILE_NAME = "nextLinkFileName";
    private static final String PREVIOUS_LINK_FILE_NAME = "previousLinkFileName";
    private static final String NOVEL_MAP_FILE_NAME = "novelMapFileName";
    private static final String FREE_WEB_NOVEL_SYNONYMS = "freeWebNovelSynonyms";
    private static final String BLOCKED_STRINGS = "blockedStrings";
    
    private EditText urlEditText;
    private EditText fullTextEditText;
    private Button speakButton;
    private Button nextButton;
    private Button previousButton;
    
    private String currentLink = "";
    private String nextLink = "";
    private String previousLink = "";
    private String titleAndHost = "";
    private StringBuffer tempText;
    
    private TextToSpeech t1;
    private TtsUtteranceListener ttsUtteranceListener;
    private Map<String, String> novelMap = new HashMap<>();
    private Set<String> freeNovelSynonyms = new HashSet<>();
    private Set<String> blockedStringsSet = new HashSet<>();
    
    private ActivityResultLauncher<Intent> startForResult;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                JeanniusLogger.log(result.toString());
            }
        });
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        
        initializeViews(view);
        initializeTts();
        setupClickListeners();
        loadSavedData();
        
        // Check if URL was passed from another fragment
        Bundle args = getArguments();
        if (args != null && args.containsKey("url")) {
            String url = args.getString("url");
            urlEditText.setText(url);
            getTextFromWeb();
        }
        
        return view;
    }
    
    private void initializeViews(View view) {
        urlEditText = view.findViewById(R.id.url);
        fullTextEditText = view.findViewById(R.id.fullText);
        speakButton = view.findViewById(R.id.button);
        nextButton = view.findViewById(R.id.next);
        previousButton = view.findViewById(R.id.previous);
    }
    
    private void initializeTts() {
        ttsUtteranceListener = new TtsUtteranceListener();
        t1 = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setOnUtteranceProgressListener(ttsUtteranceListener);
                    t1.setSpeechRate(1.7f);
                }
            }
        });
    }
    
    private void setupClickListeners() {
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
    }
    
    private void loadSavedData() {
        currentLink = loadFromLocal(CURRENT_LINK_FILE_NAME, getContext());
        urlEditText.setText(currentLink);
        nextLink = loadFromLocal(NEXT_LINK_FILE_NAME, getContext());
        previousLink = loadFromLocal(PREVIOUS_LINK_FILE_NAME, getContext());
        novelMap = loadNovelMapFromLocal(NOVEL_MAP_FILE_NAME, getContext());
        freeNovelSynonyms = loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getContext());
        blockedStringsSet = loadSetFromLocal(BLOCKED_STRINGS, getContext());
    }
    
    private void getTextFromWeb() {
        String url = urlEditText.getText().toString();
        if (url.isEmpty()) {
            toastUser("URL cannot be empty");
            return;
        }
        if (!isValidURL(url)) {
            JeanniusLogger.log("NOT VALID", url);
            toastUser(String.format("%s is not a valid URL", url));
            return;
        }
        
        freeNovelSynonyms.add("freewebnovel.noveleast.com");
        freeNovelSynonyms.add("freewebnovel.comenovel.com");
        
        CompletableFuture<WebParserResponse> future = new URLHandler().handleURL(url, freeNovelSynonyms, blockedStringsSet);
        future.thenAccept(webParserResponse -> {
            
            getActivity().runOnUiThread(() -> {
                // update UI with response
                
                saveLocally(url, CURRENT_LINK_FILE_NAME, getContext());
                currentLink = url;
                
                nextLink = webParserResponse.next;
                if (nextLink != null && !nextLink.toString().isEmpty()) {
                    JeanniusLogger.log("Jeannius next link not empty: " + nextLink);
                    saveLocally(nextLink.toString(), NEXT_LINK_FILE_NAME, getContext());
                } else {
                    JeanniusLogger.log("jeannius!!! next link is empty");
                }
                
                previousLink = webParserResponse.prev;
                if (previousLink != null && !previousLink.toString().isEmpty()) {
                    JeanniusLogger.log("Jeannius previous link not empty: " + previousLink);
                    saveLocally(previousLink.toString(), PREVIOUS_LINK_FILE_NAME, getContext());
                } else {
                    JeanniusLogger.log("jeannius!!! previous link is empty");
                }
                
                titleAndHost = webParserResponse.getTitleAndHost();
                if (titleAndHost != null && !titleAndHost.isEmpty()) {
                    JeanniusLogger.log("Jeannius title not empty: " + titleAndHost);
                    JeanniusLogger.log("Jeannius saving: " + currentLink);
                    saveTitleCurrentLink(titleAndHost, currentLink);
                } else {
                    JeanniusLogger.log("jeannius!!! title is empty");
                }
                
                tempText = webParserResponse.text;
                fullTextEditText.setText(tempText.toString());
                
                Intent callIntent = new Intent();
                callIntent.setPackage("com.hyperionics.avar");
                callIntent.setAction(Intent.ACTION_SEND);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                callIntent.putExtra(Intent.EXTRA_TEXT, tempText.toString());
                callIntent.setType("text/plain");
                
                try {
                    startForResult.launch(callIntent, ActivityOptionsCompat.makeTaskLaunchBehind());
                } catch (ActivityNotFoundException e) {
                    toastUser(e.getMessage());
                }
            });
            
        }).exceptionally(ex -> {
            getActivity().runOnUiThread(() -> {
                toastUser(ex.getMessage());
            });
            return null;
        });
    }
    
    private void toastUser(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void executeNext() {
        if (nextLink == null || nextLink.length() == 0) {
            toastUser("No next link");
        } else {
            urlEditText.setText(nextLink);
            JeanniusLogger.log("nextLink", nextLink);
            getTextFromWeb();
        }
    }
    
    private void executePrevious() {
        if (previousLink == null || previousLink.length() == 0) {
            toastUser("No previous link");
        } else {
            urlEditText.setText(previousLink);
            JeanniusLogger.log("previousLink", previousLink);
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
    
    private void saveTitleCurrentLink(String titleAndHost, String currentLink) {
        // Save to old format for compatibility
        novelMap.put(titleAndHost, currentLink);
        saveLocally(novelMap, NOVEL_MAP_FILE_NAME, getContext());
        
        // Save to database
        NovelDatabaseHelper dbHelper = new NovelDatabaseHelper(getContext());
        Novel novel = dbHelper.getNovelByUrl(currentLink);
        if (novel == null) {
            novel = new Novel(titleAndHost, currentLink);
        }
        novel.setLastReadDate(System.currentTimeMillis());
        
        // Extract chapter number from title if possible
        String chapter = extractChapterFromTitle(titleAndHost);
        if (chapter != null) {
            novel.setCurrentChapter(chapter);
        }
        
        dbHelper.insertOrUpdateNovel(novel);
        dbHelper.close();
    }
    
    private String extractChapterFromTitle(String title) {
        // Try to extract chapter number from title
        // Common patterns: "Chapter 123", "Ch. 123", "Chapter 123:", etc.
        String lowerTitle = title.toLowerCase();
        int chapterIndex = lowerTitle.indexOf("chapter");
        if (chapterIndex == -1) {
            chapterIndex = lowerTitle.indexOf("ch.");
        }
        if (chapterIndex == -1) {
            chapterIndex = lowerTitle.indexOf("ch ");
        }
        
        if (chapterIndex != -1) {
            // Extract the chapter part
            String afterChapter = title.substring(chapterIndex);
            // Find first digit
            int digitStart = -1;
            for (int i = 0; i < afterChapter.length(); i++) {
                if (Character.isDigit(afterChapter.charAt(i))) {
                    digitStart = i;
                    break;
                }
            }
            
            if (digitStart != -1) {
                // Find end of digits
                int digitEnd = digitStart;
                while (digitEnd < afterChapter.length() && 
                       (Character.isDigit(afterChapter.charAt(digitEnd)) || 
                        afterChapter.charAt(digitEnd) == '.' ||
                        afterChapter.charAt(digitEnd) == '-')) {
                    digitEnd++;
                }
                
                if (digitEnd > digitStart) {
                    return "Chapter " + afterChapter.substring(digitStart, digitEnd);
                }
            }
        }
        
        return null;
    }
    
    @Override
    public void onDestroy() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }
}