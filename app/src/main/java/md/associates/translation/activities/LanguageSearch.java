package md.associates.translation.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import md.associates.translation.ItemsModel;
import md.associates.translation.R;
import md.associates.translation.activities.HomeActivity;

public class LanguageSearch extends AppCompatActivity {

    EditText search;
    ListView listView;
    String[] languages = {"Spanish","English","Turkish","Arabic","Hindi","Africaans","Russian","Bengali","Urdu","French","Azerbaijani","Chinese"};
    int[] languageImages = {R.drawable.spanish,R.drawable.english,R.drawable.turkish,R.drawable.arabic,R.drawable.hindi,R.drawable.afrikaans,R.drawable.russian,R.drawable.bengali,R.drawable.urdu,R.drawable.french,R.drawable.azerbaijani,R.drawable.chinese};


    List<ItemsModel> itemsModelList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_search);

    listView = findViewById(R.id.listview);
    search = findViewById(R.id.etSearch);

        for(int i = 0; i < languages.length; i++){

            ItemsModel itemsModel = new ItemsModel(languages[i],languageImages[i]);

            itemsModelList.add(itemsModel);

        }


    CustomAdapter customAdapter = new CustomAdapter(itemsModelList,this);
    listView.setAdapter(customAdapter);
    Intent getIntent = getIntent();



    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = null;

            if(getIntent.getStringExtra("activity").equals("camera")){
                 intent = new Intent(getApplicationContext(), Camera.class);
            } else if(getIntent.getStringExtra("activity").equals("home")){
                 intent = new Intent(getApplicationContext(), HomeActivity.class);
            } else if(getIntent.getStringExtra("activity").equals("conversion")){
                 intent = new Intent(getApplicationContext(), Conversion.class);
            }

            intent.putExtra("language",languages[position]);
            intent.putExtra("value",getIntent.getStringExtra("sendValue"));
            startActivity(intent);
        }
    });

    search.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            customAdapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    });

    }


    private class CustomAdapter extends BaseAdapter implements Filterable{

        private List<ItemsModel> itemsModelsl;
        private List<ItemsModel> itemsModelListFiltered;
        private Context context;

        public CustomAdapter(List<ItemsModel> itemsModelsl, Context context) {
            this.itemsModelsl = itemsModelsl;
            this.itemsModelListFiltered = itemsModelsl;
            this.context = context;
        }

        @Override
        public int getCount() {
            return itemsModelListFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return itemsModelListFiltered.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {


            View view1 = getLayoutInflater().inflate(R.layout.language_data, null);
            TextView language = view1.findViewById(R.id.language);
            ImageView image = view1.findViewById(R.id.flag);

            language.setText(itemsModelListFiltered.get(position).getLanguages());
            image.setImageResource(itemsModelListFiltered.get(position).getImages());
            return view1;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {

                    FilterResults filterResults = new FilterResults();
                    if(constraint == null || constraint.length() == 0){
                        filterResults.count = itemsModelsl.size();
                        filterResults.values = itemsModelsl;

                    }else{
                        List<ItemsModel> resultsModel = new ArrayList<>();
                        String searchStr = constraint.toString();

                        for(ItemsModel itemsModel:itemsModelsl){
                            if(itemsModel.getLanguages().contains(searchStr)){
                                resultsModel.add(itemsModel);

                            }
                            filterResults.count = resultsModel.size();
                            filterResults.values = resultsModel;
                        }


                    }

                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    itemsModelListFiltered = (List<ItemsModel>) results.values;
                    notifyDataSetChanged();

                }
            };
            return filter;
        }
    }
}