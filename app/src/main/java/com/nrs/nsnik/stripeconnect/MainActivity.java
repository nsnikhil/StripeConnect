package com.nrs.nsnik.stripeconnect;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
    @BindView(R.id.testPayButton) Button tPayButton;
    @BindView(R.id.testCardWidget) CardInputWidget tCardWidget;
    @BindView(R.id.testSpinner) Spinner tAccountList;
    LoadingDialogFragment mLoadingDialog;
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

    private class createList extends AsyncTask<Void,Void,List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            Stripe.apiKey = "sk_test_vb9Wu57BSwTRcxB7wqa0tDjC";
            Map<String, Object> accountParams = new HashMap<>();
            List<String> idList = new ArrayList<>();
            try {
                AccountCollection accountCollection = Account.list(accountParams);
                List<Account> accountList = accountCollection.getData();
                for(Integer i = 0;i<accountList.size();i++){
                    idList.add(accountList.get(i).getId());
                }
            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                e.printStackTrace();
            }
            return idList;
        }

        @Override
        protected void onPostExecute(List<String> idList) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,idList);
            tAccountList.setAdapter(arrayAdapter);
        }
    }

    private void addOnConnection() {
        if (checkConnection()) {
            listeners();
            new  createList().execute();
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
        tPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay();
            }
        });
    }

    private void pay() {
        Card mCard = tCardWidget.getCard();
        if (mCard == null) {
            messageDialog("Invalid Card Data");
        } else {
            try {
                mLoadingDialog = new LoadingDialogFragment();
                mLoadingDialog.show(getSupportFragmentManager(),"wait");
                com.stripe.android.Stripe stripe = new com.stripe.android.Stripe(getApplicationContext(), TEST_PUB_API_KEY);
                stripe.createToken(mCard,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                new chargeAccount().execute(token.getId(),tAccountList.getSelectedItem().toString());
                            }

                            public void onError(Exception error) {
                                toastView(error.getLocalizedMessage(), Toast.LENGTH_LONG);
                                mLoadingDialog.dismiss();
                            }
                        }
                );
            }catch (IllegalArgumentException e){
                mLoadingDialog.dismiss();
                e.printStackTrace();
            }

        }

    }

    private void createCharge(String tokenId,String accId){
        Stripe.apiKey = "sk_test_vb9Wu57BSwTRcxB7wqa0tDjC";

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", 2000);
        chargeParams.put("currency", "usd");
        chargeParams.put("application_fee",20);
        chargeParams.put("description", "testcharge");
        chargeParams.put("source",tokenId );
        chargeParams.put("on_behalf_of",accId);

        Map<String, Object> destinationParams = new HashMap<>();
        destinationParams.put("account", accId);
        chargeParams.put("destination", destinationParams);

        try {
            Charge.create(chargeParams);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            e.printStackTrace();
        }
    }

    private boolean validEmail(){
        if(!tTestEmail.getText().toString().isEmpty()&&tTestEmail.getText().toString().length()>0){
            return true;
        }
        tTestEmail.setError("Enter a email id");
        return false;
    }

    private class chargeAccount extends AsyncTask<String,Void,Void>{

        @Override
        protected Void doInBackground(String... params) {
            createCharge(params[0],params[1]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mLoadingDialog.dismiss();
        }
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
                new  createList().execute();
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
