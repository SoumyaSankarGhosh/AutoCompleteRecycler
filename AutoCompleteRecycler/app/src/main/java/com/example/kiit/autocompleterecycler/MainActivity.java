package com.example.kiit.autocompleterecycler;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.kiit.autocompleterecycler.adapter.PlacesAutoCompleteAdapter;

import com.example.kiit.autocompleterecycler.utilities.RecyclerItemClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,View.OnClickListener{

    protected GoogleApiClient mGoogleApiClient;

    private static final LatLngBounds BOUNDS_INDIA = new LatLngBounds(
            new LatLng(22.5726, 88.3639), new LatLng(22.5958, 88.2636));

    private EditText mAutocompleteView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    ImageView delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mAutoCompleteAdapter =  new PlacesAutoCompleteAdapter(this, R.layout.search_row, BOUNDS_INDIA, null);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();

        initial();


        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAutoCompleteAdapter);
        delete.setOnClickListener(this);


        autotextChage();

        itemTouchListener();


    }

   public void initial(){
         mAutocompleteView = (EditText)findViewById(R.id.autocomplete_places);

         delete=(ImageView)findViewById(R.id.cross);


          mRecyclerView=(RecyclerView)findViewById(R.id.recyclerView);
          mLinearLayoutManager=new LinearLayoutManager(this);
   }

   public void autotextChage(){
       mAutocompleteView.addTextChangedListener(new TextWatcher() {

           public void onTextChanged(CharSequence s, int start, int before,
                                     int count) {
               if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                   mAutoCompleteAdapter.getFilter().filter(s.toString());
               }else if(!mGoogleApiClient.isConnected()){
                   Toast.makeText(getApplicationContext(), Constants.API_NOT_CONNECTED,Toast.LENGTH_SHORT).show();
                   Log.e(Constants.PlacesTag,Constants.API_NOT_CONNECTED);
               }

           }

           public void beforeTextChanged(CharSequence s, int start, int count,
                                         int after) {

           }

           public void afterTextChanged(Editable s) {

           }
       });
   }

   public void itemTouchListener(){
       mRecyclerView.addOnItemTouchListener(
               new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                   @Override
                   public void onItemClick(View view, int position) {
                       final PlacesAutoCompleteAdapter.PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                       final String placeId = String.valueOf(item.placeId);
                       Log.i("TAG", "Autocomplete item selected: " + item.description);
                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

                       PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                               .getPlaceById(mGoogleApiClient, placeId);
                       placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                           @Override
                           public void onResult(PlaceBuffer places) {
                               if(places.getCount()==1){
                                   //Do the things here on Click.....
                                  // Toast.makeText(getApplicationContext(),String.valueOf(places.get(0).getLatLng()),Toast.LENGTH_SHORT).show();
                                   Toast.makeText(getApplicationContext(),String.valueOf(places.get(0).getAddress()),Toast.LENGTH_SHORT).show();
                               }else {
                                   Toast.makeText(getApplicationContext(),Constants.SOMETHING_WENT_WRONG,Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                       Log.i("TAG", "Clicked: " + item.description);
                       Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);
                   }
               })
       );

   }

    @Override
    public void onConnected(Bundle bundle) {
        mAutoCompleteAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.v("Google API Callback", "Connection Done");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mAutoCompleteAdapter.setGoogleApiClient(null);
        Log.v("Google API Callback", "Connection Suspended");
        Log.v("Code", String.valueOf(i));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("Google API Callback","Connection Failed");
        Log.v("Error Code", String.valueOf(connectionResult.getErrorCode()));
        Toast.makeText(this, Constants.API_NOT_CONNECTED,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if(v==delete){
            mAutocompleteView.setText("");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
            Log.v("Google API","Connecting");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            Log.v("Google API","Dis-Connecting");
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
