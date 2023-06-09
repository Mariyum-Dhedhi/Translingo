package md.associates.translation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Favourites_Adapter extends RecyclerView.Adapter<MyViewHolderFav> {
    private Context context;
    private List<DataClass> dataList;
    public Favourites_Adapter(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolderFav onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourites_data, parent, false);
        return new MyViewHolderFav(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolderFav holder, int position) {
        holder.favouriteText.setText(dataList.get(position).getGeneratedText());
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
class MyViewHolderFav extends RecyclerView.ViewHolder{
    TextView favouriteText;
    public MyViewHolderFav(@NonNull View itemView) {
        super(itemView);
        favouriteText = itemView.findViewById(R.id.favouriteText);

    }
}
