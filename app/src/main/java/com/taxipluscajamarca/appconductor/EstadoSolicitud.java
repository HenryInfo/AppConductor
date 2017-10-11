package com.taxipluscajamarca.appconductor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.taxipluscajamarca.appconductor.model.Ruta;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.format;
import static android.R.attr.layout;
import static android.R.attr.mode;


/**
 * Created by hbs on 1/10/16.
 */

public class EstadoSolicitud implements
        View.OnClickListener {
    //private TextView resultadoTodal;
    private Polyline polyline=null;
    private LayoutInflater layoutInflater;
    private Ruta ruta=null;
    private LinearLayout opcionesSolicitud=null, solicitudActualLinear=null, infoCliente;
    private GoogleMap mMap;
    private static LatLng Shear=null;
    private SharedPreferences preferences=null;
    private Button btnFinalizar;
    private Button btnCancelar;
    private Button btnLLegue;
    private ImageButton imgSwhowDialogInfoCliente, btnNavegar;
    private Intent intentSolicitud;
    private Activity activity;
    ProgressDialog mProgressDialog;
    // private boolean bandera=false;
   EstadoSolicitud(Activity act, Context context, Ruta objet, GoogleMap mMap, LinearLayout opcionesSolicitud, LinearLayout solicitudActual, SharedPreferences preferences, Intent intent, LinearLayout dialogoNotificacion, ImageButton btnShowNoti, ImageButton btnNavegar) {
        layoutInflater = LayoutInflater.from(context);
        ruta=objet;
        //this.txtDesde= (TextView) linearLayout.findViewById(R.id.TxtDesde);
        //this.txtHasta= (TextView) linearLayout.findViewById(R.id.TxtHasta);
        this.mMap=mMap;
        this.activity=act;
        this.opcionesSolicitud=opcionesSolicitud;
        this.solicitudActualLinear=solicitudActual;
        this.preferences=preferences;
        imgSwhowDialogInfoCliente= btnShowNoti;
        infoCliente=dialogoNotificacion;
        this.btnNavegar=btnNavegar;
        Button btnAceptar = (Button) opcionesSolicitud.findViewById(R.id.btnAceptar);
        Button btnRechazar = (Button) opcionesSolicitud.findViewById(R.id.btnRechazar);
        btnFinalizar= (Button) solicitudActual.findViewById(R.id.btnTermine);
        btnCancelar= (Button) solicitudActual.findViewById(R.id.btnCancelar);
        btnLLegue= (Button) solicitudActual.findViewById(R.id.btnLlegue);
        intentSolicitud=intent;
        btnAceptar.setOnClickListener(this);
        btnRechazar.setOnClickListener(this);
        btnFinalizar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
        btnLLegue.setOnClickListener(this);

        monstraRutaMapa(objet,context);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onClick(final View v) {

        Shear=null;
        Ruta r=ruta;
        SharedPreferences.Editor editor = preferences.edit();
        /*
      mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
             //   downloadTask.cancel(true);
            }
        });*/
        switch (v.getId())
        {
            case R.id.btnAceptar:
                //Activar Layout de pedido Actual
                btnFinalizar.setEnabled(false);
                //solicitudActualLinear.setVisibility(View.VISIBLE);
                opcionesSolicitud.setVisibility(View.GONE);
                editor.putString("pIdSolicitud", String.valueOf(r.getId()));//Si ha aceptado una ruta se guarda su id
                editor.apply();

                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage("Enviando respuesta, por favor espere...");
                //mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);

                UpdateEstado c= new UpdateEstado();
                c.CambiarEstado(mProgressDialog, v.getContext(),"2", r.getId(), r.getIdmovilidad(), "2" );
                v.getContext().stopService(intentSolicitud);
                break;
            case R.id.btnRechazar:
                mMap.clear();
                btnFinalizar.setEnabled(true);
                btnNavegar.setVisibility(View.GONE);
                imgSwhowDialogInfoCliente.setVisibility(View.GONE);
                infoCliente.setVisibility(View.GONE);
                editor.putString("pIdSolicitud", "");//Si ha terminado se reinicia
                editor.apply();

                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage("Rechazando... por favor espere...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);

                UpdateEstado c1= new UpdateEstado();
                c1.CambiarEstado(mProgressDialog, v.getContext(), "4", r.getId(), 0, "0");//Servicio Cancelado
                mMap.clear();
                opcionesSolicitud.setVisibility(View.GONE);
                v.getContext().stopService(intentSolicitud);
                break;
            case R.id.btnLlegue:
                mMap.clear();
                btnFinalizar.setEnabled(true);
                btnNavegar.setVisibility(View.GONE);

                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage("Enviando aviso... por favor espere...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);

                UpdateEstado c2= new UpdateEstado();
                c2.CambiarEstado(mProgressDialog, v.getContext(), "6", r.getId(), r.getIdmovilidad(), "2");//LLego al lugar del cliente no actualiza su estado de la movilidad
                btnCancelar.setVisibility(View.GONE);
                btnLLegue.setVisibility(View.GONE);
                //btnFinalizar.setVisibility(View.VISIBLE);
                v.getContext().stopService(intentSolicitud);
                break;
            case R.id.btnCancelar:
                mMap.clear();
                btnNavegar.setVisibility(View.GONE);
                imgSwhowDialogInfoCliente.setVisibility(View.GONE);
                infoCliente.setVisibility(View.GONE);
                editor.putString("pIdSolicitud", "");//Si ha terminado se reinicia
                editor.apply();

                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage("Cancelando servicio... por favor espere...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                UpdateEstado c3= new UpdateEstado();
                c3.CambiarEstado(mProgressDialog, v.getContext(), "4", r.getId(), 0, "0");//Servicio Cancelado
                solicitudActualLinear.setVisibility(View.GONE);
                mMap.clear();
                v.getContext().stopService(intentSolicitud);
                break;
            case R.id.btnTermine:
                mMap.clear();

                btnNavegar.setVisibility(View.GONE);
                imgSwhowDialogInfoCliente.setVisibility(View.GONE);
                infoCliente.setVisibility(View.GONE);
                btnFinalizar.setVisibility(View.GONE);
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage("Terminando servicio... por favor espere...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                UpdateEstado c4= new UpdateEstado();
                c4.CambiarEstado(mProgressDialog, v.getContext(),"3", r.getId(), r.getIdmovilidad(), "1");//Finalizó su servicio la movilidad vuelve a estar disponible
                solicitudActualLinear.setVisibility(View.GONE);
                mMap.clear();
                editor.putString("pIdSolicitud", "");//Si ha terminado se reinicia
                editor.apply();
                break;
        }
    }


private void monstraRutaMapa(Ruta r, Context v)
    {

        BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.blue);
        BitmapDescriptor icondestino = BitmapDescriptorFactory.fromResource(R.drawable.green);
        MarkerOptions mO= new MarkerOptions();
        MarkerOptions mD= new MarkerOptions();
        mO.icon(iconorigen);
        mD.icon(icondestino);
        if(r.getLatitudD()!=0&&r.getLongitudO()!=0&&r.getLatitudO()!=0&&r.getLongitudD()!=0)
        {
            MetodosAbstractos mt= new MetodosAbstractos();
            mt.dibujarRuta(new LatLng(r.getLatitudO(),r.getLongitudO()), new LatLng(r.getLatitudD(), r.getLongitudD()), mMap, polyline);

            mO.position(new LatLng(r.getLatitudO(), r.getLongitudO()));
            mD.position(new LatLng(r.getLatitudD(), r.getLongitudD()));
            mMap.addMarker(mO);
            mMap.addMarker(mD);

        }
        else {
            if(r.getLongitudO()!=0&&r.getLatitudO()!=0)
            {
                mO.position(new LatLng(r.getLatitudO(), r.getLongitudO()));
                mMap.addMarker(mO);
            }
            else {
                //TODOS LATITUDES Y LONGITUDES 0 PERO HAY UN ORIGEN EN DIRECCION
                getLatLongFromPlace(r.getOrigen(), v);
            }
        }

    }


    public void getLatLongFromPlace(String place, Context v) {
        if(!place.toLowerCase().contains("CAJAMARCA".toLowerCase()))
        {
            place=place+", Cajamarca";
        }

        try {
            Geocoder selected_place_geocoder = new Geocoder(v);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);

            if (address == null) {
                Toast.makeText(v, "NO DIRECCION", Toast.LENGTH_LONG).show();
            } else {
                mMap.clear();
                Address location = address.get(0);
                Double lat= location.getLatitude();
                Double lng = location.getLongitude();
                MarkerOptions mO= new MarkerOptions();
                BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.blue);
                mO.icon(iconorigen);
                Shear=new LatLng(lat, lng);
                mO.position(Shear);
                mMap.addMarker(mO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fetchLatLongFromService fetch_latlng_from_service_abc = new fetchLatLongFromService(
                    place.replaceAll("\\s+", ""));
            fetch_latlng_from_service_abc.execute();

        }

    }


//Sometimes happens that device gives location = null

    public class fetchLatLongFromService extends
            AsyncTask<Void, Void, StringBuilder> {
        String place;


        public fetchLatLongFromService(String place) {
            super();
            this.place = place;

        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            this.cancel(true);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyB8nh2HxdOLqP3C3SaG-rgPbf_RcwBJ23Y?address="
                        + this.place + "&sensor=false";

                URL url = new URL(googleMapUrl);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                String a = "";
                return jsonResults;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONArray resultJsonArray = jsonObj.getJSONArray("results");

                // Extract the Place descriptions from the results
                // resultList = new ArrayList<String>(resultJsonArray.length());

                JSONObject before_geometry_jsonObj = resultJsonArray
                        .getJSONObject(0);

                JSONObject geometry_jsonObj = before_geometry_jsonObj
                        .getJSONObject("geometry");

                JSONObject location_jsonObj = geometry_jsonObj
                        .getJSONObject("location");

                String lat_helper = location_jsonObj.getString("lat");
                double lat = Double.valueOf(lat_helper);


                String lng_helper = location_jsonObj.getString("lng");
                double lng = Double.valueOf(lng_helper);


                Shear = new LatLng(lat, lng);

                MarkerOptions mO= new MarkerOptions();

                BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.blue);
                mO.icon(iconorigen);
                mO.position(Shear);
                mMap.addMarker(mO);

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }

}