package kk.techbytecare.androiddrinkserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kk.techbytecare.androiddrinkserver.R;

public class MenuViewHolder extends RecyclerView.ViewHolder {

    public ImageView img_product;
    public TextView txt_product;

    public MenuViewHolder(View itemView) {
        super(itemView);

        img_product = itemView.findViewById(R.id.img_product);
        txt_product = itemView.findViewById(R.id.txt_menu_name);
    }
}
