package com.payUMoney.sdk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.payUMoney.sdk.database.Cards;
import com.payUMoney.sdk.database.Users;
import com.payUMoney.sdk.fragment.PaymentOptionsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by piyush on 4/11/14.
 */
public class HomeActivity extends FragmentActivity
{
    //First Screen after login
    String mid;
    public  static Double amountt,amt_convenience,amt_total,amt_discount,amt_net,amount,available_points,wallet;
    String amt;
    public static final int RESULT_QUIT = 5;
    JSONObject paymentDetails,points;
    public static TextView savings;
    static final int  LOGIN = 1;
    public static final int WEB_VIEW = 2;
    public static final int SIGN_UP = 7;
    public static final int RESULT_BACK = 8;
    private boolean show =false;
    public static TextView mAmount;
    public static TextView /*mPayUpoints,*/ paymentmethod;
    public static TextView mAmoutDetails;
    Boolean signInflag,signUpflag;
    static final String STATE_MID = "merchantid";
    static final String STATE_AMT = "amount";
    private boolean activity_flag = true,mpointsFlag=true;
    private AccountManager mAccountManager;
    Session session;
    PaymentOptionsFragment fragment;
    int count=0;
   /* boolean isChooserShown=false;*/

    private Account mAccount ;
    Button login,register;

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        check_login(); //Called every time activity starts
    }
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        mpointsFlag=true;
    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    public void check_login()        // Function to check login and if yes then initiate the payment
    {

        session = Session.getInstance(getApplicationContext()); //get attached object of Session
        if (!session.isLoggedIn())  //Not logged in
        {
            setContentView(R.layout.chooser);
            show=false;
            invalidateOptionsMenu();
            login=(Button)findViewById(R.id.login);
            register=(Button)findViewById(R.id.signup);

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!session.isLoggedIn())
                    {
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.putExtra(Constants.AMOUNT, getIntent().getStringExtra(Constants.AMOUNT));
                        intent.putExtra(Constants.MERCHANTID, getIntent().getStringExtra(Constants.MERCHANTID));
                        intent.putExtra(Constants.PARAMS, getIntent().getSerializableExtra(Constants.PARAMS));
                        intent.putExtra(Constants.USER_EMAIL, getIntent().getStringExtra(Constants.USER_EMAIL));
                        intent.putExtra(Constants.USER_PHONE, getIntent().getStringExtra(Constants.USER_PHONE));
                        startActivityForResult(intent, LOGIN);
                    }

                }
            });

            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!session.isLoggedIn())
                    {
                        Intent intent = new Intent(HomeActivity.this, SignUpActivity.class);
                        intent.putExtra(Constants.AMOUNT, getIntent().getStringExtra(Constants.AMOUNT));
                        intent.putExtra(Constants.MERCHANTID, getIntent().getStringExtra(Constants.MERCHANTID));
                        intent.putExtra(Constants.PARAMS, getIntent().getSerializableExtra(Constants.PARAMS));
                        intent.putExtra(Constants.USER_EMAIL, getIntent().getStringExtra(Constants.USER_EMAIL));
                        intent.putExtra(Constants.USER_PHONE, getIntent().getStringExtra(Constants.USER_PHONE));
                        startActivityForResult(intent, SIGN_UP);
                    }
                }
            });
        }

        else  //logged in already
        {
            show=true;
            invalidateOptionsMenu();
            startpayment((HashMap<String ,String >)getIntent().getSerializableExtra(Constants.PARAMS)); //params passed from login
        }

    }

    public void startpayment(HashMap<String ,String > params)  //Intiate payment
    {
        setContentView(R.layout.activity_home);

        mid = params.get("MerchantId");
        amt = params.get("Amount");

        mAmount = ((TextView) findViewById(R.id.amountTextView));
        mAmount.setText("Rs." + amt);
      /*  mPayUpoints = (TextView)findViewById(R.id.payupoints);*/
        savings=(TextView)findViewById(R.id.savings);
        mAmoutDetails = (TextView) findViewById(R.id.amountDetails);
        mAmoutDetails.setVisibility(View.GONE);
        paymentmethod=(TextView)findViewById(R.id.paymentmethod);


        if(mpointsFlag)
        {
            Session.getInstance(getApplicationContext()).getUserPoints(); //Reload the points, wallet, cashback
        }


        //available_points=getPoints();

      fragment = new PaymentOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("params",params);
        fragment.setArguments(bundle);
        //Set PayMent Fragment
        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment, "paymentOptions").commitAllowingStateLoss();
    }



    public String getMid()
    {
        return mid;
    }
    public String getAmt()
    {
        return amt;
    }
    public JSONObject getBankObject()
    {
        return paymentDetails;
    }

    public Double getPoints()
    {
        try {
            if(!points.toString().equals("{}"))
                return points.getJSONObject("cashback").getDouble("availableAmount");
            else
                return 0.0; //No points
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) //When HomeActivity resumes/starts
    {
        if (requestCode == LOGIN)
        {
            if(resultCode == RESULT_OK)
            {
                check_login();
            }
            else if(resultCode == LoginActivity.RESULT_QUIT)
            {
                /*Log.i("Login","cancelled");
                Intent intent = new Intent();
                intent.putExtra(Constants.RESULT,"cancel");
                setResult(RESULT_CANCELED, intent);
                finish();*/
                check_login();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                check_login();
                //Write your code if there's no result
                //  Toast.makeText(getApplicationContext(),"what",Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == WEB_VIEW) //Coming back from making a payment
        {
            if(resultCode == RESULT_OK) //Success
            {
                // check_login();
                Log.i("payment_status","success");
                setResult(RESULT_OK,data);
                finish();
                //startpayment(data.getStringExtra(Constants.MERCHANTID),data.getStringExtra(Constants.AMOUNT));
            }
            else if (resultCode == RESULT_CANCELED) //Fail
            {
                // check_login();
                Log.i("payment_status","failure");
                setResult(RESULT_CANCELED,data);
                finish();
                //Write your code if there's no result
            }
            else if (resultCode == RESULT_BACK)
            {
                //Write your code if there's no result
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Something went wrong. please retry",Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == SIGN_UP)
        {
            if(resultCode == RESULT_OK)
            {
                Log.i("login_status", "success");
                check_login();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Log.i("payment_status","failure");
                activity_flag = false;
                check_login();
            }
        }
    }//onActivityResult

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.logout)
        {
           logout();
        }
        else if (item.getItemId() == R.id.add_account )
        {
           mAccountManager = AccountManager.get(getApplicationContext());
           addnewaccount();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Function to add new account
     * @param menu
     * @return
     */

    public void logout()
    {
        Session.getInstance(getApplicationContext()).logout("");
        SharedPreferences.Editor edit = getSharedPreferences(Constants.SP_SP_NAME, Activity.MODE_PRIVATE).edit();
        edit.clear();
        edit.commit();
        Cards.getInstance(getApplicationContext()).deleteAll();
        Users.getInstance(getApplicationContext()).deleteAll();

        mpointsFlag=true;  //So Points can be refetched for the new user who will Log in now

        PaymentOptionsFragment.temp_wallet=0.0;
        check_login();
       /* Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.putExtra(Constants.AMOUNT, getIntent().getStringExtra(Constants.AMOUNT));
        intent.putExtra(Constants.PARAMS, getIntent().getSerializableExtra(Constants.PARAMS));
        intent.putExtra(Constants.USER_EMAIL, getIntent().getStringExtra(Constants.USER_EMAIL));
        intent.putExtra(Constants.USER_PHONE, getIntent().getStringExtra(Constants.USER_PHONE));
        intent.putExtra("logout", "logout");
        startActivityForResult(intent, LOGIN);*/
    }
    public void addnewaccount()
    {
        // TODO: add account
        try {
            mAccountManager.addAccount(
                    "com.payUMoney.sdk.auth.account",
                    "bearer",
                    null,
                    new Bundle(),
                    this,
                    new OnAccountAddComplete(),
                    null);
        }catch(Exception e )
        {
            Toast.makeText(getApplicationContext(),"Error adding account",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Call back handler for when account is added
     * @param menu
     * @return
     */
    private class OnAccountAddComplete implements AccountManagerCallback<Bundle>
    {
        @Override
        public void run(AccountManagerFuture<Bundle> result)
        {
            Bundle bundle;
            try {
                bundle = result.getResult();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error adding account",Toast.LENGTH_LONG).show();
                return;
            } catch (AuthenticatorException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error adding account",Toast.LENGTH_LONG).show();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            mAccount = new Account(
                    bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
                    bundle.getString(AccountManager.KEY_ACCOUNT_TYPE)
            );
            Log.d("main", "Added account " + mAccount.name + ", fetching");
            Toast.makeText(getApplicationContext(),mAccount.name.toString()+ " Added",Toast.LENGTH_LONG).show();
            logout();

            //Start fetch of the new account
           // startAuthTokenFetch();
        }
    }
    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(show)
        menu.add(Menu.NONE, R.id.logout, menu.size(), R.string.logout).setIcon(R.drawable.logout).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, R.id.add_account, menu.size(), "Add Account").setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }
    public void onEventMainThread(CobbocEvent event)
    {
        if (event.getType() == CobbocEvent.LOGOUT)
        {
            if (event.getValue() != null)
            {
                if (event.getValue().equals("force"))
                {
                    Toast.makeText(this, R.string.inactivity, Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor edit = getSharedPreferences(Constants.SP_SP_NAME, Activity.MODE_PRIVATE).edit();
                    edit.clear();
                    edit.commit();
                    Cards.getInstance(getApplicationContext()).deleteAll();
                    Users.getInstance(getApplicationContext()).deleteAll();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.putExtra(Constants.AMOUNT, getIntent().getStringExtra(Constants.AMOUNT));
                    intent.putExtra(Constants.PARAMS, getIntent().getSerializableExtra(Constants.PARAMS));
                    intent.putExtra(Constants.USER_EMAIL, getIntent().getStringExtra(Constants.USER_EMAIL));
                    intent.putExtra(Constants.USER_PHONE, getIntent().getStringExtra(Constants.USER_PHONE));
                    intent.putExtra("force","force");
                    startActivityForResult(intent, LOGIN);
                }
            }
            // clear the token stored in SharedPreferences
        }else if (event.getType() == CobbocEvent.USER_POINTS)
        {
            if (event.getStatus())
            {
                mpointsFlag=false;

                JSONObject cashback = (JSONObject)event.getValue();

                points = cashback; //Just to check if something is returned
                try{

                    if(points.toString().equals("{}"))
                    {
                        wallet=points.getJSONObject("wallet").getDouble("availableAmount"); //Extracted the amount in wallet
                        Log.d("availableAmount",wallet.toString());

                        if(wallet>0.0)
                        {

                        }

                        //    Toast.makeText(getApplicationContext(),wallet.toString(),Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        /*no wallet*/
                        Log.d("NPE","NPE on jsonobject");
                    }

                }
                catch(Exception e)
                {
                    Log.d("Exception",e.toString());
                }
            }
            else if(event.getType() == CobbocEvent.LOGIN)
            {
            }
        }

    }

    public void onPaymentOptionSelected(String mode,JSONObject details)
    {

        paymentDetails = details;

    }

    @Override
    public void onBackPressed()
    {
        FragmentManager fragmentManager = HomeActivity.this.getFragmentManager();
        android.app.Fragment tempFragment=fragmentManager.findFragmentByTag("paymentOptions");
        FragmentManager fragmentManager1 = HomeActivity.this.getFragmentManager();
        android.app.Fragment PayUPointsFragment=fragmentManager.findFragmentByTag("payumoneypoints");
        count++;

        /*paymentoptionsfrag || payuMoneyFrag is showing.*/
        if ((tempFragment!=null && tempFragment.isVisible())||(PayUPointsFragment!=null && PayUPointsFragment.isVisible()))
        {
            /*confirm close twice*/
            if(count%2==0) {
                count=0;
                close();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Press Back again to cancel transaction",Toast.LENGTH_LONG).show();
            }
        }
        /*!show of payumoneyFrags*/
        else if(!show)
        {
            if(count%2==0) {
                count=0;
                iQuit();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Press back again to go cancel",Toast.LENGTH_LONG).show();
            }
        }
         else
        {
            FragmentManager fm = HomeActivity.this.getFragmentManager();
            fm.popBackStack();
        }
    }

    public void iQuit()
    {
        setResult(RESULT_QUIT);
        finish();
    }

    public void close()
    {
        Intent intent = new Intent();
        intent.putExtra(Constants.RESULT,"cancel");
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onDestroy()
    {
            super.onDestroy();
    }


}
