package md.associates.translation.activities;

import static md.associates.translation.CommonFiles.getLanguageCode;
import static md.associates.translation.CommonFiles.translateText;
import static md.associates.translation.activities.HomeActivity.REQUEST_PERMISSION_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import md.associates.translation.ConversationModel;
import md.associates.translation.ItemsModel;
import md.associates.translation.R;

public class Conversion extends AppCompatActivity {

    ListView listView;
    ArrayList<String> fromList = new ArrayList<String>();
    ArrayList<String> toList = new ArrayList<String>();
    ArrayList<String> sideCount = new ArrayList<String>();


    String fromLanguage, toLanguage, value, side;
    int fromLanguageCode,toLanguageCode;

    private TextView from, to;
    private ImageView fromMicrophone, toMicrophone;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        listView = findViewById(R.id.listview);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);
        fromMicrophone = findViewById(R.id.fromMicrophone);
        toMicrophone = findViewById(R.id.toMicrophone);


        fromMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    side = "from";
                    sideCount.add(side);
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(Conversion.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        toMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    side = "to";
                    sideCount.add(side);
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(Conversion.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                fromList.add(res.get(0));
                if(side.equals("from")) {
                    translateText(fromLanguageCode, toLanguageCode, res.get(0), Conversion.this, toList);
                } else if(side.equals("to")) {
                    translateText(toLanguageCode, fromLanguageCode, res.get(0), Conversion.this, toList);
                }
                handler.postDelayed(task,1500);
            }
        }
    }

    private Runnable task = new Runnable() {
        @Override
        public void run() {
            CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), fromList, toList, sideCount);
            listView.setAdapter(customAdapter);
        }
    };

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
                Intent intent = new Intent(Conversion.this, LanguageSearch.class);
                intent.putExtra("sendValue","from");
                intent.putExtra("activity","conversion");
                startActivity(intent);
            }
        });


        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Conversion.this, LanguageSearch.class);
                intent.putExtra("sendValue","to");
                intent.putExtra("activity","conversion");
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


    public void back(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    public static void translateText(int fromLanguageCode, int toLanguageCode, String source, Activity activity,ArrayList toList){
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        toList.add(s);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity,"Fail to translate : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity,"Fail to download language model : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CustomAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String> fromList;
        private ArrayList<String> toList;
        private ArrayList<String> side;
        private LayoutInflater layoutInflater;

        public CustomAdapter(Context context, ArrayList<String> fromList, ArrayList<String> toList, ArrayList<String> side) {
            this.context = context;
            this.fromList = fromList;
            this.toList = toList;
            this.side = side;
            layoutInflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return fromList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(side.get(position).equals("from")) {
                convertView = layoutInflater.inflate(R.layout.conversation_item, null);
            }else if(side.get(position).equals("to")) {
                convertView = layoutInflater.inflate(R.layout.conversation_item_to, null);
            }
            TextView fromText = (TextView) convertView.findViewById(R.id.fromText);
            TextView toText = (TextView) convertView.findViewById(R.id.toText);
            fromText.setText(fromList.get(position));
            toText.setText(toList.get(position));

            return convertView;
        }
    }
}

