package com.example.makeorderserverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.makeorderserverapp.Common.Common;
import com.example.makeorderserverapp.Interface.ItemClickListener;
import com.example.makeorderserverapp.Model.Request;
import com.example.makeorderserverapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

import javax.sql.CommonDataSource;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase database;
    DatabaseReference requests;

    MaterialSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.recyler_item);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder orderViewHolder, final Request request,final int i) {

                orderViewHolder.txtOrderId.setText(adapter.getRef(i).getKey());
                orderViewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(request.getStatus()));
                orderViewHolder.txtOrderAddress.setText(request.getAddress());
                orderViewHolder.txtOrderPhone.setText(request.getPhone());

                orderViewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(i).getKey(), adapter.getItem(i));
                    }
                });

                orderViewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(i).getKey());
                    }
                });

                orderViewHolder.btnDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                        Common.currentRequest = request;
                        startActivity(trackingOrder);

                    }
                });

                orderViewHolder.btnDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = request;
                        orderDetail.putExtra("OrderId", adapter.getRef(i).getKey());
                        startActivity(orderDetail);
                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void deleteOrder(final String key) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Delete Order");
        alertDialog.setMessage("Do you really want to delete this order?");

        alertDialog.setIcon(R.drawable.ic_delete_sweep_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Delete order
                requests.child(key).removeValue();
                adapter.notifyDataSetChanged();
                Toast.makeText(OrderStatus.this,"Shop item was deleted!",Toast.LENGTH_SHORT).show();
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");
        alertDialog.setIcon(R.drawable.ic_update_black_24dp);

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout, null);

        spinner = (MaterialSpinner) view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed","On your way","Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                requests.child(localKey).setValue(item);
                adapter.notifyDataSetChanged(); //To update item size


            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
