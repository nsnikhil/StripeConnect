package com.nrs.nsnik.stripeconnect;

import android.app.DatePickerDialog;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nrs.nsnik.stripeconnect.fragemnts.dialogFragments.LoadingDialogFragment;
import com.stripe.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Account;
import com.stripe.model.AccountCollection;
import com.stripe.model.Charge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
        } else {
            removeOffConnection();
        }
    }

    private void removeOffConnection() {
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
                if(validEmail()){
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
    }

    private boolean validEmail(){
        if(tTestEmail.getText().toString().isEmpty()&&tTestEmail.getText().toString().length()<=0){
            tTestEmail.setFocusable(true);
            tTestEmail.setError("Enter a email id");
            return false;
        }else if(tTestFName.getText().toString().isEmpty()&&tTestFName.getText().toString().length()<=0){
            tTestFName.setFocusable(true);
            tTestFName.setError("Enter First Name");
            return false;
        }else if(tTestLName.getText().toString().isEmpty()&&tTestLName.getText().toString().length()<=0){
            tTestLName.setFocusable(true);
            tTestLName.setError("Enter Last Name");
            return false;
        }else if(mYear==0||mMonth==0||mDay==0){
            toastView("Enter DOB",Toast.LENGTH_LONG);
            return false;
        }
        return true;
    }

    private class makeAccount extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            return stripeFunction();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.contains("acct_")){
                toastView(s,Toast.LENGTH_LONG);
            }else {
                toastView("Error",Toast.LENGTH_LONG);
                toastView(s,Toast.LENGTH_LONG);
            }
        }
    }

    private String stripeFunction() {
        String id = null;
        Stripe.apiKey = "sk_test_vb9Wu57BSwTRcxB7wqa0tDjC";

        Map<String, Object> accountParams = new HashMap<>();
        accountParams.put("managed", false);
        accountParams.put("country", "US");
        accountParams.put("email", tTestEmail.getText().toString());
        //accountParams.put("payouts_enabled",true);

        Map<String, Object> legalEntityParam = new HashMap<>();

        legalEntityParam.put("first_name",tTestFName.getText().toString());
        legalEntityParam.put("last_name",tTestLName.getText().toString());

        Map<String, Object> DobParam = new HashMap<>();

        DobParam.put("day",mDay);
        DobParam.put("month",mMonth);
        DobParam.put("year",mYear);

        legalEntityParam.put("dob",DobParam);


        accountParams.put("legal_entity", legalEntityParam);

        try {
            Account c = Account.create(accountParams);
            id = c.getId();
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            id = e.getMessage();
            e.printStackTrace();
        }
        return id;
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
