package md.associates.translation.activities;

import static md.associates.translation.CommonFiles.getLanguageCode;
import static md.associates.translation.CommonFiles.translateText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

import md.associates.translation.R;

public class Camera extends AppCompatActivity {

    private ImageView fromCamera;
    private TextView fromText, from, to, toText;
    private Uri imageUri;
    private TextRecognizer textRecognizer;

    String fromLanguage, toLanguage, value;
    int fromLanguageCode,toLanguageCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        fromCamera = findViewById(R.id.fromCamera);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);

        fromText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                toText.setText("");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(fromText.getText().length() >= 0) {
                    translateText(fromLanguageCode,toLanguageCode,fromText.getText().toString(), Camera.this,toText);
                }
            }
        });


        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        fromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Camera.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode != Activity.RESULT_OK){
            if(data!=null){
                imageUri = data.getData();
                Toast.makeText(this, "Image selected.", Toast.LENGTH_SHORT).show();
                extractText();
            }
        }else {
            Toast.makeText(this, "Image not selected.", Toast.LENGTH_SHORT).show();
        }
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
                Intent intent = new Intent(Camera.this, LanguageSearch.class);
                intent.putExtra("sendValue","from");
                intent.putExtra("activity","camera");
                startActivity(intent);
            }
        });


        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Camera.this, LanguageSearch.class);
                intent.putExtra("sendValue","to");
                intent.putExtra("activity","camera");
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

        fromLanguage = String.valueOf(from.getText());
        if (fromLanguage != null) {
            fromLanguageCode = getLanguageCode(fromLanguage);
        }
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


    private void extractText() {
        if(imageUri != null){
            try {
                InputImage inputImage = InputImage.fromFilePath(Camera.this,imageUri);

                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String extractText = text.getText();
                                fromText.setText(extractText);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Camera.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void back(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}