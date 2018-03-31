package com.nvite.adapter;


import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nvite.R;
import com.nvite.activity.CreateEventActivity;
import com.nvite.util.SavePref;


import java.util.ArrayList;


/**
 * Created by V on 3/1/2017.
 */

public class SearchLocationAdapter extends RecyclerView.Adapter<SearchLocationAdapter.MyViewHolder> {
    private Context context;
    SavePref savePref;
    ArrayList<String[]> resultList;

    public SearchLocationAdapter(Context context, ArrayList<String[]> resultList) {
        this.context = context;
        savePref = new SavePref(context);
        this.resultList = resultList;
    }


    @Override
    public SearchLocationAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_location_row, parent, false);

        return new SearchLocationAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchLocationAdapter.MyViewHolder holder, final int position) {

        holder.search_location.setText(resultList.get(position)[0]);

        holder.parent_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList.size() > 0)
                    ((CreateEventActivity) context).SetSelectLocation(resultList.get(position)[0], position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultList.size();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout parent_layout;
        public TextView search_location;

        public MyViewHolder(View view) {
            super(view);
            parent_layout = (LinearLayout) view.findViewById(R.id.parent_layout);
            search_location = (TextView) view.findViewById(R.id.search_location);
        }
    }


}
