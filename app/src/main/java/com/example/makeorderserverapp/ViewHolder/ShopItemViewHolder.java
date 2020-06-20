package com.example.makeorderserverapp.ViewHolder;


import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makeorderserverapp.Common.Common;
import com.example.makeorderserverapp.Interface.ItemClickListener;
import com.example.makeorderserverapp.R;


public class ShopItemViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
{
    public TextView item_name;
    public ImageView item_image;

    private ItemClickListener itemClickListener;

    public ShopItemViewHolder(@NonNull View itemView) {
        super(itemView);

        item_name = (TextView) itemView.findViewById(R.id.item_name);
        item_image = (ImageView) itemView.findViewById(R.id.item_image);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(), false);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select action");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(),Common.DELETE);

    }
}
