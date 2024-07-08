package com.example.quizpulse_admin;

import static android.app.ProgressDialog.show;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import android.Manifest;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class QuestionsActivity extends AppCompatActivity {

    private Button add, excel;
    private RecyclerView r_v;
    private questionAdapter adapter;
   public static List<questionModel> list;
    private Dialog loadingdialog;
    private DatabaseReference myRef;
    public static final int CELL_COUNT = 6;
    private  int set;
    private  String categoryName;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);


        Toolbar toolbar = findViewById(R.id.toobar); // Corrected ID for the Toolbar
        setSupportActionBar(toolbar);

         categoryName = getIntent().getStringExtra("category");
         set = getIntent().getIntExtra("setNo", 1);
        getSupportActionBar().setTitle(categoryName + "/set" + set);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingdialog=new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundcorners));
        loadingdialog.setCancelable(false);
        loadingText = loadingdialog.findViewById(R.id.loading);

        myRef=FirebaseDatabase.getInstance().getReference();
        add = findViewById(R.id.add);
        excel = findViewById(R.id.excel);
        r_v = findViewById(R.id.r_v);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        r_v.setLayoutManager(layoutManager);
         list = new ArrayList<>();
        adapter = new questionAdapter(list, categoryName, new questionAdapter.DeleteListener() {
            @Override
            public void onLongCLick(int position, final String id) {
                new AlertDialog.Builder(QuestionsActivity.this, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are You Sure To Delete This Question?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                loadingdialog.show();
                                    myRef.child("Sets").child(categoryName).child("questions").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                                list.remove(position);
                                                adapter.notifyItemRemoved(position);

                                        }
                                        else {
                                            Toast.makeText(QuestionsActivity.this, "Failed To Delete", Toast.LENGTH_SHORT).show();

                                        }
                                        loadingdialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        r_v.setAdapter(adapter);
         getData(categoryName,set);
         add.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent=new Intent(QuestionsActivity.this,AddQuestion.class);
                 intent.putExtra("categoryName",categoryName);
                 intent.putExtra("setNo",set);
                 startActivity(intent);
             }
         });
         excel.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                                selectFile();
                 }
                 else {
                     ActivityCompat.requestPermissions(QuestionsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
                 }
             }
         });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectFile();
            }
            else{
                Toast.makeText(this, "Please Grant The Permission !!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void selectFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select FIle"),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 102 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            if(uri != null){
                DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
                if(documentFile != null && documentFile.getName() != null && documentFile.getName().endsWith(".xlsx")){
                    String filePath = data.getData().getPath();
                    readFile(uri);

                } else {
                    Toast.makeText(this, "Please select an Excel file", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve file URI", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void getData( String categoryname,int set){
        loadingdialog.show();
        myRef
                .child("Sets").child(categoryname)
                .child("questions").orderByChild("setNo")
                .equalTo(set).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot1:snapshot.getChildren())
                        {
                            String id=dataSnapshot1.getKey();
                            String question = dataSnapshot1.child("question").getValue().toString();
                            String a = dataSnapshot1.child("optionA").getValue().toString();
                            String b = dataSnapshot1.child("optionB").getValue().toString();
                            String c = dataSnapshot1.child("optionC").getValue().toString();
                            String d = dataSnapshot1.child("optionD").getValue().toString();
                            String correctAns = dataSnapshot1.child("correctANS").getValue().toString();

                            list.add(new questionModel(id,question,a,b,c,d,correctAns,set));
                        }
                        loadingdialog.dismiss();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuestionsActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        loadingdialog.dismiss();
                    }
                });

    }
    private void readFile(Uri fileUri){
        loadingText.setText("Scanning Questions....");
        loadingdialog.show();



                HashMap<String, Object> parentmap = new HashMap<>();
                List<questionModel> tempList = new ArrayList<>();
                try{
                    InputStream inputStream = getContentResolver().openInputStream(fileUri);
                    XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    XSSFSheet sheet = workbook.getSheetAt(0);
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    int rowscount = sheet.getPhysicalNumberOfRows();
                    if ( rowscount > 0){
                        for (int r =0; r<rowscount; r++) {
                            Row row = sheet.getRow(r);
                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {
                                String question = getCellData(row, 0, formulaEvaluator); // First cell for the question
                                String a = getCellData(row, 1, formulaEvaluator); // Second cell for option A
                                String b = getCellData(row , 2, formulaEvaluator); // Third cell for option B
                                String c = getCellData(row, 3, formulaEvaluator); // Fourth cell for option C
                                String d = getCellData(row, 4, formulaEvaluator); // Fifth cell for option D
                                String correctAns = getCellData(row, 5, formulaEvaluator); // Sixth cell for correct answer


                                if(correctAns.equals(a) || correctAns.equals(b) || correctAns.equals(c) || correctAns.equals(d)){

                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question", question);
                                    questionMap.put("optionA", a);
                                    questionMap.put("optionB", b);
                                    questionMap.put("optionC", c);
                                    questionMap.put("optionD", d);
                                    questionMap.put("correctANS", correctAns);
                                    questionMap.put("setNo", set);

                                    String id =UUID.randomUUID().toString();
                                    parentmap.put(id, questionMap);
                                    tempList.add(new questionModel(id,question,a,b,c,d,correctAns,set));

                                }
                                else{
                                    loadingText.setText("Loading......");
                                    loadingdialog.dismiss();
                                    Toast.makeText(QuestionsActivity.this, "Row no. " + (r+1 ) + " has no correct option", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                loadingText.setText("Loading......");
                                loadingdialog.dismiss();
                                Toast.makeText(QuestionsActivity.this, "Row no. "+ (r+1) +" has incorrect data", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        loadingText.setText("Uploading....");
                        FirebaseDatabase.getInstance().getReference()
                                .child("Sets").child(categoryName)
                                .child("questions").updateChildren(parentmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            list.addAll(tempList);
                                            adapter.notifyDataSetChanged();

                                        }else{
                                            loadingText.setText("Loading......");
                                            loadingdialog.dismiss();
                                            Toast.makeText(QuestionsActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                                        }
                                        loadingdialog.dismiss();
                                    }
                                });

                    }else{
                        loadingText.setText("Loading......");
                        loadingdialog.dismiss();
                        Toast.makeText(QuestionsActivity.this, "File is Empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                catch (FileNotFoundException e){
                    e.printStackTrace();
                    loadingText.setText("Loading......");
                    loadingdialog.dismiss();
                    Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    loadingText.setText("Loading......");
                    loadingdialog.dismiss();
                    Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

    }



    private String getCellData(Row row, int cellPosition, FormulaEvaluator formulaEvaluator) {
        String value = "";
        Cell cell = row.getCell(cellPosition);

            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    return value + cell.getBooleanCellValue();
                case Cell.CELL_TYPE_NUMERIC:
                    return value + cell.getNumericCellValue();
                case Cell.CELL_TYPE_STRING:
                    return value + cell.getStringCellValue();
                default:
                    return value;
            }

    }




    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }
}
