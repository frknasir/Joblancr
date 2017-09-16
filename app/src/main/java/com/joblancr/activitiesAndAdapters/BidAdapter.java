package com.joblancr.activitiesAndAdapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joblancr.cards.Bids;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class BidAdapter extends RecyclerView.Adapter<BidAdapter.BidViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    public BidAdapter(Context context) {
        this.context = context;
    }

    public static class BidViewHolder extends RecyclerView.ViewHolder {
        CardView cv_bids;
        ImageView picture;
        TextView user_name;
        TextView time;
        TextView offer_text;
        TextView bidder_id;
        TextView project_id;
        TextView owner_id;
        TextView bid_id;

        BidViewHolder(View itemView) {
            super(itemView);
            cv_bids = (CardView)itemView.findViewById(R.id.cv_bids);
            picture = (ImageView)itemView.findViewById(R.id.person_photo);
            time = (TextView)itemView.findViewById(R.id.message_time);
            user_name = (TextView)itemView.findViewById(R.id.user_name);
            offer_text = (TextView)itemView.findViewById(R.id.offer_text);
            bidder_id = (TextView)itemView.findViewById(R.id.bidder_id);
            project_id = (TextView)itemView.findViewById(R.id.project_id);
            owner_id = (TextView) itemView.findViewById(R.id.owner_id);
            bid_id = (TextView) itemView.findViewById(R.id.bid_id);
        }
    }

    List<Bids> bids;

    BidAdapter(List<Bids> bids){
        this.bids = bids;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public BidViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bids_card, viewGroup, false);
        BidViewHolder pvh = new BidViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final BidViewHolder bidViewHolder, int i) {
        bidViewHolder.time.setText(bids.get(i).getTime());
        bidViewHolder.user_name.setText(bids.get(i).getBidderName());
        bidViewHolder.offer_text.setText(Html.fromHtml(bids.get(i).getOfferText()));
        bidViewHolder.bidder_id.setText(bids.get(i).getUserId());
        bidViewHolder.project_id.setText(bids.get(i).getProjectId());
        bidViewHolder.owner_id.setText(bids.get(i).getOwnerId());
        bidViewHolder.bid_id.setText(bids.get(i).getBidId());
        loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+bids.get(i).getUserId()+"/"+bids.get(i).getImage(),
        bidViewHolder.picture);

        bidViewHolder.cv_bids.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("bidder_id", bidViewHolder.bidder_id.getText());
                intent.putExtra("project_id", bidViewHolder.project_id.getText());
                intent.putExtra("owner_id", bidViewHolder.owner_id.getText());
                intent.putExtra("bid_id", bidViewHolder.bid_id.getText());

                intent.putExtra("project_title", ProjectViewActivity.project_title);
                intent.putExtra("project_user_id", ProjectViewActivity.project_user_id);
                intent.putExtra("project_status", "0");
                v.getContext().startActivity(intent);
                ProjectViewActivity.projectViewActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return bids.size();
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
            Context context1 = (Context) ProjectViewActivity.projectViewActivity;
            Bitmap bitmap = BitmapFactory.decodeResource(context1.getResources(), R.drawable.empty_profile_image);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context1.getResources(), bitmap,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }
}
