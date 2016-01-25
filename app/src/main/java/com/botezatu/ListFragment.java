package com.botezatu;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView mList;

    private ImmageArrayAdapter mImmageAdapter;

    private int mSelectedPos;

    private RSSSource[] mRssSources = {new RSSSource("http://lenta.ru/rss", "utf-8"), new RSSSource("http://www.gazeta.ru/export/rss/lenta.xml", "windows-1251")};

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        RSSItem item = (RSSItem)parent.getItemAtPosition(position);

        DetailFragment detail = new DetailFragment();

        Bundle args = new Bundle();
        args.putString("title", item.title);
        args.putString("image_url", item.image_url);
        args.putString("link", item.link);
        args.putString("pubDate", item.pubDate);
        args.putString("description", item.description);

        detail.setArguments(args);
        getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, detail).commit();
    }

    class RSSItem implements Comparable<RSSItem> {

        public String title;
        public String image_url;
        public String link;
        public String pubDate;
        public String description;
        public String source;
        public long timestamp;

        @Override
        public int compareTo(RSSItem another) {
            if (timestamp > another.timestamp) {
                return -1;
            }
            else if (timestamp <  another.timestamp) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    class RSSSource {
        public String link;
        public String charset;

        RSSSource(String l, String c){
            link = l;
            charset = c;
        }
    }

    class ImmageArrayAdapter extends ArrayAdapter<RSSItem> {

        public ImmageArrayAdapter(Context context, List<RSSItem> values) {
            super(context, R.layout.image_list_item, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Tag tag;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.image_list_item, parent, false);
                convertView.setTag(tag = new Tag(convertView));
            } else {
                tag = (Tag) convertView.getTag();
            }

            final RSSItem RSSItem = getItem(position);
            tag.img.setImageBitmap(null);
            tag.title.setText(RSSItem.title);
            tag.pubDate.setText(RSSItem.pubDate);
            tag.source.setText(RSSItem.source);

            mSelectedPos = position;

            if (RSSItem.image_url != null && !RSSItem.image_url.isEmpty()){
                tag.progress.setVisibility(View.VISIBLE);
                tag.img.setVisibility(View.VISIBLE);

                // рисуем элемент списка в фоне
                new AsyncTask<Tag, Void, Bitmap>(){

                    private Tag tag;

                    @Override
                    protected Bitmap doInBackground(Tag... tags) {

                        tag = tags[0];


                        if ( RSSItem.image_url != null && !RSSItem.image_url.isEmpty()) {
                            try {


                                return Utils.loadImage(RSSItem.image_url, 4);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        super.onPostExecute(bitmap);

                        if ( bitmap != null) {
                            tag.img.setImageBitmap(bitmap);
                            tag.progress.setVisibility(View.GONE);
                        }
                    }

                }.execute(tag);

            } else {
                tag.progress.setVisibility(View.GONE);
                tag.img.setVisibility(View.GONE);
            }

            return  convertView;
        }

        final class Tag {
            final ImageView img;
            final ProgressBar progress;
            final TextView title;
            final TextView pubDate;
            final TextView source;

            Tag(View view){

                img = (ImageView) view.findViewById(R.id.img);
                title = (TextView) view.findViewById(R.id.title);
                pubDate = (TextView) view.findViewById(R.id.pubDate);
                source = (TextView) view.findViewById(R.id.source);
                progress = (ProgressBar) view.findViewById(R.id.progress);
            }
        }

    }

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        mList = (ListView)rootView.findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mImmageAdapter == null || mImmageAdapter.getCount() <= 0){
            loadList();
        } else {
            mList.setAdapter(mImmageAdapter);
            mList.setSelection(mSelectedPos);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadList(){
        new AsyncTask<Void, Void, List<RSSItem>>(){

            @Override
            protected List<RSSItem> doInBackground(Void... params) {
                List<RSSItem> result = null;
                try {

                    if (mRssSources != null && mRssSources.length >0){

                        result = new ArrayList<>();

                        for (int i=0; i <mRssSources.length; i++) {
                            String feed = getRssFeed(mRssSources[i].link, mRssSources[i].charset);
                            result.addAll(parse(feed));
                        }

                        Collections.sort(result);
                    }


                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<RSSItem> listRSSItem) {
                super.onPostExecute(listRSSItem);

                if (listRSSItem != null) {
                    mImmageAdapter = new ImmageArrayAdapter(getActivity(), listRSSItem);
                    mList.setAdapter(mImmageAdapter);

                }

            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

        }.execute();

    }

    public String getRssFeed(String strUrl, String charset){

        InputStream in = null;
        try {

            URL url = new URL(strUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            String rssFeed = new String(response, charset);

            return rssFeed;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;

    }


    public List<RSSItem> parse(String feed) throws XmlPullParserException, IOException {

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(feed));

        return readRss(xpp);
    }

    private List<RSSItem> readRss(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<RSSItem> items = new ArrayList<>();

        RSSItem RSSItem = null;
        String source ="";

        Date date ;
        DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);




        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if(eventType == XmlPullParser.START_DOCUMENT) {

                Log.d("RSS", "Start document");

            } else if(eventType == XmlPullParser.START_TAG) {

                if (parser.getName().equalsIgnoreCase("item")) {

                    RSSItem = new RSSItem();
                    RSSItem.source = source;

                } else if (parser.getName().equalsIgnoreCase("enclosure")) {

                    if (RSSItem != null) {
                        String url = parser.getAttributeValue(0);
                        RSSItem.image_url = url;
                    }

                } else if (parser.getName().equalsIgnoreCase("title")) {

                    if (RSSItem != null) {
                        parser.next();

                        eventType = parser.getEventType();

                        if(eventType == XmlPullParser.TEXT) {
                            RSSItem.title = parser.getText();
                        }
                    }

                } else if (parser.getName().equalsIgnoreCase("description")) {

                    if (RSSItem != null) {
                        parser.next();

                        eventType = parser.getEventType();

                        if(eventType == XmlPullParser.TEXT) {
                            RSSItem.description = parser.getText();
                        }
                    }

                } else if (parser.getName().equalsIgnoreCase("link")) {

                    if (RSSItem != null) {
                        parser.next();

                        eventType = parser.getEventType();

                        if(eventType == XmlPullParser.TEXT) {
                            RSSItem.link = parser.getText();
                        }
                    } else {
                        parser.next();
                        source = parser.getText();
                    }

                } else if (parser.getName().equalsIgnoreCase("pubDate")) {

                    if (RSSItem != null) {
                        parser.next();

                        eventType = parser.getEventType();

                        if(eventType == XmlPullParser.TEXT) {

                            String pubDate =  parser.getText();

                            try {
                                date = formatter.parse(pubDate);
                                RSSItem.timestamp = date.getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            RSSItem.pubDate = parser.getText();
                        }
                    }

                }

            } else if(eventType == XmlPullParser.END_TAG) {

                if (parser.getName().equalsIgnoreCase("item")) {
                    items.add(RSSItem);
                }

                //Log.d("RSS", "End tag " + parser.getName());

            } else if(eventType == XmlPullParser.TEXT) {

                //Log.d("RSS", "Text " + parser.getText());
            }
            eventType = parser.next();
        }

        System.out.println("End document");



        return items;
    }

}
