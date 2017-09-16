package com.joblancr.activitiesAndAdapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.joblancr.cards.Project;

import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class PRVAdapter extends RecyclerView.Adapter<PRVAdapter.ProjectViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    //public PRVAdapter(Context context) { this.context = context; }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView title;
        TextView price;
        TextView description;
        TextView time;
        TextView bidCount;
        TextView place;
        TextView id;
        TextView user_id;
        TextView project_status;
        TextView project_status_value;

        ProjectViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            title = (TextView)itemView.findViewById(R.id.project_title);
            price = (TextView)itemView.findViewById(R.id.project_price);
            description = (TextView)itemView.findViewById(R.id.project_description);
            time = (TextView)itemView.findViewById(R.id.project_time_text);
            bidCount = (TextView)itemView.findViewById(R.id.project_bid_text);
            place = (TextView)itemView.findViewById(R.id.project_place_text);
            id = (TextView)itemView.findViewById(R.id.project_id);
            user_id = (TextView)itemView.findViewById(R.id.project_user_id);
            project_status = (TextView)itemView.findViewById(R.id.project_status);
            project_status_value = (TextView)itemView.findViewById(R.id.project_status_value);
        }
    }

    List<Project> projects;

    public PRVAdapter(List<Project> projects){
        this.projects = projects;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.project_card, viewGroup, false);
        ProjectViewHolder pvh = new ProjectViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder projectViewHolder, int i) {
        projectViewHolder.title.setText(projects.get(i).getTitle());
        projectViewHolder.price.setText(projects.get(i).getBudget());
        projectViewHolder.description.setText(projects.get(i).getDescription());
        projectViewHolder.time.setText(projects.get(i).getTime());
        projectViewHolder.bidCount.setText(projects.get(i).getBid());
        projectViewHolder.place.setText(projects.get(i).getLocation() );
        projectViewHolder.id.setText(projects.get(i).getId());
        projectViewHolder.user_id.setText(projects.get(i).getUserId());
        projectViewHolder.project_status_value.setText(""+projects.get(i).getStatus());

        if(projects.get(i).getStatus() == 1) {
            projectViewHolder.project_status.setText("Open");
        } else if(projects.get(i).getStatus() == 0) {
            projectViewHolder.project_status.setText("Closed");
        }

        projectViewHolder.cv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProjectViewActivity.class);
                intent.putExtra("project_title", projectViewHolder.title.getText());
                intent.putExtra("project_id", projectViewHolder.id.getText());
                intent.putExtra("project_user_id", projectViewHolder.user_id.getText());
                intent.putExtra("project_status", projectViewHolder.project_status_value.getText());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }
}
