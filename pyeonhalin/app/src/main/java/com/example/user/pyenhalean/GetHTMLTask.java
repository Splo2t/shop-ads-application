package com.example.user.pyenhalean;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GetHTMLTask extends AsyncTask<String, Void, String> {
    String sUrl = "http://18.188.162.184:5010/";
    String routeUrl;
    String loginCode;
    String id;
    String pw;
    String time = "temp";
    private Elements element;


    @Override
    protected void onPostExecute(String html) {
        super.onPostExecute(html);
        //startActivity(activity,result,result.getExtras());
    }


    @Override
    protected String doInBackground(String... parm) {
        String returnString = "";
        Connection.Response res;
        Document doc;

        try {
            if(parm[0].equals("signIn")){
                res = Jsoup.connect(sUrl + parm[0]).data("id", parm[1]).data("pw", parm[2]).data("time", parm[3]).method(Connection.Method.POST).execute();
                element = res.parse().select("h1");
                returnString = element.get(0).text() + "#" + element.get(2).text() + "#" + res.cookie(parm[1]);
            } else if(parm[0].equals("signUp")){
                res = Jsoup.connect(sUrl + parm[0]).data("id", parm[1]).data("pw", parm[2]).method(Connection.Method.POST).execute();
                element = res.parse().select("h1");
                returnString = element.get(0).text();
            } else if(parm[0].equals("testData")){
                doc = Jsoup.connect(sUrl + parm[0]).data("id", parm[1]).data("key",parm[2]).cookie(parm[1],parm[3]).post();
                element = doc.select("h1");
                returnString = element.text();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("test",returnString);
        return returnString;

    }
}