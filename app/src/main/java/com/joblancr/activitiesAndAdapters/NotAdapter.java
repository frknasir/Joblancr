package com.joblancr.activitiesAndAdapters;

import android.content.Context;
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

import com.joblancr.cards.NotificationCard;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class NotAdapter extends RecyclerView.Adapter<NotAdapter.NotificationViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    public NotAdapter(Context context) {
        this.context = context;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cv_notification;
        ImageView picture;
        TextView messageText;
        TextView time;

        NotificationViewHolder(View itemView) {
            super(itemView);
            cv_notification = (CardView)itemView.findViewById(R.id.cv_notification);
            picture = (ImageView)itemView.findViewById(R.id.person_photo);
            time = (TextView)itemView.findViewById(R.id.message_time);
            messageText = (TextView)itemView.findViewById(R.id.message_text);
        }
    }

    List<NotificationCard> notifications;

    NotAdapter(List<NotificationCard> notifications){
        this.notifications = notifications;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_card, viewGroup, false);
        NotificationViewHolder pvh = new NotificationViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder notViewHolder, int i) {
        notViewHolder.time.setText(notifications.get(i).getTime());
        notViewHolder.messageText.setText(notifications.get(i).getMessageText());
        loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+notifications.get(i).getUserId()+"/"+notifications.get(i).getImage(),
                notViewHolder.picture);

        /*notViewHolder.cv_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(v.getContext(), ExchangeActivity.class);
                //v.getContext().startActivity(intent);
                Toast toast = new Toast(v.getContext());
                toast.makeText(v.getContext(),"clicked",Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return notifications.size();
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
            Context context1 = (Context) NotificationActivity.notificationActivity;
            Bitmap bitmap = BitmapFactory.decodeResource(context1.getResources(), R.drawable.empty_profile_image);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context1.getResources(), bitmap,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }
}
