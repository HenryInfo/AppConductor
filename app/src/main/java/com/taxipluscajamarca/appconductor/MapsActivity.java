package com.taxipluscajamarca.appconductor;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.taxipluscajamarca.appconductor.model.Ruta;

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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements  OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1000;
    private GoogleMap mMap;
    Handler mHandler;
    private final static int INTERVAL = 5000;
    SharedPreferences preferences;
    ArrayList<Ruta> lista;
    LinearLayout SolicitudOpciones, solicitudactual, dialogoNotificacion;
    NotificationManager manager;
    String idmovilidad = "";
    EditText txtmovilida, txtchofer;
    Dialog dialog = null;

    Button btnBuscar = null;
    ImageButton btnShowNoti=null, btnHideDialog, btnCallClient, btnNavegar, btnCambiarEstado;
    String pIdChofer, pPassword;
    EditText editText = null;
    LatLng Shear = null, latLngNavegar;
    LocationManager mlocManager;
    GoogleApiClient mGoogleApiClient;
    Location location = null;
    SupportMapFragment mapFragment;
    TextView txtCliente, txtDireccionCliente, txtTelefonoCliente;
    Intent intentTask= null;
    //Notificaciones enviadas
    Map<String, String> arrayNotificaciones = new HashMap<String, String>();
    MetodosAbstractos metodosAbstractos=new MetodosAbstractos();
    Ruta rutaAceptada=null;
    Activity activity;
    ProgressDialog mProgressDialog =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Iniciar Handle para solicitudes Asyncronas
        mHandler = new Handler();
        //Inicializar elementos Android
        //Linears
        SolicitudOpciones = (LinearLayout) findViewById(R.id.SolicitudOpciones);
        solicitudactual = (LinearLayout) findViewById(R.id.SolicitudActualOptiones);
        dialogoNotificacion= (LinearLayout) findViewById(R.id.dialog);
        mProgressDialog = new ProgressDialog(MapsActivity.this);
        //Buttons
        btnShowNoti= (ImageButton) findViewById(R.id.btnShowNotificacion);
        btnHideDialog= (ImageButton) findViewById(R.id.btnHideDialog);
        btnBuscar = (Button) findViewById(R.id.btnBuscar);
        btnCallClient= (ImageButton) findViewById(R.id.btnSolicitudLlamar);
        btnNavegar= (ImageButton) findViewById(R.id.btnNavegar);
        btnCambiarEstado= (ImageButton) findViewById(R.id.btnCambiarEstado);


        //EditText
        editText = (EditText) findViewById(R.id.editText);

        //TextView
        txtCliente= (TextView) findViewById(R.id.txtSolicitudCliente);
        txtDireccionCliente= (TextView) findViewById(R.id.txtSolicitudDireccion);
        txtTelefonoCliente= (TextView) findViewById(R.id.txtSolicitudTelefono);

        //Operaciones cn elementos incializados
        SolicitudOpciones.setVisibility(View.GONE);
        solicitudactual.setVisibility(View.GONE);
        dialogoNotificacion.setVisibility(View.GONE);
        btnNavegar.setVisibility(View.GONE);

        //Eventos Click
        btnShowNoti.setOnClickListener(this);
        btnHideDialog.setOnClickListener(this);
        btnCallClient.setOnClickListener(this);
        btnBuscar.setOnClickListener(this);
        btnNavegar.setOnClickListener(this);
        btnCambiarEstado.setOnClickListener(this);

        //Inicializar servicios
        startService(new Intent(this, ActualizarEstado.class));
        intentTask=new Intent(this, TTSService.class);

        //Metodos Iniciales
        probarCredenciales();
        iniciarServicioGPS();

        activity=this;

    }

    private void iniciarServicioGPS() {

        LocationListener locationListener = new MyLocation();
        mlocManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mlocManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        } else {
            if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }
        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }


    private void iniciarServicioHablar(String key, String text )
    {
        //Key= S"idruta" para notificaciones de solicitud de ruta
        //Key= C"idruta" para notificaciones de solicitud de ruta en camino
        if(!arrayNotificaciones.containsKey(key))
        {
            arrayNotificaciones.put(key, text);
            intentTask.putExtra("talk", text);
            startService(intentTask);
        }

    }
    //comprueba si se tienee permisos para acceder a la ubicacion del dspositivo
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }

            return true;
        }
    }

    private void probarCredenciales() {

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pIdChofer = preferences.getString("pIdChofer", "");
        pPassword = preferences.getString("pPassword", "");
        String estadoCambiar = preferences.getString("estadoCambiar", "1");
        if(equals(estadoCambiar, "2")){
            btnCambiarEstado.setImageResource(R.drawable.warning);
        }

        if (!equals(pIdChofer,"") && !equals(pPassword, "")) {

            idmovilidad = preferences.getString("pIdMovilidad", "");
            String idruta = preferences.getString("pIdSolicitud", "");
            if (equals(idruta,""))
            {
                starTraerPedidosTask();
                ProgressDialog mProgressDialog = new ProgressDialog(MapsActivity.this);
                mProgressDialog.setMessage("Probando conexión a internet");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                /*mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //   downloadTask.cancel(true);
                    }
                });
                */
                UpdateEstado c= new UpdateEstado();
                c.CambiarEstado(mProgressDialog, getBaseContext(), "0", 0, Integer.parseInt(idmovilidad), "1");

            }
            else {
                mProgressDialog.setMessage("Probando conexión a internet");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //   downloadTask.cancel(true);
                    }
                });
                UpdateEstado c= new UpdateEstado();
                c.CambiarEstado(mProgressDialog, getBaseContext(), "0", 0, Integer.parseInt(idmovilidad), "2");
                solicitudactual.setVisibility(View.VISIBLE);
                SolicitudOpciones.setVisibility(View.GONE);
                starTraerSolicitudTask();
            }

            boolean estado = loginConductorAzync((pIdChofer), pPassword);
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) getBaseContext().getSystemService(ns);
            nMgr.cancel(11);//elimina notificcaciones de nuevas solicitudes
        } else {
            MostrarLogin();
        }
    }
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(getCameraChangeListener());

        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        MyLocation Local = new MyLocation();
        Local.setMapsActivity(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Criteria criteria = new Criteria();
        location = mlocManager.getLastKnownLocation(mlocManager.getBestProvider(criteria, false));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (location != null) {
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 40000, null);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.5f), 4000, null);

                    // LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                }
            }
        } else {
            mMap.setMyLocationEnabled(true);
            if (location != null) {

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }


        }
        //mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
    }
    public GoogleMap.OnCameraChangeListener getCameraChangeListener()
    {
        return new GoogleMap.OnCameraChangeListener()
        {
            @Override
            public void onCameraChange(CameraPosition position)
            {
                int mCameraTilt = (position.zoom < 15) ? 0 : 60;
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(position.target)
                                .tilt(mCameraTilt)
                                .zoom(position.zoom)
                                .build()));
            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        super.onCreateOptionsMenu(menu);
        return  true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navCerrarApp:
                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("¿Está seguro de salir de la vi" +
                        "sta de los clientes?");
                //dialog.setMessage("Ped");
                //final Ruta ruta1 = ruta;
                dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if(MapsActivity.equals(preferences.getString("pIdSolicitud", ""), ""))
                        {
                            Toast.makeText(getApplicationContext(), "Saliendo de la aplicación", Toast.LENGTH_SHORT).show();
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "No puede salir de la aplicación tiene una solicitud en proceso", Toast.LENGTH_SHORT).show();

                        }
                    }
                }).setNegativeButton("No ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getContext(), "No se ", Toast.LENGTH_LONG).show();
                    }
                });

                final AlertDialog alert = dialog.create();
                alert.show();
                break;
        }
        return true;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //Ventana para mostrar el login
    public void MostrarLogin() {

        dialog = new Dialog(MapsActivity.this);
        dialog.setContentView(R.layout.dialog);
        Button b = (Button) dialog.findViewById(R.id.guardar);
        txtchofer = (EditText) dialog.findViewById(R.id.chofer);
        dialog.setCancelable(false);
        txtmovilida = (EditText) dialog.findViewById(R.id.movilidadtxt);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(view.getId()==R.id.guardar)
                {
                    if (!TextUtils.isEmpty(txtchofer.getText()) && !TextUtils.isEmpty(txtmovilida.getText())) {
                        pIdChofer = (txtchofer.getText().toString());
                        pPassword = (txtmovilida.getText().toString());
                        boolean estado = loginConductorAzync(pIdChofer, pPassword);

                    } else {
                        Toast.makeText(getApplicationContext(), "Ingrese los datos", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        if(!dialog.isShowing())
            dialog.show();
    }


    //Alerta para que el usuario activa el GPS
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        alertDialogBuilder.setMessage("GPS está desactivado en tu dispositivo, para un mejor servicio debe estar activado.")
                .setCancelable(false)
                .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                        mapFragment.getMapAsync(MapsActivity.this);
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        if(!this.isFinishing()){
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();

        }
    }

    //Prueba si el conductor esta habilidado para el logico y prueba si el pass es correco
    public boolean loginConductorAzync(final String IdChofer, final String pPassword) {
        final boolean[] estadoL = {false};
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                JSONObject jsonObject = new JSONObject();
                String body = "";
                //progress.setTitle("Consultando Dni");
                //Toast.makeText(view.getContext(), "Actualizando", Toast.LENGTH_SHORT).show();
                try {

                    jsonObject.put("idchofer", String.valueOf(IdChofer));
                    jsonObject.put("password", pPassword);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/usuarios/loginchofer");
                    httpPost.setEntity(new StringEntity(jsonObject.toString()));
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    HttpResponse response = httpClient.execute(httpPost);
                    body = MetodosAbstractos.getResponseBody(response);

                } catch (ClientProtocolException e) {
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                } catch (IOException ee) {

                }
                return body;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                JSONObject respuesta = null;

                try {
                    respuesta = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject array;
                    if (respuesta != null) {
                        String estado = respuesta.getString("estado");
                        switch (estado) {
                            case "1":
                                estadoL[0] = true;
                                array = respuesta.getJSONObject("usuario");

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("pIdChofer", String.valueOf(pIdChofer));
                                editor.putString("pIdMovilidad", array.getString("idmovilidad"));
                                editor.putString("pNPlaca", array.getString("nplaca"));
                                editor.putString("pNumeroInterno", array.getString("interno"));
                                editor.putString("pPassword", (pPassword));
                                editor.apply();
                                idmovilidad = preferences.getString("pIdMovilidad", "");
                                if (dialog != null)
                                    dialog.dismiss();

                                //traer Notificaciones
                                String idruta = preferences.getString("pIdSolicitud", "");
                                if(MapsActivity.equals(idruta,""))
                                    starTraerPedidosTask();

                                break;
                            default:
                                if (dialog != null) {
                                    if (!dialog.isShowing()) {
                                        MostrarLogin();
                                        Toast.makeText(getApplicationContext(), "Usted ha sido dado de baja, ir al administrador para activar cuenta", Toast.LENGTH_LONG).show();


                                    } else {
                                        Toast.makeText(getApplicationContext(), "Los datos ingresados son incorrectos o ha sido dado de baja", Toast.LENGTH_LONG).show();

                                    }

                                } else {

                                    MostrarLogin();
                                    Toast.makeText(getApplicationContext(), "Los datos ingresados son incorrectos o ha sido dado de baja", Toast.LENGTH_LONG).show();

                                }
                                estadoL[0] = false;

                                break;
                        }
                    }

                } catch (Exception e) {

                    estadoL[0] = false;
                    e.printStackTrace();
                }

            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();
        return estadoL[0];
    }

    //Actualiza La ubicacion de la movilidad
    public void ActualizarUbicacionAzync() {

        /*final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                    */
        if (!mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
        try {
            if (!MapsActivity.equals(idmovilidad,"") && location != null) {

                class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
                    public  boolean equals(Object a, Object b) {
                        return (a == b) || (a != null && a.equals(b));
                    }
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    protected String doInBackground(String... params) {
                        JSONObject jsonObject = new JSONObject();
                        String body = "";
                        //progress.setTitle("Consultando Dni");
                        //Toast.makeText(view.getContext(), "Actualizando", Toast.LENGTH_SHORT).show();
                        try {

                            jsonObject.put("idmovilidad", String.valueOf(idmovilidad));

                            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String idruta = preferences.getString("pIdSolicitud", "");
                            String estadoCambiar = preferences.getString("estadoCambiar", "1");
                            if(equals(idruta, "")&&equals(estadoCambiar,"1"))
                            {
                                jsonObject.put("estado", "1");
                                Log.d("NOMAMES", "cambio a estado 1");

                            }
                            else {
                                jsonObject.put("estado", "2");
                            }
                            jsonObject.put("longitud", location.getLongitude());
                            jsonObject.put("latitud", location.getLatitude());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/movilidades/actualizar");
                            httpPost.setEntity(new StringEntity(jsonObject.toString()));
                            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                            HttpResponse response = httpClient.execute(httpPost);
                            body = MetodosAbstractos.getResponseBody(response);

                        } catch (ClientProtocolException e) {
                            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                        } catch (IOException ee) {

                        }
                        return body;
                    }

                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        JSONObject respuesta = null;
                        try {
                            if(result!=null)
                            respuesta = new JSONObject(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            JSONArray array;
                            if (respuesta != null) {

                                // listView.setAdapter(new CustomArrayAdapter(getContext(), lista, progress));

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
                sendPostReqAsyncTask.execute();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
         /*
                    }
                });

            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
*/
    }

    //Trae los datos de la solicud que está en la actualidad
    public void getRutaActualAzync() {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                JSONObject jsonObject = new JSONObject();
                String body = "";
                String idruta = preferences.getString("pIdSolicitud", "");
                //progress.setTitle("Consultando Dni");
                //Toast.makeText(view.getContext(), "Actualizando", Toast.LENGTH_SHORT).show();
                try {
                    jsonObject.put("idruta", idruta);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/rutas/getrutaid");
                    httpPost.setEntity(new StringEntity(jsonObject.toString()));
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    HttpResponse response = httpClient.execute(httpPost);
                    body = MetodosAbstractos.getResponseBody(response);

                } catch (ClientProtocolException e) {
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                } catch (IOException ee) {

                }
                return body;
            }

            public boolean equals(Object a, Object b) {
                return (a == b) || (a != null && a.equals(b));
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                JSONObject respuesta = null;
                try {
                    respuesta = new JSONObject(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONObject rutaJSON;
                    if (respuesta != null&& !equals(respuesta.getString("ruta"), "false")) {
                        String  nombreCliente="", telefono="";
                        rutaJSON = respuesta.getJSONObject("ruta");
                        int idruta=Integer.parseInt(rutaJSON.getString("idruta"));
                        int idmovilidad=Integer.parseInt(rutaJSON.getString("idmovilidad"));
                        Double latitudO=Double.parseDouble(rutaJSON.getString("latitudo"));
                        Double latitudD=Double.parseDouble(rutaJSON.getString("latitudd"));
                        Double longitudO=Double.parseDouble(rutaJSON.getString("longitudo"));
                        Double longitudD=0.0;
                        nombreCliente=rutaJSON.getString("nombre")+" "+rutaJSON.getString("apaterno")+ " "+rutaJSON.getString("amaterno");
                        telefono=rutaJSON.getString("telefono");
                        if(!rutaJSON.isNull("longitudd"))
                            longitudD=Double.parseDouble(rutaJSON.getString("longitudd"));
                        char estado=rutaJSON.getString("estado").charAt(0);
                        Double precio=Double.parseDouble(rutaJSON.getString("precio"));
                        String origen=(rutaJSON.getString("origen"));
                        String destino=(rutaJSON.getString("destino"));
                        if(longitudO==0||latitudO==0)
                        {
                            LatLng ltg=getLaltLongPlace(origen);
                            if(ltg!=null)
                            {
                                longitudO=ltg.longitude;
                                latitudO=ltg.latitude;
                            }
                        }
                        if(equals(destino, "0"))
                        {
                            destino="No destino";
                        }
                        final Ruta r =new Ruta(
                                idruta,
                                idmovilidad,
                                latitudO,
                                latitudD,
                                longitudO,
                                longitudD,
                                estado,
                                precio,
                                origen,
                                destino
                        );
                        if(estado=='6')
                        {
                            stopTraerPedidosTask();
                            rutaAceptada=r;
                            solicitudactual.findViewById(R.id.btnLlegue).setVisibility(View.GONE);
                            solicitudactual.findViewById(R.id.btnCancelar).setVisibility(View.GONE);
                            solicitudactual.findViewById(R.id.btnTermine).setVisibility(View.VISIBLE);
                            agregarIcono("IC"+r.getId(), new LatLng(r.getLatitudO(), r.getLongitudO()));
                            //Mostrar direccion a donde tiene que ir el taxista
                            if(r.getOrigen()!=null)
                            {
                                txtDireccionCliente.setText(r.getOrigen());
                            }

                            if(r.getLatitudO()!=null &&r.getLongitudO()!=null)
                            {
                                btnNavegar.setVisibility(View.VISIBLE);
                            }else
                                btnNavegar.setVisibility(View.GONE);
                            if(!equals(nombreCliente, ""))
                                txtCliente.setText(nombreCliente);
                            if(!equals(telefono, ""))
                            {
                                txtTelefonoCliente.setText(telefono);
                            }

                        }

                        if(estado=='1'){
                            iniciarServicioHablar(String.valueOf("S"+r.getId()), "Se solicita servicio a " + r.getOrigen());
                            //findViewById(R.id.btnLlegue).setVisibility(View.GONE);
                            findViewById(R.id.btnAceptar).setVisibility(View.VISIBLE);
                            findViewById(R.id.btnRechazar).setVisibility(View.VISIBLE);
                            SolicitudOpciones.setVisibility(View.VISIBLE);
                            btnShowNoti.setVisibility(View.VISIBLE);
                            agregarIcono("IC"+r.getId(), new LatLng(r.getLatitudO(), r.getLongitudO()));
                            //Mostrar direccion a donde tiene que ir el taxista
                            if(r.getOrigen()!=null)
                            {
                                txtDireccionCliente.setText(r.getOrigen());
                            }

                            if(r.getLatitudO()!=null &&r.getLongitudO()!=null)
                            {
                                btnNavegar.setVisibility(View.VISIBLE);
                                latLngNavegar=new LatLng(r.getLatitudO(), r.getLongitudO());
                            }else
                                btnNavegar.setVisibility(View.GONE);
                            if(!equals(nombreCliente, ""))
                                txtCliente.setText(nombreCliente);
                            if(!equals(telefono, ""))
                            {
                                txtTelefonoCliente.setText(telefono);
                            }

                        }
                        if(estado=='2')
                        {
                            solicitudactual.setVisibility(View.VISIBLE);
                            SolicitudOpciones.setVisibility(View.GONE);
                            stopTraerPedidosTask();
                            rutaAceptada=r;
                            solicitudactual.findViewById(R.id.btnLlegue).setVisibility(View.VISIBLE);
                            btnShowNoti.setVisibility(View.VISIBLE);
                            solicitudactual.findViewById(R.id.btnCancelar).setVisibility(View.VISIBLE);
                            solicitudactual.findViewById(R.id.btnTermine).setVisibility(View.GONE);
                            agregarIcono("IC"+r.getId(), new LatLng(r.getLatitudO(), r.getLongitudO()));
                            //Mostrar direccion a donde tiene que ir el taxista
                            if(r.getOrigen()!=null)
                            {
                                txtDireccionCliente.setText(r.getOrigen());
                            }

                            if(r.getLatitudO()!=null &&r.getLongitudO()!=null)
                            {
                                btnNavegar.setVisibility(View.VISIBLE);
                                latLngNavegar=new LatLng(r.getLatitudO(), r.getLongitudO());
                            }else
                                btnNavegar.setVisibility(View.GONE);
                            if(!equals(nombreCliente, ""))
                                txtCliente.setText(nombreCliente);
                            if(!equals(telefono, ""))
                            {
                                txtTelefonoCliente.setText(telefono);
                            }
                        }
                        if(estado=='3'||estado=='5'||estado=='4')
                        {
                            mMap.clear();
                            btnCambiarEstado.setVisibility(View.VISIBLE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("pIdSolicitud", "");
                            editor.apply();
                            btnNavegar.setVisibility(View.GONE);
                            stopService(intentTask);
                            if(estado=='5')
                            {
                                iniciarServicioHablar("C"+r.getId(),"El servicio ha sido cancelado");
                            }
                            starTraerPedidosTask();
                            stopTraerSolicitudTask();
                            btnShowNoti.setVisibility(View.GONE);
                            solicitudactual.setVisibility(View.GONE);
                        }else{
                            btnCambiarEstado.setVisibility(View.GONE);
                            new EstadoSolicitud(MapsActivity.this, getApplicationContext(), r, mMap, SolicitudOpciones, solicitudactual, preferences, intentTask, dialogoNotificacion, btnShowNoti, btnNavegar);

                        }


                    }
                    else {
                        stopTraerSolicitudTask();
                        starTraerPedidosTask();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute();

    }

    //Agrgar Marker
    private void agregarIcono(String key, LatLng latLng) {
        if(arrayNotificaciones.containsKey(key))
        {
            arrayNotificaciones.put(key, "Nuev");
            MarkerOptions mO = new MarkerOptions();
            BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.blue);
            mO.icon(iconorigen);
            mO.position(new LatLng(latLng.latitude,latLng.longitude));
            mMap.addMarker(mO);
        }

    }

    //Trae los pedidos asignados al chofer
    public void TraerPedidosAzync(final int idchofer) {
        try {
            class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
                @Override
                protected String doInBackground(String... params) {
                    JSONObject jsonObject = new JSONObject();
                    String body = "";
                    //progress.setTitle("Consultando Dni");
                    //Toast.makeText(view.getContext(), "Actualizando", Toast.LENGTH_SHORT).show();
                    try {
                        jsonObject.put("idchofer", idchofer);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("https://reserva.taxipluscajamarca.com/webapi/v1/rutas/mipedido");
                        httpPost.setEntity(new StringEntity(jsonObject.toString()));
                        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                        HttpResponse response = httpClient.execute(httpPost);
                        body = MetodosAbstractos.getResponseBody(response);

                    } catch (ClientProtocolException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                    } catch (IOException ee) {
                        ee.getMessage();

                    }
                    return body;
                }

                public boolean equals(Object a, Object b) {
                    return (a == b) || (a != null && a.equals(b));
                }

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    lista = new ArrayList<Ruta>();
                    JSONObject respuesta = null;
                    try {
                        if(result!=null)
                            respuesta = new JSONObject(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        JSONArray array;
                        if (respuesta != null) {
                            Intent intent = new Intent(getBaseContext(), MapsActivity.class);

                            PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity.this, 1, intent, 0);

                            Notification.Builder builder = new Notification.Builder(MapsActivity.this);

                            String men = "", mensolo = "", nombreCliente="", telefono="";
                            array = respuesta.getJSONArray("resultado");
                            if(array.length()==0)
                            {
                                dialogoNotificacion.setVisibility(View.GONE);
                            }
                            for (int i = 0; i < array.length(); i++) {
                                int idmovilidad = 0;

                                double latitudo = 0.0;
                                double latitudd = 0.0;
                                double longitudo = 0.0;
                                double longitudd = 0.0;
                                if (!array.getJSONObject(i).isNull("idmovilidad")) {
                                    idmovilidad = Integer.parseInt(array.getJSONObject(i).get("idmovilidad").toString());

                                }
                                if (!array.getJSONObject(i).isNull("latitudo") && array.getJSONObject(i).get("latitudo") != "") {
                                    latitudo = Double.parseDouble(array.getJSONObject(i).get("latitudo").toString());

                                }
                                if (!array.getJSONObject(i).isNull("latitudd") && array.getJSONObject(i).get("latitudd") != "") {
                                    latitudd = Double.parseDouble(array.getJSONObject(i).get("latitudd").toString());

                                }
                                if (!array.getJSONObject(i).isNull("longitudo") && array.getJSONObject(i).get("longitudo") != "") {
                                    longitudo = Double.parseDouble(array.getJSONObject(i).get("longitudo").toString());

                                }
                                if (!array.getJSONObject(i).isNull("longitudd") && array.getJSONObject(i).get("longitudd" +
                                        "") != "") {
                                    longitudd = Double.parseDouble(array.getJSONObject(i).get("longitudd").toString());

                                }
                                if (!array.getJSONObject(i).isNull("nombre") && array.getJSONObject(i).get("nombre" +
                                        "") != "") {
                                    nombreCliente = array.getJSONObject(i).get("nombre").toString();

                                }
                                if (!array.getJSONObject(i).isNull("apaterno") && array.getJSONObject(i).get("apaterno" +
                                        "") != "") {
                                    nombreCliente = nombreCliente +" "+ array.getJSONObject(i).get("apaterno").toString();

                                }
                                if (!array.getJSONObject(i).isNull("amaterno") && array.getJSONObject(i).get("amaterno" +
                                        "") != "") {
                                    nombreCliente = nombreCliente  +" "+  array.getJSONObject(i).get("amaterno").toString();

                                }
                                if (!array.getJSONObject(i).isNull("telefono") && array.getJSONObject(i).get("telefono" +
                                        "") != "") {
                                    telefono = array.getJSONObject(i).get("telefono").toString();

                                }

                                Ruta ruta = new Ruta(
                                        Integer.parseInt(array.getJSONObject(i).get("idruta").toString()),
                                        idmovilidad,
                                        latitudo,
                                        latitudd,
                                        longitudo,
                                        longitudd,
                                        array.getJSONObject(i).get("estado").toString().charAt(0),
                                        Double.parseDouble(array.getJSONObject(i).get("precio").toString()),
                                        array.getJSONObject(i).get("origen").toString(),
                                        array.getJSONObject(i).get("destino").toString()

                                );
                                lista.add(ruta);
                                if(lista.size()!=0)
                                {
                                    btnCambiarEstado.setVisibility(View.VISIBLE);

                                }else {
                                    btnCambiarEstado.setVisibility(View.GONE);
                                }

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("pIdSolicitud", String.valueOf(ruta.getId()));
                                editor.apply();
                                builder.setAutoCancel(false);
                                builder.setTicker("!Necesitamos de tu apoyo!");
                                builder.setContentTitle("Tiene Nueva Solicitud");
                                mensolo = "Alguien necesita de sus servicios";
                                men = "Alguien necesita de sus servicios";
                                if (ruta.getOrigen() != null || ruta.getDestino() != null)
                                {
                                    men += " desde " + ruta.getOrigen() + " hasta " + ruta.getDestino();
                                }


                                //Mostrar btn Azul izquierdo si no esta visible el dialog principal de hacer pedido
                                if(dialogoNotificacion.getVisibility()!=View.VISIBLE)
                                    btnShowNoti.setVisibility(View.VISIBLE);

                                //Mostrar direccion a donde tiene que ir el taxista
                                if(ruta.getOrigen()!=null)
                                {
                                    txtDireccionCliente.setText(ruta.getOrigen());
                                    SolicitudOpciones.setVisibility(View.VISIBLE);

                                }
                                else
                                {
                                    txtDireccionCliente.setText("Error en los datos ingresados por el cliente");
                                    SolicitudOpciones.setVisibility(View.GONE);

                                }

                                if(!equals(nombreCliente, ""))
                                    txtCliente.setText(nombreCliente);
                                if(!equals(telefono, ""))
                                {
                                    txtTelefonoCliente.setText(telefono);
                                }

                                if(ruta.getLatitudO()!=null &&ruta.getLongitudO()!=null)
                                {
                                    btnNavegar.setVisibility(View.VISIBLE);
                                    latLngNavegar=new LatLng(ruta.getLatitudO(), ruta.getLongitudO());
                                }else
                                    btnNavegar.setVisibility(View.GONE);
                                //Para Dibujar ruta
                                rutaAceptada=ruta;

                                //iniciarServicioHablar(String.valueOf("S"+ruta.getId()), "Se solicita servicio a " + ruta.getOrigen());

                                break;

                            }


                            if (lista.size() > 0) {
                                mMap.clear();
                                stopTraerPedidosTask();
                                starTraerSolicitudTask();
                                builder.setContentText(mensolo);
                                builder.setSmallIcon(R.drawable.blue);
                                builder.setContentIntent(pendingIntent);
                                builder.setOngoing(true);
                                builder.setSubText(men);   //API level 16
                                builder.setNumber(100);
                                builder.build();
                                Notification myNotication = builder.getNotification();
                                manager.notify(11, myNotication);
                                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                v.vibrate(500);
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


                            } else {
                                SolicitudOpciones.setVisibility(View.GONE);
                                btnCambiarEstado.setVisibility(View.VISIBLE);
                                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor=preferences.edit();
                                editor.putString("pIdSolicitud", "");
                                editor.apply();
                                String ns = Context.NOTIFICATION_SERVICE;
                                NotificationManager nMgr = (NotificationManager) getBaseContext().getSystemService(ns);
                                nMgr.cancel(11);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
            sendPostReqAsyncTask.execute();
        } catch (Exception e) {
            e.getMessage();
            // TODO Auto-generated catch block
        }
    }

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            TraerPedidosAzync(Integer.parseInt(pIdChofer));
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    public  void starTraerPedidosTask() {
        mHandlerTask.run();
    }

    public void stopTraerPedidosTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    Runnable mHandlerTaskMiSolicitud = new Runnable() {
        @Override
        public void run() {
            getRutaActualAzync();
            mHandler.postDelayed(mHandlerTaskMiSolicitud, INTERVAL);
        }
    };

    public void starTraerSolicitudTask() {
        mHandlerTaskMiSolicitud.run();
    }

    public void stopTraerSolicitudTask() {
        mHandler.removeCallbacks(mHandlerTaskMiSolicitud);
    }


    //Obtiene la latitud y longitur a partir del nombre de un lugar --Metodo para asignar la latitud y longitud para la busqueda del conductor
    public  void getLatLongFromPlace(String place) {
        if (!place.toLowerCase().contains("CAJAMARCA".toLowerCase())) {
            place = place + ", Cajamarca";
        }

        try {
            Geocoder selected_place_geocoder = new Geocoder(getApplicationContext());
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);

            if (address == null) {
                Toast.makeText(getApplicationContext(), "NO ENCONTRADA", Toast.LENGTH_LONG).show();
            } else {
                Address location = address.get(0);
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                MarkerOptions mO = new MarkerOptions();
                BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.green);
                mO.icon(iconorigen);
                Shear = new LatLng(lat, lng);
                mO.position(Shear);
                mMap.addMarker(mO);
                latLngNavegar=Shear;
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Shear, 16.0f));
                btnNavegar.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "NO ENCONTRADA, REINTENTANDO", Toast.LENGTH_LONG).show();
            fetchLatLongFromService fetch_latlng_from_service_abc = new fetchLatLongFromService(
                    place.replaceAll("\\s+", ""));
            fetch_latlng_from_service_abc.execute();

        }

    }

    //Obtiene la latitud y longitur a partir del nombre de un lugar --Metodo para asignar la latitud y longitud si es que no se tiene en pedido
    public LatLng getLaltLongPlace(String place) {
        if (!place.toLowerCase().contains("CAJAMARCA".toLowerCase())) {
            place = place + ", Cajamarca";
        }
        LatLng ltlng=null;
        try {
            Geocoder selected_place_geocoder = new Geocoder(getApplicationContext());
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);

            if (address == null) {
                Toast.makeText(getApplicationContext(), "NO ENCONTRADA", Toast.LENGTH_LONG).show();
            } else {
                Address location = address.get(0);
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                ltlng= new LatLng(lat,lng);
                MarkerOptions mO = new MarkerOptions();
                BitmapDescriptor iconorigen = BitmapDescriptorFactory.fromResource(R.drawable.green);
                mO.icon(iconorigen);
                Shear = new LatLng(lat, lng);
                mO.position(Shear);
                mMap.addMarker(mO);
                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Shear, 16.0f));
                btnNavegar.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "ERROR, MODIFIQUE SU BUSQUEDA", Toast.LENGTH_LONG).show();

        }
        return ltlng;

    }

    //Iniciar Google Direction para navegar por Google Maps
    public void Navegar(LatLng Shear) {
        if (Shear != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + Shear.latitude + "," + Shear.longitude));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("NOMAMES", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("NOMAMES", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NOMAMES", "onDestroy");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnHideDialog:
                dialogoNotificacion.setVisibility(LinearLayout.GONE);
                Animation animation   = AnimationUtils.loadAnimation(this, R.anim.desanim);
                animation.setDuration(500);
                dialogoNotificacion.setAnimation(animation);
                dialogoNotificacion.animate();
                animation.start();
                btnShowNoti.setVisibility(View.VISIBLE);
                break;
            case R.id.btnShowNotificacion://Mostrar animacion con los datos
                dialogoNotificacion.setVisibility(LinearLayout.VISIBLE);
                Animation animation1  = AnimationUtils.loadAnimation(this, R.anim.anim);
                animation1.setDuration(500);
                dialogoNotificacion.setAnimation(animation1);
                dialogoNotificacion.animate();
                animation1.start();
                btnShowNoti.setVisibility(View.INVISIBLE);
                break;
            case R.id.btnNavegar:
                Navegar(latLngNavegar);
                break;
            case R.id.btnBuscar:
                if (editText.isShown()) {
                    if (TextUtils.isEmpty(editText.getText())) {
                        editText.setVisibility(View.GONE);
                    } else {
                        if (equals(editText.getText().toString(), "")) {
                            editText.setVisibility(View.GONE);
                        } else {
                            getLatLongFromPlace(editText.getText().toString());
                            Toast.makeText(getApplicationContext(), "BUSCANDO......... ", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    editText.setVisibility(View.VISIBLE);

                }
                break;
            case R.id.btnSolicitudLlamar:
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
                } else {
                    callPhone();
                }
                break;
            case R.id.btnCambiarEstado:
                String estadoCambiar = preferences.getString("estadoCambiar", "1");
                SharedPreferences.Editor editor = preferences.edit();

                if(equals(estadoCambiar,"1"))
                {
                    editor.putString("estadoCambiar", "2");
                    stopService(intentTask);
                    stopTraerSolicitudTask();
                    stopTraerPedidosTask();
                    ProgressDialog mProgressDialog = new ProgressDialog(MapsActivity.this);
                    mProgressDialog.setMessage("Actualizando a ocupado");
                    //mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mProgressDialog.setCancelable(false);
                    /*mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //   downloadTask.cancel(true);
                        }
                    });
                    */
                    UpdateEstado c= new UpdateEstado();
                    c.CambiarEstado(mProgressDialog, getBaseContext(), "0", 0, Integer.parseInt(idmovilidad), "2");
                    btnCambiarEstado.setImageResource(R.drawable.warning);
                   // Toast.makeText(getApplicationContext(), "Actualizando a Ocupado", Toast.LENGTH_SHORT).show();

                }
                else {
                    editor.putString("estadoCambiar", "1");
                    starTraerPedidosTask();
                    starTraerSolicitudTask();
                    ProgressDialog mProgressDialog = new ProgressDialog(MapsActivity.this);
                    mProgressDialog.setMessage("Ahora está disponible");
                   // mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                   /* mProgressDialog.setCancelable(true);
                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            //   downloadTask.cancel(true);
                        }
                    });*/
                    UpdateEstado c= new UpdateEstado();
                    c.CambiarEstado(mProgressDialog, getBaseContext(), "0", 0, Integer.parseInt(idmovilidad), "1");
                    btnCambiarEstado.setImageResource(R.drawable.success);
                    //Toast.makeText(getApplicationContext(), "Ahora está disponible", Toast.LENGTH_SHORT).show();

                }
                editor.apply();

                break;
        }
    }
    private void callPhone() {
        if(!TextUtils.isEmpty(txtTelefonoCliente.getText())&&dialogoNotificacion.getVisibility()==View.VISIBLE)
        {
            Intent intent = new Intent(Intent.ACTION_CALL);

            String telefonoMyTaxi=txtTelefonoCliente.getText().toString();
            intent.setData(Uri.parse("tel:" + telefonoMyTaxi));
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            }
        }

    }

    //TODO:Mostrar ruta hacia pedido
    void mostrarRuta(Location location){

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String idruta = preferences.getString("pIdSolicitud", "");
        if(!equals(idruta, "")||rutaAceptada!=null){

            metodosAbstractos.dibujarRuta(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    new LatLng(rutaAceptada.getLatitudO(), rutaAceptada.getLongitudO()),
                    mMap,
                    null
            );

        }

    }



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
                if(result!=null){
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
                    latLngNavegar=Shear;
                    // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(Shear, 16.0f));
                    btnNavegar.setVisibility(View.VISIBLE);
                }


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }

    //Inciializa el api de Google maps
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Clase que escucha los eventos del GPS
    public class MyLocation implements LocationListener {
        MapsActivity mapsActivity;

        public MapsActivity getMapsActivity() {
            return mapsActivity;
        }

        public void setMapsActivity(MapsActivity mapsActivity) {
            this.mapsActivity = mapsActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            location = loc;
            loc.getLatitude();

            //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
            ActualizarUbicacionAzync();
            //Dibujar ruta
            //if(rutaAceptada!=null)
            // mostrarRuta(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            // Toast.makeText(getApplicationContext(), "GPS Desactivado", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            //Toast.makeText(getApplicationContext(), "GPS Activado", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Este metodo se ejecuta cada vez que se detecta un cambio en el
            // status del proveedor de localizacion (GPS)
            // Los diferentes Status son:
            // OUT_OF_SERVICE -> Si el proveedor esta fuera de servicio
            // TEMPORARILY_UNAVAILABLE -> Temporalmente no disponible pero se
            // espera que este disponible en breve
            // AVAILABLE -> Disponible
        }

    }/* Fin de la clase localizacion */

}
