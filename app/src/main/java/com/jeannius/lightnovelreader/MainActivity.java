package com.jeannius.lightnovelreader;


import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadNovelMapFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.loadSetFromLocal;
import static com.jeannius.lightnovelreader.SaverLoaderUtils.saveLocally;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.DialogFragment;

import com.jeannius.lightnovelreader.DialogFragment.SimpleDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.BlockedStringDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.FreeWebNovelSynonymsDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.NovelDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.ParserDialogFragment;
import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdatedListener;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdatedListener;
import com.jeannius.lightnovelreader.Interface.NovelListActionListener;
import com.jeannius.lightnovelreader.databinding.ActivityMainBinding;
import com.jeannius.lightnovelreader.webparser.WebParserResponse;

import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// MUST USE STRINGBUFFER!!!
public class MainActivity extends AppCompatActivity implements NovelListActionListener, OnFreeWebNovelSynonymSetUpdatedListener, OnBlockedStringSetUpdatedListener {

    private static final String CURRENT_LINK_FILE_NAME = "currentLinkFileName";
    private static final String NEXT_LINK_FILE_NAME = "nextLinkFileName";
    private static final String PREVIOUS_LINK_FILE_NAME = "previousLinkFileName";
    private static final String NOVEL_MAP_FILE_NAME = "novelMapFileName";

    private static final String FREE_WEB_NOVEL_SYNONYMS = "freeWebNovelSynonyms";
    private static final String BLOCKED_STRINGS = "blockedStrings";

    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";

    private static final String CHANNEL_ID = "com.jeannius.lightnovelreader.CHANNEL_ID";

    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_POST_NOTIFICATION = 112;
    protected static final String LIGHT_NOVEL_READER = "Light Novel Reader";

    private String test = "https://noveltop.net/novel/birth-of-the-demonic-sword/chapter-2227-2227-respect/";

    private ActivityMainBinding binding;
    private Button speakButton, nextButton, previousButton;
    private EditText urlEditText;
    private EditText fullTextEditText;
    private Map<String, String> novelMap = new HashMap<>();
    private Set<String> freeNovelSynonyms = new HashSet<>();
    private Set<String> blockedStringsSet = new HashSet<>();


    TextToSpeech t1;
    StringBuffer tempText;
    private String currentLink = "";
    String nextLink = "";
    String previousLink = "";
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
        createNotificationChannel();
        createNotification();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
            case R.id.freeWebNovelSynonyms:
                showFreeWebNovelSynonyms();
                break;
            case R.id.blockedStrings:
                showBlockedStrings();
                break;
            case R.id.version:
                showVersion();
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

        currentLink = loadFromLocal(CURRENT_LINK_FILE_NAME, getApplicationContext());
        urlEditText.setText(currentLink);
        nextLink = loadFromLocal(NEXT_LINK_FILE_NAME, getApplicationContext());
        previousLink = loadFromLocal(PREVIOUS_LINK_FILE_NAME, getApplicationContext());
        novelMap = loadNovelMapFromLocal(NOVEL_MAP_FILE_NAME, getApplicationContext());
        freeNovelSynonyms = loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getApplicationContext());
    }

    private void getTextFromWeb() {
        String url = urlEditText.getText().toString();
//        String url = "https://innread.com/novel/shadow-slave/chapter-990.html";
        if (url.isEmpty()) {
            toastUser("URL cannot be empty");
        }
        if (!isValidURL(url)) {
            JeanniusLogger.log("NOT VALID", url);
            toastUser(String.format("%s is not a valid URL", url));
        }
        freeNovelSynonyms.add("freewebnovel.noveleast.com");

        CompletableFuture<WebParserResponse> future = new URLHandler().handleURL(url, freeNovelSynonyms, blockedStringsSet);
        future.thenAccept(webParserResponse -> {

            this.runOnUiThread(() -> {
                // update UI with response

                saveLocally(url, CURRENT_LINK_FILE_NAME, getApplicationContext());
                currentLink = url;

                nextLink = webParserResponse.next;
                if (nextLink != null && !nextLink.toString().isEmpty()) {
                    JeanniusLogger.log("Jeannius next link not empty: " + nextLink);
                    saveLocally(nextLink.toString(), NEXT_LINK_FILE_NAME, getApplicationContext());
                } else {
                    JeanniusLogger.log("jeannius!!! next link is empty");
                }

                previousLink = webParserResponse.prev;
                if (previousLink != null && !previousLink.toString().isEmpty()) {
                    JeanniusLogger.log("Jeannius previous link not empty: " + previousLink);
                    saveLocally(previousLink.toString(), PREVIOUS_LINK_FILE_NAME, getApplicationContext());
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
//        JeanniusLogger.log(temp);
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
            runOnUiThread(() -> {
                toastUser(ex.getMessage());
            });
            return null;
        });


    }

    private void toastUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

    // MENU STUFF
    private void showParsers() {
        DialogFragment newFragment = new ParserDialogFragment();
        newFragment.show(getSupportFragmentManager(), "Parsers");
    }

    private void showNovels() {
        DialogFragment newFragment = new NovelDialogFragment(NOVEL_MAP_FILE_NAME, this);
        newFragment.show(getSupportFragmentManager(), "Novels");
    }

    private void showFreeWebNovelSynonyms() {
        DialogFragment newFragment = new FreeWebNovelSynonymsDialogFragment(FREE_WEB_NOVEL_SYNONYMS, this, "Add a new synonym");
        newFragment.show(getSupportFragmentManager(), "FreeWebNovel Synonyms");
    }

    private void showBlockedStrings() {
        DialogFragment newFragment = new BlockedStringDialogFragment(BLOCKED_STRINGS, this, "Add a new blocked string");
        newFragment.show(getSupportFragmentManager(), "Blocked Strings");
    }

    private void showVersion() {
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            SimpleDialogFragment newFragment = new SimpleDialogFragment("Version", version);
            newFragment.show(getSupportFragmentManager(), "Jeannius");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNovelSelected(String url, String fullText) {
        urlEditText.setText(url);
        fullTextEditText.setText(fullText);
    }

    private void saveTitleCurrentLink(String title, String currentLink) {
        novelMap.put(title, currentLink);
        saveLocally(novelMap, NOVEL_MAP_FILE_NAME, getApplicationContext());
    }

    @Override
    public void reloadSynonyms() {
        this.freeNovelSynonyms = loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getApplicationContext());
    }

    @Override
    public void reloadBlockedStrings() {
        this.blockedStringsSet = loadSetFromLocal(BLOCKED_STRINGS, getApplicationContext());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void createNotification() {
        Intent nextIntent = new Intent(this, MainActivity.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getActivity(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, MainActivity.class);
        prevIntent.setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getActivity(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use a custom layout for the notification
        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        collapsedView.setOnClickPendingIntent(R.id.next_button, nextPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.prev_button, prevPendingIntent);
        collapsedView.setTextViewText(R.id.notification_title, LIGHT_NOVEL_READER);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.a3)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCustomContentView(collapsedView) // Use setCustomContentView for collapsed view
//                .setCustomBigContentView(expandedView)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setAutoCancel(true);
//
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switch (intent.getAction()){
            case ACTION_NEXT:
                JeanniusLogger.log("notification", "executing next");
                executeNext();
                break;
            case ACTION_PREVIOUS:
                JeanniusLogger.log("notification", "executing previous");
                executePrevious();
                break;
            default:
                throw new RuntimeException("Unknown action in notification intent");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_POST_NOTIFICATION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toastUser("Permission granted!");
            }
        }
    }
}


