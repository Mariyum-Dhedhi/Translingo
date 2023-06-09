package md.associates.translation.activities;

import static md.associates.translation.CommonFiles.getLanguageCode;
import static md.associates.translation.CommonFiles.translateText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

import md.associates.translation.CommonFiles;
import md.associates.translation.DataClass;
import md.associates.translation.R;

public class HomeActivity extends AppCompatActivity {

    ClipboardManager clipboardManager;
    TextToSpeech textToSpeech;

    private EditText sourceText;
    private View mic, paste;
    private ImageView copy,sourceSpeaker,destinationSpeaker,save, favourites;
    private TextView destinationText, sourceIndex, destinationIndex, from, to;
    String fromLanguage, toLanguage, value;


    protected static final int REQUEST_PERMISSION_CODE = 1;
    int fromLanguageCode,toLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sourceText = findViewById(R.id.sourceText);
        destinationText = findViewById(R.id.destinationText);
        mic = findViewById(R.id.mic);
        sourceIndex = findViewById(R.id.sourceIndex);
        destinationIndex = findViewById(R.id.destinationIndex);
        paste = findViewById(R.id.paste);
        copy = findViewById(R.id.copy);
        save = findViewById(R.id.save);
        favourites = findViewById(R.id.favouriteIcon);
        sourceSpeaker = findViewById(R.id.sourceSpeaker);
        destinationSpeaker = findViewById(R.id.destinationSpeaker);

        from = findViewById(R.id.from);
        to = findViewById(R.id.toLanguage);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = destinationText.getText().toString();
                if (!text.equals("")) {
                    ClipData clipData = ClipData.newPlainText("text", text);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(HomeActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                    paste.setEnabled(true);
                }
            }
        });

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                sourceText.setText(item.getText().toString());
                Toast.makeText(HomeActivity.this, "Pasted", Toast.LENGTH_SHORT).show();
            }
        });


        sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                destinationText.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(sourceText.getText().length() >= 0) {
                    translateText(fromLanguageCode,toLanguageCode,sourceText.getText().toString(), HomeActivity.this,destinationText);
                }
            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(HomeActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData("History");
            }
        });

        favourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadData("Favourites");
            }
        });

        textToSpeech = new TextToSpeech (this, new TextToSpeech.OnInitListener() {
            @Override
        public void onInit(int i) {
                if (i != TextToSpeech.ERROR)
                    textToSpeech.setLanguage(Locale.getDefault());
            }
        });
        sourceSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = sourceText.getText().toString();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        destinationSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = destinationText.getText().toString();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

    }



    @Override
    protected void onResume() {
        super.onResume();
        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString("from", "");
        String s2 = sh.getString("to", "");

        from.setText(s1);
        to.setText(s2);

        from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LanguageSearch.class);
                intent.putExtra("sendValue","from");
                intent.putExtra("activity","home");
                startActivity(intent);
            }
        });


        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LanguageSearch.class);
                intent.putExtra("sendValue","to");
                intent.putExtra("activity","home");
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        value = intent.getStringExtra("value");

        if(value != null) {
            if (value.equals("from")) {
                from.setText(intent.getStringExtra("language"));
            }

            if(value.equals("to")) {
                to.setText(intent.getStringExtra("language"));
            }
            onPause();
        }

        toLanguage = String.valueOf(to.getText());
        if (toLanguage != null) {
            toLanguageCode = getLanguageCode(toLanguage);
        }
        destinationIndex.setText(toLanguage);

        fromLanguage = String.valueOf(from.getText());
        if (fromLanguage != null) {
            fromLanguageCode = getLanguageCode(fromLanguage);
        }
        sourceIndex.setText(fromLanguage);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Creating a shared pref object with a file name "MySharedPref" in private mode
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putString("from", from.getText().toString());
        myEdit.putString("to", to.getText().toString());
        myEdit.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceText.setText(res.get(0));
            }
        }
    }

    public void uploadData(String reference){
        String generatedText = destinationText.getText().toString();
            DataClass dataClass = new DataClass(generatedText);
            FirebaseDatabase.getInstance().getReference(reference).push()
                    .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    public void history(View view) {
        Intent intent = new Intent(this, History.class);
        startActivity(intent);
    }

    public void favourites(View view) {
        Intent intent = new Intent(this, Favourites.class);
        startActivity(intent);
    }

    public void camera(View view) {
        Intent intent = new Intent(this, Camera.class);
        startActivity(intent);
    }

    public void conversion(View view) {
        Intent intent = new Intent(this, Conversion.class);
        startActivity(intent);
    }
}