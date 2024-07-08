package com.example.quizpulse_admin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List; // Import List

public class questionAdapter extends RecyclerView.Adapter<questionAdapter.ViewHolder> {
    private List<questionModel> list; // Corrected th   e type of list
    private String category;
    private DeleteListener listener;

    public questionAdapter(List<questionModel> list, String category,DeleteListener listener) {
        this.list = list;
        this.category=category;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout and create a ViewHolder
        // Example: View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.your_item_layout, parent, false);
        // return new ViewHolder(view);
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to your ViewHolder
        String question = list.get(position).getQuestion();
        String answer = list.get(position).getAnswer();

        holder.setData(question,answer,position);
    }

    @Override
    public int getItemCount() {
        return list.size(); // Return the size of your data list
    }

    // Corrected the class name to ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView question, answer;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);

        }
        private void setData(String question,String answer,int position)
        {
            this.question.setText(position+1+". "+question);
            this.answer.setText("Ans. "+answer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent= new Intent(itemView.getContext(),AddQuestion.class);
                    intent.putExtra("categoryName",category);
                    intent.putExtra("setNo",list.get(position).getSet());
                    intent.putExtra("position",position);
                    itemView.getContext().startActivity(intent);
                }
            });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onLongCLick(position,list.get(position).getId());
                return false;
            }
        });

        }
    }
    public interface DeleteListener{
        void onLongCLick(int position, String id);
    }
}
