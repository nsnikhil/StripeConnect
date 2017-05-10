package com.nrs.nsnik.stripeconnect;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.nrs.nsnik.stripeconnect.fragemnts.dialogFragments.LoadingDialogFragment;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.FileUpload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainToolbar) Toolbar mMainToolbar;
    @BindView(R.id.mainContainer) LinearLayout mMainContainer;
    @BindView(R.id.testButton) Button tTestbutton;
    @BindView(R.id.testEmail) TextInputEditText tTestEmail;
    @BindView(R.id.testFName) TextInputEditText tTestFName;
    @BindView(R.id.testLName) TextInputEditText tTestLName;

    @BindView(R.id.testCountry) TextInputEditText tTestCountry;
    @BindView(R.id.testCurrency) TextInputEditText tTestLCurrency;
    @BindView(R.id.testRoutingno) TextInputEditText tTestRoutingNo;
    @BindView(R.id.testAccountno) TextInputEditText tTestAccountNo;
    @BindView(R.id.testAddress) TextInputEditText tTestAddressLine;
    @BindView(R.id.testPostal) TextInputEditText tTestPostalCode;
    @BindView(R.id.testCity) TextInputEditText tTestCity;
    @BindView(R.id.testState) TextInputEditText tTestState;
    @BindView(R.id.testPid) TextInputEditText tPid;

    @BindView(R.id.testModeSwitch) Switch tModChange;

    @BindView(R.id.testDob) TextView tDob;
    LoadingDialogFragment mLoadingDialog;
    int mYear,mMonth,mDay;
    private static final String TEST_PUB_API_KEY = "pk_test_cHZ8p6lv1KldUz7RkWC50VEO";
    private static final String TEST_SEC_API_KEY = "sk_test_vb9Wu57BSwTRcxB7wqa0tDjC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize() {
        setSupportActionBar(mMainToolbar);
        addOnConnection();
    }

    private void addOnConnection() {
        if (checkConnection()) {
            listeners();
            tTestbutton.setEnabled(true);
        } else {
            removeOffConnection();
        }
    }

    private void removeOffConnection() {
        tTestbutton.setEnabled(false);
        Snackbar.make(mMainContainer, "No Internet", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOnConnection();
            }
        }).setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)).show();
    }

    private void listeners() {
        tTestbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyFields()){
                    mLoadingDialog  = new LoadingDialogFragment();
                    mLoadingDialog.setCancelable(false);
                    mLoadingDialog.show(getSupportFragmentManager(),"wait");
                    new makeAccount().execute();
                }
            }
        });
        final Calendar c = Calendar.getInstance();
        tDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        tDob.setText(dayOfMonth+"/"+month+"/"+year);
                        mYear = year;
                        mMonth = month;
                        mDay = dayOfMonth;
                    }
                },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_WEEK));
                datePickerDialog.show();
            }
        });
        tModChange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    tModChange.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorAccent));
                }else {
                    tModChange.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimaryDark));
                }
            }
        });
    }

    private boolean verifyFields(){
        if(tTestEmail.getText().toString().isEmpty()&&tTestEmail.getText().toString().length()<=0){
            tTestEmail.requestFocus();
            tTestEmail.setError("Enter the email id");
            return false;
        }else if(tTestFName.getText().toString().isEmpty()&&tTestFName.getText().toString().length()<=0){
            tTestFName.requestFocus();
            tTestFName.setError("Enter the First Name");
            return false;
        }else if(tTestLName.getText().toString().isEmpty()&&tTestLName.getText().toString().length()<=0){
            tTestLName.requestFocus();
            tTestLName.setError("Enter the Last Name");
            return false;
        }else if(tTestCountry.getText().toString().isEmpty()&&tTestCountry.getText().toString().length()<=0){
            tTestCountry.requestFocus();
            tTestCountry.setError("Enter the country");
            return false;
        }else if(tTestLCurrency.getText().toString().isEmpty()&&tTestLCurrency.getText().toString().length()<=0){
            tTestLCurrency.requestFocus();
            tTestLCurrency.setError("Enter the currency");
            return false;
        }else if(tTestRoutingNo.getText().toString().isEmpty()&&tTestRoutingNo.getText().toString().length()<=0){
            tTestRoutingNo.requestFocus();
            tTestRoutingNo.setError("Enter the routing no");
            return false;
        }else if(tTestAccountNo.getText().toString().isEmpty()&&tTestAccountNo.getText().toString().length()<=0){
            tTestAccountNo.requestFocus();
            tTestAccountNo.setError("Enter the account no");
            return false;
        }else if(tTestAddressLine.getText().toString().isEmpty()&&tTestAddressLine.getText().toString().length()<=0){
            tTestAddressLine.requestFocus();
            tTestAddressLine.setError("Enter the address");
            return false;
        }else if(tTestPostalCode.getText().toString().isEmpty()&&tTestPostalCode.getText().toString().length()<=0){
            tTestPostalCode.requestFocus();
            tTestPostalCode.setError("Enter the postal code");
            return false;
        }else if(tTestCity.getText().toString().isEmpty()&&tTestCity.getText().toString().length()<=0){
            tTestCity.requestFocus();
            tTestCity.setError("Enter the name of city");
            return false;
        }else if(tTestState.getText().toString().isEmpty()&&tTestState.getText().toString().length()<=0){
            tTestState.requestFocus();
            tTestState.setError("Enter the name of state");
            return false;
        }else if(tPid.getText().toString().isEmpty()&&tPid.getText().toString().length()<=0){
            tPid.requestFocus();
            tPid.setError("Enter the Personal identification no");
            return false;
        }else if(mYear==0||mMonth==0||mDay==0) {
            toastView("Enter DOB", Toast.LENGTH_LONG);
            return false;
        }if(!tModChange.isChecked()){
            return checkIFTest();
        }
        return true;
    }

    private boolean checkIFTest(){
        if(!tTestCountry.getText().toString().equalsIgnoreCase("US")){
            tTestCountry.requestFocus();
            tTestCountry.setError("Only us can be used while in test mode");
            return false;
        }else if(!tTestLCurrency.getText().toString().equalsIgnoreCase("usd")){
            tTestLCurrency.requestFocus();
            tTestLCurrency.setError("Only usd can be used while in test mode");
            return false;
        }else if(!tTestRoutingNo.getText().toString().equalsIgnoreCase("110000000")){
            tTestRoutingNo.requestFocus();
            tTestRoutingNo.setError("Enter 110000000 while in test mode");
            return false;
        }else if(!tTestAccountNo.getText().toString().equalsIgnoreCase("000123456789")){
            tTestAccountNo.requestFocus();
            tTestAccountNo.setError("Enter 000123456789 while in test mode");
            return false;
        }else if(!tTestAddressLine.getText().toString().equalsIgnoreCase("1234 Main Street")){
            tTestAddressLine.requestFocus();
            tTestAddressLine.setError("Enter 1234 Main Street while in test mode");
            return false;
        }else if(!tTestPostalCode.getText().toString().equalsIgnoreCase("94111")){
            tTestPostalCode.requestFocus();
            tTestPostalCode.setError("Enter 94111 Main Street while in test mode");
            return false;
        }else if(!tTestCity.getText().toString().equalsIgnoreCase("San Francisco")){
            tTestCity.requestFocus();
            tTestCity.setError("Enter San Francisco while in test mode");
            return false;
        }else if(!tTestState.getText().toString().equalsIgnoreCase("CA")){
            tTestState.requestFocus();
            tTestState.setError("Enter CA while in test mode");
            return false;
        }else if(!tPid.getText().toString().equalsIgnoreCase("123456789")){
            tPid.requestFocus();
            tPid.setError("Enter 123456789 while in test mode");
            return false;
        }
        return true;
    }

    private class makeAccount extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            return addManagedAccount();
        }

        @Override
        protected void onPostExecute(String s) {
            mLoadingDialog.dismiss();
            super.onPostExecute(s);
            if(s.contains("acct_")){
                toastView("Account Created",Toast.LENGTH_LONG);
            }else {
                toastView("Error",Toast.LENGTH_LONG);
                toastView(s,Toast.LENGTH_LONG);
            }
        }
    }


    private String addManagedAccount(){
        String id ;
        if(tModChange.isChecked()){
            Stripe.apiKey = "sk_live_QHz7MtmApAQ8tft662JqXvHh";
        }else {
            Stripe.apiKey = "sk_test_vb9Wu57BSwTRcxB7wqa0tDjC";
        }
        Map<String, Object> accountParams = new HashMap<>();
        accountParams.put("managed", true);
        accountParams.put("country",  tTestCountry.getText().toString());
        accountParams.put("email", tTestEmail.getText().toString());

        Map<String, Object> externalAccountParams = new HashMap<>();
        externalAccountParams.put("object", "bank_account");
        externalAccountParams.put("country", tTestCountry.getText().toString());
        externalAccountParams.put("currency", tTestLCurrency.getText().toString());
        externalAccountParams.put("routing_number", tTestRoutingNo.getText().toString());
        externalAccountParams.put("account_number",  tTestAccountNo.getText().toString());
        accountParams.put("external_account", externalAccountParams);

        Map<String, Object> tosParams = new HashMap<>();
        tosParams.put("date", 1494327004);
        tosParams.put("ip", "202.142.82.86");
        accountParams.put("tos_acceptance", tosParams);

        Map<String, Object> addressParams = new HashMap<String, Object>();
        addressParams.put("line1",  tTestAddressLine.getText().toString());
        addressParams.put("postal_code", Integer.parseInt( tTestPostalCode.getText().toString()));
        addressParams.put("city",  tTestCity.getText().toString());
        addressParams.put("state",  tTestState.getText().toString());

        Map<String, Object> legalEntityParam = new HashMap<>();

        Map<String, Object> DobParam = new HashMap<>();

        DobParam.put("day",mDay);
        DobParam.put("month",mMonth);
        DobParam.put("year",mYear);

        Map<String, Object> fileUploadParams = new HashMap<>();
        fileUploadParams.put("purpose", "identity_document");
        fileUploadParams.put("file", makeFile());
        FileUpload fileObj = null;
        try {
            fileObj = FileUpload.create(fileUploadParams);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            mLoadingDialog.dismiss();
            id = e.getMessage();
            e.printStackTrace();
        }

        Map<String, Object> verificationParams = new HashMap<>();
        verificationParams.put("document", fileObj.getId());


        legalEntityParam.put("dob",DobParam);
        legalEntityParam.put("first_name",tTestFName.getText().toString());
        legalEntityParam.put("last_name",tTestLName.getText().toString());
        legalEntityParam.put("type", "individual");
        legalEntityParam.put("address", addressParams);
        //legalEntityParam.put("ssn_last_4", 1234);
        //legalEntityParam.put("personal_id_number", 123456789);
        legalEntityParam.put("verification", verificationParams);
        legalEntityParam.put("personal_id_number", Integer.parseInt( tPid.getText().toString()));

        accountParams.put("legal_entity", legalEntityParam);

        try {
            Account c = Account.create(accountParams);
            id = c.getId();
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            mLoadingDialog.dismiss();
            id = e.getMessage();
            e.printStackTrace();
        }
        return id;
    }

    private File makeFile(){
        Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.success);
        File f = new File(getExternalCacheDir(),"success.jpg");
        if(!f.exists()) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return f;
    }

    private void messageDialog(String message) {
        if (message != null) {
            AlertDialog.Builder messageDialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message);
            messageDialog.create().show();
        } else {
            toastView("Null", Toast.LENGTH_SHORT);
        }
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void toastView(String message, final int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }
}
