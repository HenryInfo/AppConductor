package com.taxipluscajamarca.appconductor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hbs on 4/06/17.
 */

public abstract class AbstService extends Service {

    protected final String TAG = "AbstService";

    @Override
    public void onCreate() {
        super.onCreate();
        onStartService();
        Log.i("NOMAMES", "onCreate(): Service Started.");
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("NOMAMES", "onStarCommand(): Received id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onStopService();
        Log.i("NOMAMES", "Service Stopped.");
    }

    public abstract void onStartService();
    public abstract void onStopService();
    public abstract void onReceiveMessage(Message msg);

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String idmovilidad=preferences.getString("pIdMovilidad","");
        if(!equals(idmovilidad, ""))
        {
            int idm= Integer.parseInt(idmovilidad);
            Toast.makeText(getApplicationContext(), "Usted esta desconectado, no le llegarán notificaciones, ni aparecá en nuestros mapas, vuelva a ingresar si esto es un error", Toast.LENGTH_LONG).show();
            if(equals(preferences.getString("pIdSolicitud", ""), ""))
            {
                Log.d("NOMAMES", "cambio a estado 3");
                CambiarEstado("3", idm);


            }
            else {
                Log.d("NOMAMES", "cambio a estado 2");

                CambiarEstado("2", idm);

            }
        }

        super.onTaskRemoved(rootIntent);
    }
    public boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public  static  void CambiarEstado(final  String estadoMovilidad, final int idmovilidad) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
                                @Override
                                protected String doInBackground(String... params) {
                                    JSONObject jsonObject = new JSONObject();
                                    String body = "";
                                    try {
                                        jsonObject.put("idmovilidad", idmovilidad);
                                        jsonObject.put("estadomovilidad", estadoMovilidad);

                                    } catch (JSONException e) {
                                        Log.d("NOMAMES", "Service Stopped 1.");

                                        e.printStackTrace();
                                    }
                                    try {
                                        HttpClient httpClient = new DefaultHttpClient();
                                        HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/rutas/updateEstadoRutaMovilidad");
                                        httpPost.setEntity(new StringEntity(jsonObject.toString()));
                                        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                                        HttpResponse response = httpClient.execute(httpPost);
                                        body=getResponseBody(response);
                                        Log.d("NOMAMES", "Service Stopped 0." +body);

                                    }catch(ClientProtocolException e) {
                                        Log.d("ENTRO", estadoMovilidad);

                                    } catch (IOException ee) {
                                        Log.d("ENTRO", estadoMovilidad);

                                    }
                                    return body;
                                }
                                @Override
                                protected void onPostExecute(String result) {
                                    super.onPostExecute(result);
                                    try {
                                        finalize();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                }
                            }
                            SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
                            sendPostReqAsyncTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1); //execute in every 50000 ms

    }
    public static String getResponseBody(HttpResponse response) {

        String response_text = null;
        HttpEntity entity = null;
        try {
            entity = response.getEntity();
            response_text = _getResponseBody(entity);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (entity != null) {
                try {
                    entity.consumeContent();
                } catch (IOException e1) {
                }
            }
        }
        return response_text;
    }
    public static String _getResponseBody(final HttpEntity entity) throws IOException, ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }

        InputStream instream = entity.getContent();

        if (instream == null) {
            return "";
        }

        if (entity.getContentLength() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(

                    "HTTP entity too large to be buffered in memory");
        }

        String charset = getContentCharSet(entity);

        if (charset == null) {

            charset = HTTP.DEFAULT_CONTENT_CHARSET;

        }

        Reader reader = new InputStreamReader(instream, charset);

        StringBuilder buffer = new StringBuilder();

        try {

            char[] tmp = new char[1024];

            int l;

            while ((l = reader.read(tmp)) != -1) {

                buffer.append(tmp, 0, l);

            }

        } finally {

            reader.close();

        }

        return buffer.toString();

    }
    public static String getContentCharSet(final HttpEntity entity) throws ParseException {

        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }

        String charset = null;

        if (entity.getContentType() != null) {

            HeaderElement values[] = entity.getContentType().getElements();

            if (values.length > 0) {

                NameValuePair param = values[0].getParameterByName("charset");

                if (param != null) {

                    charset = param.getValue();

                }

            }

        }

        return charset;

    }

}