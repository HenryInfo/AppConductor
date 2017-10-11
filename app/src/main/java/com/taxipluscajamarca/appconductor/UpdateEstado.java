package com.taxipluscajamarca.appconductor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by hbs on 10/10/17.
 */

public class UpdateEstado {

    public  void CambiarEstado(final ProgressDialog mProgressDialog, final Context contex, final String estado, final int idruta, final  int idmovilidad, final  String estadoMovilidad) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                mProgressDialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                JSONObject jsonObject = new JSONObject();
                String body = "";
                try {
                    if(idmovilidad!=0&&idruta!=0)
                    {
                        jsonObject.put("idruta", idruta);
                        jsonObject.put("estado", estado);
                        jsonObject.put("idmovilidad", idmovilidad);
                        jsonObject.put("estadomovilidad", estadoMovilidad);

                    }
                    else{
                        if(idmovilidad==0)
                        {

                            jsonObject.put("idruta", idruta);
                            jsonObject.put("estado", estado);
                        }
                        else {
                            jsonObject.put("idmovilidad", idmovilidad);
                            jsonObject.put("estadomovilidad", estadoMovilidad);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/rutas/updateEstadoRutaMovilidad");
                    httpPost.setEntity(new StringEntity(jsonObject.toString()));
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    HttpResponse response = httpClient.execute(httpPost);
                    body=MetodosAbstractos.getResponseBody(response);
                }catch(ClientProtocolException e) {
                    Toast.makeText(contex,e.toString(), Toast.LENGTH_LONG).show();
                } catch (IOException ee) {

                }
                return body;
            }
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                JSONObject respuesta= null;
                try {
                    respuesta = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray array;
                    if (respuesta != null) {



                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
    }

}
