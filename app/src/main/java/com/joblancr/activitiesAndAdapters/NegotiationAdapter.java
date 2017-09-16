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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joblancr.cards.NegotiationCard;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class NegotiationAdapter extends RecyclerView.Adapter<NegotiationAdapter.NegotiationViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    /*public NegotiationAdapter(Context context) {
        this.context = context;
    }*/

    public static class NegotiationViewHolder extends RecyclerView.ViewHolder {
        CardView cv_inbox;
        ImageView picture;
        TextView project_title;
        TextView lastMsg;
        TextView time;
        TextView selectedBidder;
        TextView projectOwner;
        TextView negotiationId;
        TextView newMessage;

        NegotiationViewHolder(View itemView) {
            super(itemView);
            cv_inbox = (CardView)itemView.findViewById(R.id.cv_inbox);
            project_title = (TextView)itemView.findViewById(R.id.project_title);
            picture = (ImageView)itemView.findViewById(R.id.person_photo);
            time = (TextView)itemView.findViewById(R.id.message_time);
            lastMsg = (TextView)itemView.findViewById(R.id.last_message);
            selectedBidder = (TextView)itemView.findViewById(R.id.selected_bidder);
            projectOwner = (TextView)itemView.findViewById(R.id.project_owner);
            negotiationId = (TextView)itemView.findViewById(R.id.negotiation_id);
            newMessage = (TextView)itemView.findViewById(R.id.new_message);
        }
    }

    List<NegotiationCard> messages;

    public NegotiationAdapter(List<NegotiationCard> messages){
        this.messages = messages;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public NegotiationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.negotiation_card, viewGroup, false);
        NegotiationViewHolder pvh = new NegotiationViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final NegotiationViewHolder negotiationViewHolder, int i) {
        negotiationViewHolder.project_title.setText(messages.get(i).getTitle());
        negotiationViewHolder.time.setText(messages.get(i).getTime());
        negotiationViewHolder.lastMsg.setText(messages.get(i).getLastExchange());
        negotiationViewHolder.selectedBidder.setText(messages.get(i).getSelectedBidder());
        negotiationViewHolder.negotiationId.setText(messages.get(i).getNegotiationId());
        negotiationViewHolder.projectOwner.setText(messages.get(i).getProjectOwner());

        if(NegotiationActivity.userDetails.get("id").equals(messages.get(i).getProjectOwner())) {
            loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+messages.get(i).getSelectedBidder()+"/"+messages.get(i).getPhotoID(),
                    negotiationViewHolder.picture);
        } else {
            loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+messages.get(i).getProjectOwner()+"/"+messages.get(i).getPhotoID(),
                    negotiationViewHolder.picture);
        }

        if(messages.get(i).getExchangeStatus() == 1) {
            negotiationViewHolder.newMessage.setVisibility(View.VISIBLE);
        }

        negotiationViewHolder.cv_inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ExchangeActivity.class);
                intent.putExtra("negotiation_title",negotiationViewHolder.project_title.getText());
                intent.putExtra("negotiation_id",negotiationViewHolder.negotiationId.getText());
                intent.putExtra("selected_bidder", negotiationViewHolder.selectedBidder.getText());
                intent.putExtra("project_owner", negotiationViewHolder.projectOwner.getText());
                v.getContext().startActivity(intent);
                NegotiationActivity.negotiationActivity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
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
            Context context1 = (Context) NegotiationActivity.negotiationActivity;
            Bitmap bitmap = BitmapFactory.decodeResource(context1.getResources(), R.drawable.empty_profile_image);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context1.getResources(), bitmap,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }
}
