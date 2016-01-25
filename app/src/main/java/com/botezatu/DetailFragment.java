package com.botezatu;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private ImageView mImg;


    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (getArguments() != null) {

            String title = getArguments().getString("title");
            if (title != null && !title.isEmpty()) {
                TextView tvTitle = (TextView)rootView.findViewById(R.id.title);
                tvTitle.setText(title);
                tvTitle.setVisibility(View.VISIBLE);
            }

            String pubDate = getArguments().getString("pubDate");
            if (pubDate != null && !pubDate.isEmpty()) {
                TextView tvPubDate = (TextView)rootView.findViewById(R.id.pubDate);
                tvPubDate.setText(pubDate);
                tvPubDate.setVisibility(View.VISIBLE);
            }

            String link = getArguments().getString("link");
            if (link != null && !link.isEmpty()) {
                TextView tvlink = (TextView)rootView.findViewById(R.id.link);
                tvlink.setText(link);
                tvlink.setVisibility(View.VISIBLE);
            }

            String description = getArguments().getString("description");
            if (description != null && !description.isEmpty()) {
                TextView tvDescription = (TextView)rootView.findViewById(R.id.description);
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            }



            final String image_url = getArguments().getString("image_url");
            if (image_url != null && !image_url.isEmpty()) {

                mImg = (ImageView)rootView.findViewById(R.id.img);

                // рисуем элемент списка в фоне
                new AsyncTask<Void, Void, Bitmap>(){
                    @Override
                    protected Bitmap doInBackground(Void... tags) {

                        try {
                            return Utils.loadImage(image_url,1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);

                        if ( bitmap != null) {
                            mImg.setImageBitmap(bitmap);
                            mImg.setVisibility(View.VISIBLE);
                        }
                    }

                }.execute();
            }

        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mImg != null && mImg.getDrawable() != null) {
            if (((BitmapDrawable) mImg.getDrawable()).getBitmap() != null) {
                ((BitmapDrawable) mImg.getDrawable()).getBitmap().recycle();
                mImg.setImageDrawable(null);
            }
            mImg = null;
        }

    }
}
