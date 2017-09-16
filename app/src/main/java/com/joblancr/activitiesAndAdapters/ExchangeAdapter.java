package com.joblancr.activitiesAndAdapters;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joblancr.cards.ExchangeCard;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Faruk on 8/4/16.
 */
public class ExchangeAdapter extends RecyclerView.Adapter<ExchangeAdapter.ExchangeViewHolder> {

    private Context context;
    //provide a reference to the views for each data item
    //you provide access to all the views for a data item in a view holder
    public ExchangeAdapter(Context context) {
        this.context = context;
    }

    public static class ExchangeViewHolder extends RecyclerView.ViewHolder {
        //CardView cv_exchange;
        LinearLayout rightLayout;
        LinearLayout leftLayout;
        TextView nameLayoutLeft;
        ImageView pictureLayoutLeft;
        TextView exchangeLayoutLeft;
        TextView timeLayoutLeft;
        TextView nameLayoutRight;
        ImageView pictureLayoutRight;
        TextView exchangeLayoutRight;
        TextView timeLayoutRight;

        ExchangeViewHolder(View itemView) {
            super(itemView);
            rightLayout = (LinearLayout) itemView.findViewById(R.id.right_layout);
            leftLayout = (LinearLayout) itemView.findViewById(R.id.left_layout);
            //cv_exchange = (CardView)itemView.findViewById(R.id.cv_exchange);

            pictureLayoutLeft = (ImageView)itemView.findViewById(R.id.image_layout_left);
            timeLayoutLeft = (TextView)itemView.findViewById(R.id.time_layout_left);
            exchangeLayoutLeft = (TextView)itemView.findViewById(R.id.exchange_layout_left);
            nameLayoutLeft = (TextView)itemView.findViewById(R.id.name_layout_left);

            pictureLayoutRight = (ImageView)itemView.findViewById(R.id.image_layout_right);
            timeLayoutRight = (TextView)itemView.findViewById(R.id.time_layout_right);
            exchangeLayoutRight = (TextView)itemView.findViewById(R.id.exchange_layout_right);
            nameLayoutRight = (TextView)itemView.findViewById(R.id.name_layout_right);
        }
    }

    List<ExchangeCard> exchanges;

    ExchangeAdapter(List<ExchangeCard> exchanges){
        this.exchanges = exchanges;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ExchangeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.exchange_card, viewGroup, false);
        ExchangeViewHolder pvh = new ExchangeViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final ExchangeViewHolder exchangeViewHolder, int i) {
        String id = ExchangeActivity.userDetails.get("id");

        if(exchanges.get(i).getUserId().equals(id)) {
            exchangeViewHolder.rightLayout.setVisibility(View.VISIBLE);

            exchangeViewHolder.timeLayoutRight.setText(exchanges.get(i).getTime());
            exchangeViewHolder.nameLayoutRight.setText(exchanges.get(i).getName());
            exchangeViewHolder.exchangeLayoutRight.setText(exchanges.get(i).getExchange());
            loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+exchanges.get(i).getUserId()+"/"+exchanges.get(i).getImage(),
                    exchangeViewHolder.pictureLayoutRight);
        } else {
            exchangeViewHolder.leftLayout.setVisibility(View.VISIBLE);

            exchangeViewHolder.timeLayoutLeft.setText(exchanges.get(i).getTime());
            exchangeViewHolder.nameLayoutLeft.setText(exchanges.get(i).getName());
            exchangeViewHolder.exchangeLayoutLeft.setText(exchanges.get(i).getExchange());
            loadBitmap("http://192.168.43.14/Joblancr/Php/Webservice/uploads/"+exchanges.get(i).getUserId()+"/"+exchanges.get(i).getImage(),
                    exchangeViewHolder.pictureLayoutLeft);
        }

        /*exchangeViewHolder.cv_exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something here
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return exchanges.size();
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
            Context context1 = (Context) ExchangeActivity.exchangeActivity;
            Bitmap bitmap = BitmapFactory.decodeResource(context1.getResources(), R.drawable.empty_profile_image);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context1.getResources(), bitmap,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }
}
