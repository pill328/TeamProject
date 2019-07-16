package com.example.teamproject;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentInfo extends Fragment {
    View view;
    private TextView tv_outPut;
    String url = "http://192.168.0.29:8081/";
    ContentValues info = new ContentValues();
    public FragmentInfo() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.info_fragment,container,false);
        // AsyncTask를 통해 HttpURLConnection 수행

        tv_outPut = (TextView) view.findViewById(R.id.tv_outPut);
        info.put("hello","");
        NetworkTask networkTask = new NetworkTask(url,info);
      //  NetworkTask networkTask = new NetworkTask(url, null);
        networkTask.execute();
        return view;
    }

    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {

            String result;
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력
            super.onPostExecute(s);

            tv_outPut.setText(s);
        }
    }

}
