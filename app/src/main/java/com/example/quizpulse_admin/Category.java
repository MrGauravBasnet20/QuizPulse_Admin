package com.example.quizpulse_admin;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AlertDialogLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Category extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private List<CategoryModel> list;

    private Dialog loadingdialog,categorydialog;

    private RecyclerView recyclerView;

    private ImageView image;
    private EditText category;
    private TextView title;
    private Button add;
    private Uri   selectedimage;
    private String dowbloadurl;
     private  CategoryAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the layout defined in activity_category.xml.
        setContentView(R.layout.activity_category);
        // Toolbar setup for the activity.
        Toolbar toolbar = findViewById(R.id.toobar);
        setSupportActionBar(toolbar);

        // Set the t    itle of the toolbar to "Categories".
        getSupportActionBar().setTitle("Categories");

        // Enable the Up button in the toolbar for navigation.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find the RecyclerView in the layout and assign it to the 'recyclerView' variable.
        recyclerView = findViewById(R.id.rv);
        loadingdialog=new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundcorners));
        loadingdialog.setCancelable(false);

        setCategorydialog();


        // Create a LinearLayoutManager for the RecyclerView to arrange items in a vertical orientation.
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(recyclerView.VERTICAL);

        // Set the LinearLayoutManager to the RecyclerView.
        recyclerView.setLayoutManager(layoutManager);

        // Create a list of CategoryModel objects representing different categories.
       list = new ArrayList<>();


        // Create a CategoryAdapter with the list of categories and set it to the RecyclerView.
        adapter = new CategoryAdapter(list, new CategoryAdapter.Deletelistner() {
            @Override
            public void ondelete(String key,int position) {

               new AlertDialog.Builder(Category.this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Category")
                                .setMessage("Are You Sure To Delete This Category??")
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                             public void onClick(DialogInterface dialogInterface, int i) {

                                                loadingdialog.show();
                                                myRef.child("Categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            myRef.child("Sets").child(list.get(position).getName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        list.remove(position);
                                                                        adapter.notifyDataSetChanged();
                                                                    }
                                                                    else
                                                                    {
                                                                        Toast.makeText(Category.this, "Failed To Delete", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                    loadingdialog.dismiss();
                                                                }
                                                            });

                                                        }
                                                        else {
                                                            Toast.makeText(Category.this, "Failed To Delete", Toast.LENGTH_SHORT).show();
                                                            loadingdialog.dismiss();
                                                        }

                                                    }
                                                });
                                            }
                                        })
                        .setNegativeButton("No",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        recyclerView.setAdapter(adapter);
        loadingdialog.show();
        myRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    // Use snapshot1 to get the value of each child
                    CategoryModel category =new CategoryModel(snapshot1.child("name").getValue().toString(),
                            Integer.parseInt(snapshot1.child("sets").getValue().toString()),
                            snapshot1.child("url").getValue().toString(),
                            snapshot1.getKey()
                            );
                    list.add(category);
                }
                adapter.notifyDataSetChanged();
                loadingdialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Category.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingdialog.dismiss();
                finish();
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    // onOptionsItemSelected is called when an item in the options menu is selected.
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Check if the selected item is the home/up button in the toolbar.
        if (item.getItemId() == R.id.add) {
          //dialog show
            categorydialog.show();

        }
        return super.onOptionsItemSelected(item);
    }
    private void setCategorydialog()
    {
        categorydialog=new Dialog(this);
        categorydialog.setContentView(R.layout.add_category_dialog);
        categorydialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        categorydialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundcorners));
        categorydialog.setCancelable(true);

        image=categorydialog.findViewById(R.id.image);
        category=categorydialog.findViewById(R.id.categoryname);
        add=categorydialog.findViewById(R.id.add);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(gallery,101);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(category.getText()==null)
                    {
                        category.setError("Required");
                        return;
                    }
                    for(CategoryModel model: list)
                    {
                        if(category.getText().toString().equals(model.getName())){
                            category.setError("Already Exists");
                            return;
                        }
                    }
                    if (selectedimage==null)
                    {
                        Toast.makeText(Category.this, "Upload Image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    categorydialog.dismiss();
                    upload_data();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101)
        {
            if(resultCode == RESULT_OK)
            {
             selectedimage = data.getData();
                image.setImageURI(selectedimage);
            }
        }
    }
    private void upload_data()
    {
        loadingdialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        StorageReference image_reference = storageReference.child("Categories").child(selectedimage.getLastPathSegment());

       UploadTask uploadTask = image_reference.putFile(selectedimage);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return image_reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            dowbloadurl=task.getResult().toString();
                            upload_category();
                        }
                        else
                        {
                            loadingdialog.dismiss();
                            Toast.makeText(Category.this, "Somthing Went Wrong....", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    // Handle failures
                    // ...
                    loadingdialog.dismiss();
                    Toast.makeText(Category.this, "Somthing Went Wrong....", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    private  void upload_category()
    {

        Map<String,Object> map = new HashMap <> ();
        map.put("name",category.getText().toString());
        map.put("sets",0);
        map.put("url",dowbloadurl);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference().child("Categories").child("Category"+(list.size()+1)).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    list.add(new CategoryModel(category.getText().toString(),0,dowbloadurl,"Category"+(list.size()+1)));
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(Category.this, "Somthing Went Wrong....", Toast.LENGTH_SHORT).show();
                }
                loadingdialog.dismiss();
            }
        });
    }
}
