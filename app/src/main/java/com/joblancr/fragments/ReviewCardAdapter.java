package com.joblancr.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.joblancr.activitiesAndAdapters.ProfileActivity;
import com.joblancr.activitiesAndAdapters.R;
import com.joblancr.cards.ReviewCard;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class ReviewCardAdapter extends RecyclerView.Adapter<ReviewCardAdapter.ReviewViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    public ReviewCardAdapter(Context context) {
        this.context = context;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView name;
        RatingBar rating;
        TextView time;
        TextView review_comment;

        ReviewViewHolder(View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.person_photo);
            name = (TextView) itemView.findViewById(R.id.person_name);
            rating = (RatingBar) itemView.findViewById(R.id.ratingBar);
            time = (TextView) itemView.findViewById(R.id.review_time);
            review_comment = (TextView) itemView.findViewById(R.id.review_comment);
        }
    }

    List<ReviewCard> reviews;

    ReviewCardAdapter(List<ReviewCard> reviews){
        this.reviews = reviews;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_card, viewGroup, false);
        ReviewViewHolder pvh = new ReviewViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder reviewViewHolder, int i) {
        reviewViewHolder.name.setText(reviews.get(i).getName());
        reviewViewHolder.rating.setRating((float) reviews.get(i).getRating());
        reviewViewHolder.time.setText(reviews.get(i).getTime());
        reviewViewHolder.review_comment.setText((reviews.get(i).getReviewComment()));

        loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+reviews.get(i).getUserId()+"/"+reviews.get(i).getImage(),
                reviewViewHolder.photo);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<DownloadImageTask> downloadImageTaskWeakReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, DownloadImageTask downloadImageTask) {
            super(res, bitmap);
            downloadImageTaskWeakReference = new WeakReference<DownloadImageTask>(downloadImageTask);
        }

        public DownloadImageTask getDownloadImageTask() {
            return downloadImageTaskWeakReference.get();
        }
    }

    public static boolean cancelPotentialWork(String urldisplay, ImageView imageView) {
        final DownloadImageTask downloadImageTask = getDownloadImageTask(imageView);

        if(downloadImageTask != null) {
            final String bitmapData = downloadImageTask.urldisplay;
            //if bitmap is not set or it differs from the new data
            if(bitmapData == "" || bitmapData != urldisplay) {
                //cancel previous task
                downloadImageTask.cancel(true);
            } else {
                //the same work is already in progress
                return false;
            }
        }

        //No task associated with imageView, or an existing task was cancelled
        return true;
    }

    private static DownloadImageTask getDownloadImageTask(ImageView imageView) {
        if(imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if(drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getDownloadImageTask();
            }
        }

        return null;
    }

    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> bmImageReference;
        private String urldisplay = "";

        public DownloadImageTask(ImageView bmImage) {
            bmImageReference = new WeakReference<ImageView>(bmImage);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... urls) {
            urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if(isCancelled()) {
                result = null;
            }

            if(bmImageReference != null && result != null) {
                final ImageView imageView = bmImageReference.get();
                final DownloadImageTask downloadImageTask = getDownloadImageTask(imageView);
                if(this == downloadImageTask && imageView != null) {
                    imageView.setImageBitmap(result);
                }
            }
        }
    }

    public void loadBitmap(String url, ImageView imageView) {
        if(cancelPotentialWork(url, imageView)) {
            final DownloadImageTask task = new DownloadImageTask(imageView);
            Context context1 = (Context) ProfileActivity.profileActivity;
            Bitmap bitmap = BitmapFactory.decodeResource(context1.getResources(), R.drawable.empty_profile_image);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context1.getResources(), bitmap,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }
}
