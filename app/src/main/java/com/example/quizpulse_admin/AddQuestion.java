package com.example.quizpulse_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.UUID;

public class AddQuestion extends AppCompatActivity {
    private EditText question;
    private RadioGroup options;
    private LinearLayout answers;
    private Button addbtn;
    private String categoryName;
    private int setNo , position;
    private Dialog loadingdialog;
    private questionModel questionmodel;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("ADD Question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingdialog=new Dialog(this);
        loadingdialog.setContentView(R.layout.loading);
        loadingdialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingdialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.roundcorners));
        loadingdialog.setCancelable(false);
        question=findViewById(R.id.editTextText3);
        options = findViewById(R.id.radio);
        answers=findViewById(R.id.answers);
        addbtn=findViewById(R.id.button);

        categoryName=getIntent().getStringExtra("categoryName");
        setNo=getIntent().getIntExtra("setNo",-1);
        position=getIntent().getIntExtra("position",-1);


        if(setNo==-1 ){
            finish();
            return;
        }
        if(position != -1)
        {
            questionmodel = QuestionsActivity.list.get(position);
            setData();
        }

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (question.getText().toString().isEmpty()){
                    question.setError("Please Enter a Question");
                    return;
                }
                upload();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setData(){

        question.setText(questionmodel.getQuestion());
        ((EditText)answers.getChildAt(0)).setText(questionmodel.getA());
        ((EditText)answers.getChildAt(1)).setText(questionmodel.getB());
        ((EditText)answers.getChildAt(2)).setText(questionmodel.getC());
        ((EditText)answers.getChildAt(3)).setText(questionmodel.getD());

        for (int i = 0; i<answers.getChildCount(); i++)
        {
            if(((EditText)answers.getChildAt(i)).getText().toString().equals(questionmodel.getAnswer())){
                RadioButton radioButton = (RadioButton) options.getChildAt(i);
                radioButton.setChecked(true);
                break;
            }
        }
    }
    private void upload() {
        int correct = -1;
        for (int i = 0; i < options.getChildCount(); i++) {
            EditText answer = (EditText) answers.getChildAt(i);
            if (answer.getText().toString().isEmpty()) {
                answer.setError("Required");
                return;
            }

            RadioButton radioButton = (RadioButton) options.getChildAt(i);
            if (radioButton.isChecked()) {
                correct = i;
                break;
            }
        }
        if (correct == -1) {
            Toast.makeText(this, "Please Select The Correct Answer", Toast.LENGTH_LONG).show();
            return;
        }

        final HashMap<String, Object> map = new HashMap<>();
        map.put("correctANS", ((EditText) answers.getChildAt(correct)).getText().toString());
        map.put("optionD", ((EditText) answers.getChildAt(3)).getText().toString());
        map.put("optionC", ((EditText) answers.getChildAt(2)).getText().toString());
        map.put("optionB", ((EditText) answers.getChildAt(1)).getText().toString());
        map.put("optionA", ((EditText) answers.getChildAt(0)).getText().toString());
        map.put("question", question.getText().toString());
        map.put("setNo", setNo);

        final String id;
        if (position != -1) {
            id = questionmodel.getId();
        } else {
            id = UUID.randomUUID().toString();
        }

        final String questionId = id;
        loadingdialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child("Sets").child(categoryName)
                .child("questions").child(questionId)
                .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Check if updating an existing question
                            if (position != -1) {
                                // If position is not -1, it means you are updating an existing question
                                questionModel questionModel = new questionModel(questionId,
                                        map.get("question").toString(),
                                        map.get("optionA").toString(),
                                        map.get("optionB").toString(),
                                        map.get("optionC").toString(),
                                        map.get("optionD").toString(),
                                        map.get("correctANS").toString(),
                                        (int) map.get("setNo"));

                                QuestionsActivity.list.set(position, questionModel);
                            } else {
                                // If position is -1, it means you are adding a new question
                                questionModel questionModel = new questionModel(questionId,
                                        map.get("question").toString(),
                                        map.get("optionA").toString(),
                                        map.get("optionB").toString(),
                                        map.get("optionC").toString(),
                                        map.get("optionD").toString(),
                                        map.get("correctANS").toString(),
                                        (int) map.get("setNo"));

                                QuestionsActivity.list.add(questionModel);
                            }

                            finish();
                        } else {
                            Toast.makeText(AddQuestion.this, "Something Went Wrong", Toast.LENGTH_LONG).show();
                        }
                        loadingdialog.dismiss();
                    }
                });
    }

}