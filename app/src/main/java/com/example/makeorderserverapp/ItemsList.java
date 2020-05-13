package com.example.makeorderserverapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.makeorderserverapp.Common.Common;
import com.example.makeorderserverapp.Interface.ItemClickListener;
import com.example.makeorderserverapp.Model.ShopItem;
import com.example.makeorderserverapp.ViewHolder.ShopItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import java.util.UUID;

public class ItemsList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout rootLayout;

    FloatingActionButton fab;

    //Firebase
    FirebaseDatabase database;
    DatabaseReference itemsList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";

    FirebaseRecyclerAdapter<ShopItem, ShopItemViewHolder> adapter;

    //Add New Shop Item
    MaterialEditText edtName, edtDescription, edtPrice, edtDiscount;
    Button btnSelect, btnUpload;
    Uri saveUri;

    ShopItem newShopItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);

        //Firebase
        database = FirebaseDatabase.getInstance();
        itemsList = database.getReference("Shop");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.recyler_item);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddShopItemDialog();
            }
        });

        if(getIntent() != null)
        {
           categoryId = getIntent().getStringExtra("CategoryId");
        }

        if(!categoryId.isEmpty())
        {
            loadListItems(categoryId);
        }
    }

    private void showAddShopItemDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ItemsList.this);
        alertDialog.setTitle("Add new Shop Item");
        //alertDialog.setMessage("Please fill all fields!");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_category_layout = inflater.inflate(R.layout.add_new_shop_item_layout,null);

        edtName = add_category_layout.findViewById(R.id.edtName);
        edtDescription = add_category_layout.findViewById(R.id.edtDescription);
        edtPrice = add_category_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_category_layout.findViewById(R.id.edtDiscount);

        btnSelect = add_category_layout.findViewById(R.id.btnSelect);
        btnUpload = add_category_layout.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from Gallery
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_category_layout);
        alertDialog.setIcon(R.drawable.ic_add_circle_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                //Adding new Category
                if(newShopItem != null)
                {
                    itemsList.push().setValue(newShopItem);
                    Snackbar.make(rootLayout, newShopItem.getName()+" item was added!", Snackbar.LENGTH_SHORT)
                            .show();
                }


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

    private void loadListItems(String categoryId) {

        adapter = new FirebaseRecyclerAdapter<ShopItem, ShopItemViewHolder>(
                ShopItem.class,
                R.layout.shop_item,
                ShopItemViewHolder.class,
                itemsList.orderByChild("catID").equalTo(categoryId)
        ) {
            @Override
            protected void populateViewHolder(ShopItemViewHolder shopItemViewHolder, ShopItem shopItem, int i) {
                    shopItemViewHolder.item_name.setText(shopItem.getName());

                Glide.with(getBaseContext())
                        .load(shopItem.getImage())
                        .centerCrop()
                        .into(shopItemViewHolder.item_image);

                shopItemViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //CODE HERE
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    private void uploadImage() {

        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();
            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ItemsList.this, "Picture uploaded!",Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //new Shop Item
                                    newShopItem = new ShopItem();
                                    newShopItem.setName(edtName.getText().toString());
                                    newShopItem.setDescription(edtDescription.getText().toString());
                                    newShopItem.setPrice(edtPrice.getText().toString());
                                    newShopItem.setDiscount(edtDiscount.getText().toString());
                                    newShopItem.setCatID(categoryId);
                                    newShopItem.setImage(uri.toString());

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ItemsList.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            saveUri = data.getData();
            btnSelect.setText("Image Selected");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdateShopItemDialog(adapter.getRef(item.getOrder()).getKey(),
                    adapter.getItem(item.getOrder()));
        } else if(item.getTitle().equals(Common.DELETE)){
             deleteShopItem(adapter.getRef(item.getOrder()).getKey());
        }


        return super.onContextItemSelected(item);
    }

    private void deleteShopItem(final String key) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ItemsList.this);
        alertDialog.setTitle("Delete Shop Item");
        alertDialog.setMessage("Do you really want to delete this shop item?");

        alertDialog.setIcon(R.drawable.ic_delete_sweep_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Delete category
                itemsList.child(key).removeValue();
                Toast.makeText(ItemsList.this,"Shop item was deleted!",Toast.LENGTH_SHORT).show();
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

    private void showUpdateShopItemDialog(final String key, final ShopItem item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ItemsList.this);
        alertDialog.setTitle("Update Shop Item");
        //alertDialog.setMessage("Please fill all fields!");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_shop_item_layout = inflater.inflate(R.layout.add_new_shop_item_layout,null);

        edtName = add_shop_item_layout.findViewById(R.id.edtName);
        edtDescription = add_shop_item_layout.findViewById(R.id.edtDescription);
        edtPrice = add_shop_item_layout.findViewById(R.id.edtPrice);
        edtDiscount = add_shop_item_layout.findViewById(R.id.edtDiscount);

        //set Default value for edit view
        edtName.setText(item.getName());
        edtDescription.setText(item.getDescription());
        edtPrice.setText(item.getPrice());
        edtDiscount.setText(item.getDiscount());
        item.setCatID(categoryId);

        btnSelect = add_shop_item_layout.findViewById(R.id.btnSelect);
        btnUpload = add_shop_item_layout.findViewById(R.id.btnUpload);

        //Event for button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select image from Gallery
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_shop_item_layout);
        alertDialog.setIcon(R.drawable.ic_update_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                    //Updating info
                    item.setName(edtName.getText().toString());
                    item.setPrice(edtPrice.getText().toString());
                    item.setDiscount(edtDiscount.getText().toString());
                    item.setDescription(edtDescription.getText().toString());

                    //adapter.notifyDataSetChanged();
                    itemsList.child(key).setValue(item);

                    Snackbar.make(rootLayout, "Shop Item "+item.getName()+" was edited!", Snackbar.LENGTH_SHORT)
                            .show();
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

    private void changeImage(final ShopItem item) {

        if(saveUri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ItemsList.this, "Picture uploaded!",Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    item.setImage(uri.toString());

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ItemsList.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    mDialog.setMessage("Uploaded "+progress+"%");
                }
            });

        }
    }
}
