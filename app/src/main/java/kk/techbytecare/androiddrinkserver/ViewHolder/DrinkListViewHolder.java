package kk.techbytecare.androiddrinkserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kk.techbytecare.androiddrinkserver.Interface.ItemClickListener;
import kk.techbytecare.androiddrinkserver.R;

public class DrinkListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ImageView img_product;
    public TextView txt_drink_name,txt_price;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public DrinkListViewHolder(View itemView) {
        super(itemView);

        img_product = itemView.findViewById(R.id.img_product);
        txt_drink_name = itemView.findViewById(R.id.txt_drink_name);
        txt_price = itemView.findViewById(R.id.txt_price);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,false);
    }
}
