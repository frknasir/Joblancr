package com.joblancr.activitiesAndAdapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joblancr.cards.SelectCategoryCard;

import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class SelectCategoryAdapter extends RecyclerView.Adapter<SelectCategoryAdapter.SelectCategoryViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    public SelectCategoryAdapter(Context context) {
        this.context = context;
    }

    public static class SelectCategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cv_select_category;
        ImageView category_image;
        TextView category_name;
        TextView category_id;

        SelectCategoryViewHolder(View itemView) {
            super(itemView);
            cv_select_category = (CardView) itemView.findViewById(R.id.cv_select_category);
            category_image = (ImageView) itemView.findViewById(R.id.category_image);
            category_name = (TextView) itemView.findViewById(R.id.category_name);
            category_id = (TextView) itemView.findViewById(R.id.category_id);
        }
    }

    List<SelectCategoryCard> categoryCards;

    SelectCategoryAdapter(List<SelectCategoryCard> categoryCards){
        this.categoryCards = categoryCards;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public SelectCategoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select_category_card, viewGroup, false);
        SelectCategoryViewHolder pvh = new SelectCategoryViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final SelectCategoryViewHolder sCViewHolder, int i) {
        sCViewHolder.category_image.setImageResource(categoryCards.get(i).getPhotoId());
        sCViewHolder.category_name.setText(categoryCards.get(i).getName());
        sCViewHolder.category_id.setText(""+categoryCards.get(i).getId());

        sCViewHolder.cv_select_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), BrowseActivity.class);
                i.putExtra("cat_id", sCViewHolder.category_id.getText());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryCards.size();
    }
}
