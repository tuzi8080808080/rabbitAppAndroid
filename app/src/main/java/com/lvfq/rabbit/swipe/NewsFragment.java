/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lvfq.rabbit.swipe;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;

import com.lvfq.rabbit.MainApplication;
import com.lvfq.rabbit.R;
import com.lvfq.rabbit.adapter.RabbitNewsAdapter;
import com.lvfq.rabbit.data.RabbitDataItem;
import com.lvfq.rabbit.util.HttpRequest;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * A sample which shows how to use {@link SwipeRefreshLayout} within a
 * {@link android.support.v4.app.ListFragment} to add the 'swipe-to-refresh' gesture to a
 * {@link android.widget.ListView}. This is provided through the provided re-usable
 * {@link SwipeRefreshListFragment} class.
 *
 * <p>To provide an accessible way to trigger the refresh, this app also provides a refresh
 * action item. This item should be displayed in the Action Bar's overflow item.
 *
 * <p>In this sample app, the refresh updates the ListView with a random set of new items.
 *
 * <p>This sample also provides the functionality to change the colors displayed in the
 * {@link SwipeRefreshLayout} through the options menu. This is meant to
 * showcase the use of color rather than being something that should be integrated into apps.
 */
public class NewsFragment extends SwipeRefreshListFragmentFragment {

    private static final String TAG="NewsFragment";

    private long SINCE_ID = 0;
    private long MAX_ID = 0;
    private List<RabbitDataItem> rabbitData=null;

    private int preLast=-1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rabbitAdapter=new RabbitNewsAdapter(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = getListView();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //Log.d(TAG, "first:"+firstVisibleItem+" visible:"+visibleItemCount+" total:"+totalItemCount+" last:"+listView.getLastVisiblePosition());
                if (listView.getLastVisiblePosition() >=0 && listView.getLastVisiblePosition() == listView.getAdapter().getCount() - 1
                        && listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight()) {
                    Log.d(TAG, "load more");
                    new NewsLoadMoreBackgroundTask().execute();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    // BEGIN_INCLUDE (initiate_refresh)
    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    protected void initiateRefresh() {
        super.initiateRefresh();
        /**
         * Execute the background task, which uses {@link AsyncTask} to load the data.
         */
        new NewsRefreshBackgroundTask().execute();
    }
    // END_INCLUDE (initiate_refresh)

    /**
     * Dummy {@link AsyncTask} which simulates a long running task to fetch new cheeses.
     */
    private class NewsRefreshBackgroundTask extends AsyncTask<Void, Void, List<RabbitDataItem>> {
        private static final String TAG="RefreshNews";
        private Boolean hasMore=false;
        @Override
        protected List<RabbitDataItem> doInBackground(Void... params) {
            //request the server to get rabbit data
            rabbitData=((MainApplication)getActivity().getApplication()).getListRabbitDataItem_NEWS();
            if(rabbitData==null) {
                String result = HttpRequest.sendGet(getString(R.string.news_server), "count=10");
                if (result != null) {
                    Log.d(TAG, "into add rabit item. empty data.");
                    rabbitData = new ArrayList<RabbitDataItem>();
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        if(jsonArray.length()>0)
                            hasMore=true;
                        else
                            hasMore=false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject oneRabbit = jsonArray.getJSONObject(i);
                            RabbitDataItem rabbitDataItem = new RabbitDataItem();
                            rabbitDataItem.title = oneRabbit.getString("title");
                            rabbitDataItem.thumbnail = oneRabbit.getJSONArray("thumbnail").getString(0);
                            rabbitDataItem.maintext = oneRabbit.getString("content");
                            rabbitDataItem.timetext = oneRabbit.getString("time");
                            rabbitData.add(rabbitDataItem);

                            if(i==0) {
                                SINCE_ID=oneRabbit.getLong("id");
                            }
                            else if(i==jsonArray.length()-1) {
                                MAX_ID=oneRabbit.getLong("id");
                            }
                        }
                    } catch (JSONException e) {
                        rabbitData = null;
                        Log.e(TAG, "JSONException");
                    }
                }
            }
            else {
                String result = HttpRequest.sendGet(getString(R.string.news_server), "since_id="+SINCE_ID);
                if (result != null) {
                    Log.d(TAG, "into add rabit item");
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        if(jsonArray.length()>0)
                            hasMore=true;
                        else
                            hasMore=false;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject oneRabbit = jsonArray.getJSONObject(i);
                            RabbitDataItem rabbitDataItem = new RabbitDataItem();
                            rabbitDataItem.title = oneRabbit.getString("title");
                            rabbitDataItem.thumbnail = oneRabbit.getJSONArray("thumbnail").getString(0);
                            rabbitDataItem.maintext = oneRabbit.getString("content");
                            rabbitDataItem.timetext = oneRabbit.getString("time");
                            rabbitData.add(i, rabbitDataItem);

                            if(i==0) {
                                SINCE_ID=oneRabbit.getLong("id");
                            }
                        }
                    } catch (JSONException e) {
                        rabbitData = null;
                        Log.e(TAG, "JSONException");
                    }
                }
            }
            // Return rabbitdata
            return rabbitData;
        }

        @Override
        protected void onPostExecute(List<RabbitDataItem> result) {
            super.onPostExecute(result);
            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result, hasMore);
        }
    }

    private class NewsLoadMoreBackgroundTask extends AsyncTask<Void, Void, List<RabbitDataItem>> {
        private static final String TAG="LoadMoreNews";
        private Boolean hasMore=false;
        @Override
        protected List<RabbitDataItem> doInBackground(Void... params) {
            //request the server to get rabbit data
            rabbitData=((MainApplication)getActivity().getApplication()).getListRabbitDataItem_NEWS();
            String result = HttpRequest.sendGet(getString(R.string.news_server), "count=5&max_id="+MAX_ID);
            if (result != null) {
                Log.d(TAG, "into add rabit item");
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    if(jsonArray.length()>0)
                        hasMore=true;
                    else
                        hasMore=false;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject oneRabbit = jsonArray.getJSONObject(i);
                        RabbitDataItem rabbitDataItem = new RabbitDataItem();
                        rabbitDataItem.title = oneRabbit.getString("title");
                        rabbitDataItem.thumbnail = oneRabbit.getJSONArray("thumbnail").getString(0);
                        rabbitDataItem.maintext = oneRabbit.getString("content");
                        rabbitDataItem.timetext = oneRabbit.getString("time");
                        rabbitData.add(rabbitDataItem);

                        if(i==jsonArray.length()-1) {
                            MAX_ID=oneRabbit.getLong("id");
                        }
                    }
                } catch (JSONException e) {
                    rabbitData = null;
                    Log.e(TAG, "JSONException");
                }
            }
            // Return rabbitdata
            return rabbitData;
        }

        @Override
        protected void onPostExecute(List<RabbitDataItem> result) {
            super.onPostExecute(result);
            // Tell the Fragment that the refresh has completed
            onRefreshComplete(result, hasMore);
        }
    }
}
