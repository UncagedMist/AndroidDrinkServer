package kk.techbytecare.androiddrinkserver.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import kk.techbytecare.androiddrinkserver.Model.Category;
import kk.techbytecare.androiddrinkserver.R;
import kk.techbytecare.androiddrinkserver.ViewHolder.MenuViewHolder;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    Context context;
    List<Category> categoryList;

    public MenuAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.menu_item_layout,parent,false);

        return new MenuViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {

        Picasso.with(context)
                .load(categoryList.get(position).Link)
                .into(holder.img_product);

        holder.txt_product.setText(categoryList.get(position).Name);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
