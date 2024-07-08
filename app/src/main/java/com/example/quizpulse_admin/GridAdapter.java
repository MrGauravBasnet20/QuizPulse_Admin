package com.example.quizpulse_admin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GridAdapter extends BaseAdapter {

  public int sets=0;
    private String category;
    private GridListner listner;
    // Constructor to set the number of sets
    public GridAdapter(int sets, String category, GridListner listner) {
        this.sets = sets;
        this.category=category;
        this.listner=listner;
    }



    @Override
    public int getCount() {
        return sets+1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView,final ViewGroup viewGroup) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.setitem, viewGroup, false);
        } else {
            view = convertView;
        }
        if(i==0)
        {
            ((TextView) view.findViewById(R.id.text)).setText(String.valueOf("+"));
        }else {
            ((TextView) view.findViewById(R.id.text)).setText(String.valueOf(i));
        }
       view.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(i==0)
               {
                   listner.addset();
               }
               else {
                   Intent intent=new Intent(viewGroup.getContext(),QuestionsActivity.class);
                   intent.putExtra("category",category);
                   intent.putExtra("setNo",i);
                   viewGroup.getContext().startActivity(intent);
               }
           }
       });

        return view;
    }
    public interface GridListner{
        public void addset();
    }
}
