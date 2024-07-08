package com.example.quizpulse_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SetsActivity extends AppCompatActivity {
    private GridView gridView;
    private Dialog loadingdialog;
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        Toolbar toolbar = findViewById(R.id.toobar);
        gridView = findViewById(R.id.gridview);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Available Sets");
        loadingdialog=new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundcorners));
        loadingdialog.setCancelable(false);

        // Use the correct variable name

         gridAdapter = new GridAdapter(getIntent().getIntExtra("sets", 0), getIntent().getStringExtra("title"), new GridAdapter.GridListner() {
             // ...

             public void addset() {
                 loadingdialog.show();

                 final DatabaseReference categoryReference = FirebaseDatabase.getInstance().getReference().child("Categories").child(getIntent().getStringExtra("key"));

                 categoryReference.child("sets").addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if (dataSnapshot.exists()) {
                             int currentSets = dataSnapshot.getValue(Integer.class);
                             int newSets = currentSets + 1;

                             categoryReference.child("sets").setValue(newSets).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()) {
                                         gridAdapter.sets++;
                                         gridAdapter.notifyDataSetChanged();
                                     } else {
                                         // Handle the failure case
                                     }
                                     loadingdialog.dismiss();
                                 }
                             });
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                         // Handle the error
                         loadingdialog.dismiss();
                     }
                 });
             }
// ...

         });
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
