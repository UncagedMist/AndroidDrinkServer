package kk.techbytecare.androiddrinkserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import kk.techbytecare.androiddrinkserver.Interface.ItemClickListener;
import kk.techbytecare.androiddrinkserver.R;

public class OrderViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txt_order_id,txt_order_price,txt_order_address,txt_order_comment,txt_order_status;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(View itemView) {
        super(itemView);

        txt_order_id = itemView.findViewById(R.id.txt_order_id);
        txt_order_price = itemView.findViewById(R.id.txt_order_price);
        txt_order_address = itemView.findViewById(R.id.txt_order_address);
        txt_order_comment = itemView.findViewById(R.id.txt_order_comment);
        txt_order_status = itemView.findViewById(R.id.txt_order_status);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,false);
    }
}