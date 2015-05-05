package com.payUMoney.sdk.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payUMoney.sdk.CobbocEvent;
import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.HomeActivity;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.Session;
import com.payUMoney.sdk.WebViewActivityPoints;
import com.payUMoney.sdk.adapter.CouponListAdapter;
import com.payUMoney.sdk.adapter.PaymentModeAdapter;
import com.payUMoney.sdk.dialog.QustomDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import de.greenrobot.event.EventBus;


public class PaymentOptionsFragment extends Fragment {
    private LinearLayout lv1;
    private CheckBox walletcheck;
    Timer timer;
    private TextView wallettext,walletbalance,minconvfee;
    private Button wallet_payment,goback;
    String amt, surl, furl,mid;
    JSONObject temp;
    ProgressDialog mProgressDialog;
    HashMap<String ,String > map;
    ArrayAdapter<String> arrayAdapter;

    private RelativeLayout rvlv;

    ListView mPaymentOptionList;
    CouponListAdapter coupanAdapter;
    JSONObject x;
    private boolean walletflag=true;
    public static String choosedCoupan,choosedCoupanUser;
    public static int choosedItem;
    PaymentModeAdapter adapter;
    ListView listView;
    String coupon_string="coupon applied";
    List<String> availableModes;
    final HashMap<String, Object> data = new HashMap<String, Object>();
    ListView lv;
    Boolean fragment_check = true;
    JSONObject details;
    public static Double wallet_usage=0.0,temp_wallet=0.0,wallet=0.0,coupan_amt=0.0,amt_convenience, amtafterCoupanDiscount,temp_amt_discount, amt_total, amt_discount=0.0, amt_net, amount, cashback_amt, cashback_amt_total, amtafterDicount,cashbackDisplayamt;

    public PaymentOptionsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        coupan_amt = 0.0;
        wallet=0.0;
        temp_wallet=0.0;
        choosedItem = -1;
        cashback_amt_total=0.0;
        cashback_amt=0.0;
        amt_convenience=0.0;
        amtafterCoupanDiscount=0.0;
        amt_total=0.0;
        amt_net=0.0;
        amount=0.0;
        amtafterDicount=0.0;
        map = ((HashMap<String,String>)getArguments().getSerializable("params"));
        mid = map.get("MerchantId");
        amt = map.get("Amount");
        surl = getArguments().getString("surl");
        furl = getArguments().getString("furl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.payment_options, container, false);
        wallet_payment=(Button)view.findViewById(R.id.walletpayment);
        goback=(Button)view.findViewById(R.id.useNewCardButton);
        walletcheck=(CheckBox)view.findViewById(R.id.walletcheck);
        walletcheck.setVisibility(view.GONE);
        wallettext=(TextView)view.findViewById(R.id.wallettext);
        walletbalance=(TextView)view.findViewById(R.id.walletbalance);
        rvlv = (RelativeLayout)view.findViewById(R.id.listviewrl);
       // selectPaymentMethodTextView=(TextView)view.findViewById(R.id.selectPaymentMethodTextView);

        minconvfee=(TextView)view.findViewById(R.id.minconfee);
        lv1=(LinearLayout)view.findViewById(R.id.lv1);
        listView = (ListView) view.findViewById(R.id.paymentOptionsListView);



        //mPaymentOptionList = (ListView) findViemwById(R.id.paymentOptionsListView);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(walletcheck.isChecked())
                walletcheck.setChecked(false);
               unchecked();

            }
        });

        wallet_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walletDialog();
            }
        });

        walletcheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                if(b)   //TICKED
                {
                   if(getView().findViewById(R.id.couponSection).isShown())
                   {
                       getView().findViewById(R.id.couponSection).setVisibility(View.GONE);
                   }
                    try
                    {
                        //Wallet conv charge
                        amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");
                    }catch (Exception e){}

                    if(amount-amt_discount-coupan_amt-cashback_amt_total+amt_convenience<=temp_wallet) //Wallet is fatter so pay from it.
                    {
                        walletflag=true;
                        wallet_usage=(temp_wallet-amt_convenience-(amount-amt_discount-coupan_amt-cashback_amt_total));
                        walletbalance.setText("Your wallet balance after this transaction: "+round(wallet_usage,2));
                        if(!wallet_payment.isShown())
                        {
                          //  selectPaymentMethodTextView.setText("Pay Using Wallet : ");
                            HomeActivity.paymentmethod.setText("Pay Using Wallet");
                            rvlv.setVisibility(View.GONE);
                            wallet_payment.setVisibility(View.VISIBLE);
                            goback.setVisibility(View.VISIBLE);

                            /*if(cashback_amt_total>0.0)
                            HomeActivity.mPayUpoints.setText("Convenience Charged : Rs."+round((amt_convenience),2)+" And Rs. " + cashback_amt_total + " will be deducted from bill (PayUPoints)");
                            else
                            HomeActivity.mPayUpoints.setText("Convenience Charged : Rs."+round(amt_convenience,2));
                            HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/

                            wallet=amount-amt_discount-coupan_amt-cashback_amt_total+amt_convenience; // Wallet will cover entire charges
                            HomeActivity.mAmount.setText("Rs. "+round(((wallet)),2));

                        }
                    }
                    else //Wallet is smaller, remove wallet amount from net discounted amount
                    {
                        walletflag=false;
                        wallet_usage=temp_wallet;
                        walletbalance.setText("Your wallet balance after this transaction: "+0.0);
                        wallet=temp_wallet;
                        amt_net=amt_net-wallet;
                        if(amt_net<0 ) //When convinience fee of other cards is in play
                        {
                            HomeActivity.mAmount.setText("Rs." + 0.0);
                        }
                        else
                        HomeActivity.mAmount.setText("Rs." + round((amt_net),2));
                        amt_convenience=0.0; //Set convinience to zero if wallet is smaller
                    }

                    try{
                        wallettext.setText("You have Rs. " + String.valueOf(temp.getJSONObject("wallet").getDouble("availableAmount")) + " in your wallet");
                    }catch (Exception e){}

                    walletbalance.setVisibility(View.VISIBLE);
                    wallettext.setVisibility(View.VISIBLE);
                }
                else if(!b) //NOT TICKED
                {
                 unchecked();
                }
            }
        });
        return view;

    }

    public void unchecked()
    {
        try {
            if (details.getJSONArray("userCouponsAvailable").length() != 0)
            {
                if(coupan_amt>0.0)
                {
                    ((TextView) getView().findViewById(R.id.selectCoupon1)).setText(coupon_string);
                    ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.remove);
                    getView().findViewById(R.id.couponSection).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.selectCoupon).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.selectCoupon1).setVisibility(View.VISIBLE);
                }
                else
                for (int i = 0; i < details.getJSONArray("userCouponsAvailable").length(); i++)
                {
                    if (details.getJSONArray("userCouponsAvailable").getJSONObject(i).getBoolean("enabled"))
                    {
                        getView().findViewById(R.id.couponSection).setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.selectCoupon).setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.selectCoupon1).setVisibility(View.VISIBLE);
                        ((TextView) getView().findViewById(R.id.selectCoupon1)).setText(R.string.select_coupon_option);
                        ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.view_coupon);
                        break;
                    }
                }
                amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");
            }
        }catch(Exception e){}
        walletbalance.setVisibility(View.GONE);
        wallettext.setVisibility(View.GONE);
        if(walletflag) //Wallet is fatter
        {
            if(wallet_payment.isShown())
            {
                // availableModes.remove("wallet");
                wallet=0.0;
                if(coupan_amt==0.0)
                    amt_net=amount-cashback_amt_total-amt_discount;
                else if(coupan_amt>0.0)amt_net=amount-cashback_amt_total-coupan_amt;

                HomeActivity.mAmount.setText("Rs. "+round((amt_net),2));

                if(amt_net<0.0)
                    HomeActivity.mAmount.setText("Rs. "+0.0);
                // selectPaymentMethodTextView.setText("Select Payment Method : ");
                HomeActivity.paymentmethod.setText("Select Payment Method");
                wallet_payment.setVisibility(View.GONE);
                goback.setVisibility(View.GONE);
                /*HomeActivity.mPayUpoints.setText("Rs. " + cashback_amt_total + " will be deducted from bill (PayUPoints)");
                HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/
                rvlv.setVisibility(View.VISIBLE);
                amt_convenience=0.0;
            }
        }
        else if(!walletflag)//Wallet is smaller, remove wallet amount
        {
            // amtafterCoupanDiscount=amtafterCoupanDiscount+wallet; //Remove wallet discount

            amt_net=amt_net+wallet;
            wallet=0.0;
            // amt_convenience=0.0;
            HomeActivity.mAmount.setText("Rs." + round((amt_net),2));
            if(amt_net<0.0)
                HomeActivity.mAmount.setText("Rs." + round((amt_net),2));

            amt_convenience=0.0;
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        if (fragment_check) //If new Instance
        {
         /*   mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("please wait...");
            mProgressDialog.setIndeterminate(true);
          //  mProgressDialog.setCancelable(false);
            mProgressDialog.show();*/
            mProgressDialog=showProgress(getActivity());
            Session.getInstance(getActivity()).createPayment(map); //Create  event fired when Parent Activity is created
            Session.getInstance(getActivity()).getUserPoints(); // vault
            fragment_check=false;
        }
        else
        {  //Not new Instance of Fragment
            try
            {
               // Session.getInstance(getActivity()).getUserPoints();
                showPaymentModeChooseDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onResume()
    {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
            amt_convenience=0.0;
        /**PAYU POINTS INFO****/
        if(cashback_amt_total!=null)
        {
            if(cashback_amt_total>0.0)
            {
               /* HomeActivity.mPayUpoints.setText("Rs. " + cashback_amt_total + " has been deducted from bill (PayUPoints)");
                HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/
            }
            else
            {
                /*if(HomeActivity.mPayUpoints.isShown())
                {
                    HomeActivity.mPayUpoints.setVisibility(View.GONE);
                }*/
                cashback_amt_total=0.0;
            }
        }

        //if(temp_wallet!=null)temp_wallet=0.0;
        /**Coupan INFO****/
        if(getView()!=null&&x!=null&&coupan_amt>0.0)
        {
            try {
                ((TextView) getView().findViewById(R.id.selectCoupon1)).setText("Congrats, " + x.getString("couponStringForUser") + " Applied  ");
                ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.remove);
            }
            catch(Exception e)
            {
                Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_LONG).show();
            }
        }
        /**Net Amount INFO****/
     /*   if(HomeActivity.mAmount!=null&&amtafterCoupanDiscount!=null)
        {

            HomeActivity.mAmount.setText("Rs." + round((amount-amt_discount-coupan_amt-cashback_amt_total+amt_convenience),2));
        }*/
        /**Wallet INFO****/

        walletcheck.setVisibility(View.GONE); //Default action
        if(amt_net!=null)
        {
            if (temp_wallet > 0.0)
            {
                //wallettext.setVisibility(View.VISIBLE);
                walletcheck.setVisibility(View.VISIBLE);
                lv1.setVisibility(View.VISIBLE);

                try {
                    wallettext.setText("You have Rs. " + String.valueOf(temp.getJSONObject("wallet").getDouble("availableAmount")) + " in your wallet");
                }catch (Exception e){}

                if(amt_net<0.0&&!walletcheck.isChecked())
                    HomeActivity.mAmount.setText("Rs." + 0.0);
         //       if(amt_net>wallet) //if not suff wallet
         //           HomeActivity.mAmount.setText("Rs." + round((float)((amt_net-wallet)/100)*100,2));
          //      else   //if wallet was fatter
           //         HomeActivity.mAmount.setText("Rs." + round((float)((0.0)/100)*100,2));
            }
        }
        /**Savings INFO****/
        if(amt_discount!=null&&coupan_amt!=null)
        {
            if(coupan_amt==0.0)
                HomeActivity.savings.setText("Total Savings : INR " + round((float)(amt_discount/100)*100,2));
            else
                HomeActivity.savings.setText("Total Savings : INR " + round((float)(coupan_amt/100)*100,2));
                HomeActivity.savings.setVisibility(View.VISIBLE);

        }

    }



    @Override
    public void onPause() {
        super.onPause();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);

            walletcheck.setVisibility(View.GONE);
          // temp_wallet=0.0;

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void onEventMainThread(final CobbocEvent event) //Bus Function
    {
        if(event!=null){
            if (event.getType() == CobbocEvent.USER_POINTS) //Add wallet points
            {
                if (event.getStatus()) {
                    try {
                           temp = (JSONObject) event.getValue();
                        temp_wallet = temp.getJSONObject("wallet").getDouble("availableAmount");
                        wallet_usage=temp_wallet;
                       // Toast.makeText(getActivity(), "New value is " + temp.toString(), Toast.LENGTH_LONG).show();
                        if (temp_wallet > 0.0) {
                            //   wallettext.setVisibility(View.VISIBLE);
                            walletcheck.setVisibility(View.VISIBLE);
                            lv1.setVisibility(View.VISIBLE);
                            wallettext.setText("You have Rs. " + temp_wallet.toString() + " in your wallet");

                            //  HomeActivity.savings.setText("Total Savings: "+);
                        }
                    } catch (Exception e) {
                        Log.d("Exception while getting wallet detials", e.toString());
                    }
                } else {
                    if(mProgressDialog!=null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Toast.makeText(getActivity(), "Some error occurred! Try again", Toast.LENGTH_LONG).show();
                }
            } else if (event.getType() == CobbocEvent.CREATE_PAYMENT)  //New Payment
            {
                if (event.getStatus()) {
                    Session.getInstance(getActivity()).getPaymentDetails((String) event.getValue()); //Fire getpaymentdetails of session

                } else {
                    if(mProgressDialog!=null && mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Toast.makeText(getActivity(), "Please retry after some time", Toast.LENGTH_LONG).show();
                }
            } else if (event.getType() == CobbocEvent.PAYMENT_DETAILS)  // Payment details (CC DC PayuP inquired) at session
            {
                if (event.getStatus()) {
                    // so we got payment details.
                    // now we'll ask the user about his choice of payment mode.
                    details = (JSONObject) event.getValue();   //Get the Json Object attached with the post of event Payment_details
                    try {
                        showPaymentModeChooseDialog(); //Call Payment Mode
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            } else if (event.getType() == CobbocEvent.PAYMENT_POINTS) {
                if (event.getStatus()) {

                    Intent intent = new Intent(getActivity(), WebViewActivityPoints.class);
                    intent.putExtra(Constants.RESULT, event.getValue().toString());
                    getActivity().startActivityForResult(intent, ((HomeActivity) getActivity()).WEB_VIEW);


                } else {

                }
            }
        }
    }

    private void showPaymentModeChooseDialog() throws JSONException //Called when user comes to paymentoptionsfragment or getpaymentdetail api is called
    {
        JSONObject priority = details.getJSONObject(Constants.PAYMENT_OPTION); //Get Json object paymentOptions
        availableModes = new ArrayList<String>();
        cashback_amt_total = ((HomeActivity) getActivity()).getPoints(); //Get points user has
        amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount"); //Get amount user need to pay

      //  if(amt_discount!=0)//then check for 0.0
        if(coupan_amt==0.0&&!details.getString("cashbackAccumulated").toString().equals("null"))//If No coupon
        amt_discount = details.getJSONObject("cashbackAccumulated").getDouble(Constants.AMOUNT); //Get discount offered
        else //Coupons
        amt_discount=0.0;

        amtafterDicount=amount-amt_discount; //amount after preset discount

        if(coupan_amt>0.0)
        {
            amtafterCoupanDiscount=amount-coupan_amt; //no preset discount
        }
        else
        {
            amtafterCoupanDiscount = amtafterDicount; //Coupons are zero in start until applied
        }



/**Display essential info**/
        if(cashback_amt_total!=null&&amtafterCoupanDiscount!=null)
        {
            if(cashback_amt_total>0.0&&cashback_amt_total<amtafterCoupanDiscount)
            {
              /*  HomeActivity.mPayUpoints.setText("Rs. " + cashback_amt_total + " will be deducted from bill (PayUPoints)");
                HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/
            }

        }
        if(amt_discount!=null&&coupan_amt!=null)
        {
            HomeActivity.savings.setText("Total Savings : INR "+(round((float)(amt_discount/100)*100,2)));
            HomeActivity.savings.setVisibility(View.VISIBLE);
        }

        /**Add in list**/
        availableModes.add("STORED_CARDS"); //Add available mode by default

        if (priority.has("db")) //Add debitcard to list
        {
            availableModes.add("DC");
        }
        if (priority.has("cc")) {  //Add creditcard to list
            availableModes.add("CC");
        }
        if (priority.has("nb")) {     //Add NetBanking to list
            availableModes.add("NB");
        }

        amt_convenience= new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");
        setMincon();

        //Check for convinience amount on CC and check again if PayUpoints are enough
        //HANDLE PAYUPOINTS>Amount
        if (round((amtafterCoupanDiscount +amt_convenience),2) <= round(cashback_amt_total,2))  //If Total Amount <= PayUMoney Points
        {
            amt_net=amtafterCoupanDiscount +amt_convenience;
            try
            {
               // Toast.makeText(getActivity(),amt_net+"",Toast.LENGTH_LONG).show();
                HomeActivity.mAmount.setText("Rs." + round(amt_net,2));
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            HomeActivity.mAmoutDetails.setVisibility(View.VISIBLE);
            PayUMoneyPointsFragment fragment = new PayUMoneyPointsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("details", details.toString());
            bundle.putDouble("cashback_amt_total",cashback_amt_total);
            bundle.putDouble("coupon",0.0);
            fragment.setArguments(bundle);
            FragmentTransaction transaction;
            transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
            transaction.replace(R.id.fragmentContainer, fragment, "payumoneypoints");
            transaction.addToBackStack("a");
            transaction.commit();
            getActivity().getFragmentManager().executePendingTransactions();
        }
        else
        {
            amt_net = amtafterCoupanDiscount - cashback_amt_total;
            if(amt_net<0.0)
            {
                amt_net=0.0;

            }
            amt_convenience=0.0; //reset value if above "if" is false
            try
            {
                HomeActivity.mAmount.setText("Rs." + round((amt_net),2));
                HomeActivity.mAmoutDetails.setVisibility(View.VISIBLE);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            //amtafterCoupanDiscount=amtafterCoupanDiscount-cashback_amt_total; //cashback is less or zero
            // cashbackDisplayamt = cashback_amt_total;
        }

        HomeActivity.mAmoutDetails.setOnClickListener(new View.OnClickListener()

        {
            //@Override if (amtafterCoupanDiscount - cashback_amt_total <= 0) {
            public void onClick(View v) {
                Double temp_net;
                if(amt_net<0) temp_net=0.0;else temp_net=amt_net;
                if(coupan_amt==0.0)
                {
                    if(wallet_payment.isShown())
                        new QustomDialogBuilder(getActivity(), R.style.PauseDialog).
                                setTitleColor(Constants.greenPayU).
                                setDividerColor(Constants.greenPayU)
                                .setTitle("Payment Details")
                                .setMessage("**Bill Break Down**\n\n"+"Order Amount : Rs." + round((float)(amount/100)*100,2) + "\nConvenience Fee : Rs." + round((float)(amt_convenience/100)*100,2) + "\nTotal : Rs." + round((float)((amt_convenience+amount)/100)*100,2) + "\n\n**Payment Break Down**\n"+"\nDiscount : Rs." + round((float)(amt_discount/100)*100,2) + "\nAvailable PayUMoney points : Rs." + round((float)(cashback_amt_total/100)*100,2) + "\nNet Amount : Rs." +0.0+"\nWallet:Rs. "+round((float)(temp_wallet-wallet_usage),2))

                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Your code
                                    }
                                })

                                .show();
                    else
                        new QustomDialogBuilder(getActivity(), R.style.PauseDialog).
                                setTitleColor(Constants.greenPayU).
                                setDividerColor(Constants.greenPayU)
                                .setTitle("Payment Details")
                                .setMessage("**Bill Break Down**\n\n" + "Order Amount : Rs." + round((float) (amount / 100) * 100, 2) + "\nConvenience Fee : Rs." + round((float) (amt_convenience / 100) * 100, 2) + "\nTotal : Rs." + round((float) ((amt_convenience + amount) / 100) * 100, 2) + "\n\n**Payment Break Down**\n" + "\nDiscount : Rs." + round((float) (amt_discount / 100) * 100, 2) + "\nAvailable PayUMoney points : Rs." + round((float) (cashback_amt_total / 100) * 100, 2) + "\nNet Amount : Rs." + round((float) (temp_net * 100) / 100, 2))

                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Your code
                                    }
                                })

                                .show();
                }else {
                    if(wallet_payment.isShown())
                        new QustomDialogBuilder(getActivity(),R.style.PauseDialog).
                                setTitleColor(Constants.greenPayU).
                                setDividerColor(Constants.greenPayU)
                                .setTitle("Payment Details")
                                .setMessage("\n**Bill Break Down**\n\n"+"Order Amount : Rs." + round((float)(amount/100)*100,2) + "\nConvenience Fee : Rs." + round((float)(amt_convenience/100)*100,2) + "\nTotal : Rs." + round((float)((amt_convenience+amount)/100)*100,2) + "\n\n**Payment Break Down**\n"+"\nAvailable PayUMoney points : Rs." + round((float)(cashback_amt_total/100)*100,2) + "\nCoupon Discount :" + round((float)(coupan_amt/100)*100,2) + "\nNet Amount : Rs." + 0.0+"\nWallet :Rs. "+round((float)(temp_wallet-wallet_usage),2))

                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Your code
                                    }
                                })

                                .show();
                    else
                        new QustomDialogBuilder(getActivity(),R.style.PauseDialog).
                                setTitleColor(Constants.greenPayU).
                                setDividerColor(Constants.greenPayU)
                                .setTitle("Payment Details")
                                .setMessage("\n**Bill Break Down**\n\n" + "Order Amount : Rs." + round((float) (amount / 100) * 100, 2) + "\nConvenience Fee : Rs." + round((float) (amt_convenience / 100) * 100, 2) + "\nTotal : Rs." + round((float) ((amount + amt_convenience) / 100) * 100, 2) + "\n\n**Payment Break Down**\n" + "\nAvailable PayUMoney points : Rs." + round((float) (cashback_amt_total / 100) * 100, 2) + "\nCoupon Discount :" + round((float) (coupan_amt / 100) * 100, 2) + "\nNet Amount : Rs." + round((float) (temp_net * 100) / 100, 2))

                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Your code
                                    }
                                })

                                .show();
                }
            }
        });
        adapter= new PaymentModeAdapter(getActivity(), availableModes);
        listView.setAdapter(adapter);
        final JSONObject bankObject = new JSONObject(details.getJSONObject(Constants.PAYMENT_OPTION).getString("nb"));

        if(mProgressDialog!=null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mode = (String) listView.getAdapter().getItem(i);

                if (mode.equals("NB")) {
                    FragmentTransaction transaction;
                    transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                    try {
                        amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                        // cashbackDisplayamt = details.getJSONObject(Constants.PAYMENT).getDouble("");
                        amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject(mode).getDouble("DEFAULT");
                        amt_total = amount + amt_convenience;
                        amt_net = amt_total - amt_discount - coupan_amt-wallet; //amt_discount can be normal discount by PayUMoney OR coupan discount

                        cashback_amt = cashback_amt_total;
                        if (amt_net - cashback_amt <= 0&&amt_net>0)
                        {
                            cashback_amt_total = amt_net; // remove only these payupoints
                            pointDialog();
                        } else {
                            amt_net = amt_net - cashback_amt;  //Autodeduct

                            NetBankingFragment fragment = new NetBankingFragment();

                            Bundle bundle = new Bundle();
                            bundle.putString("details", details.toString());
                            bundle.putDouble("cashback_amt", round((float) (cashback_amt * 100) / 100,2));
                            bundle.putDouble("amt_net", round((float) (amt_net * 100) / 100,2));
                            bundle.putDouble("amount", round((float) (amount * 100) / 100,2));
                            bundle.putDouble("amt_convenience", round((float) (amt_convenience * 100) / 100,2));
                            bundle.putDouble("amt_total", round((float) (amt_total * 100) / 100,2));
                            bundle.putDouble("amt_discount", round((float) (amt_discount * 100) / 100,2));
                            bundle.putDouble("coupan_amt", round((float) (coupan_amt * 100) / 100,2));
                            bundle.putDouble("wallet",round((float) (wallet * 100) / 100,2));


                            fragment.setArguments(bundle);
                            ((HomeActivity) getActivity()).onPaymentOptionSelected(mode, details);

                            transaction.replace(R.id.fragmentContainer, fragment);
                            transaction.addToBackStack("a");
                            transaction.commit();
                            getActivity().getFragmentManager().executePendingTransactions();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }

                } else if (mode.equals("CC")) {
                    FragmentTransaction transaction;
                    transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                    try {
                        amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                        amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject(mode).getDouble("DEFAULT");
                        amt_total = amount + amt_convenience;
                        amt_net = amt_total - amt_discount - coupan_amt-wallet; //either 1 will be zero

                        cashback_amt = cashback_amt_total;

                        if (amt_net - cashback_amt <= 0&&amt_net>0) {
                            cashback_amt_total = amt_net; // remove only these payupoints
                            pointDialog();
                        } else {
                            amt_net = amt_net - cashback_amt;  //Autodeduct PayUPoints

                            CreditCardFragment fragment = new CreditCardFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("details", details.toString());
                            bundle.putDouble("cashback_amt", round((float) (cashback_amt * 100) / 100,2));
                            bundle.putDouble("amt_net", round((float) (amt_net * 100) / 100,2));
                            bundle.putDouble("amount", round((float) (amount * 100) / 100,2));
                            bundle.putDouble("amt_convenience", round((float) (amt_convenience * 100) / 100,2));
                            bundle.putDouble("amt_total", round((float) (amt_total * 100) / 100,2));
                            bundle.putDouble("amt_discount", round((float) (amt_discount * 100) / 100,2));
                            bundle.putDouble("coupan_amt", round((float) (coupan_amt * 100) / 100,2));
                            bundle.putDouble("wallet",round((float) (wallet * 100) / 100,2));

                            fragment.setArguments(bundle);

                            transaction.replace(R.id.fragmentContainer, fragment);
                            transaction.addToBackStack("a");
                            transaction.commit();
                            getActivity().getFragmentManager().executePendingTransactions();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "something went wrong", Toast.LENGTH_LONG).show();
                    }

                } else if (mode.equals("DC")) {
                    FragmentTransaction transaction;
                    transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                    try {
                        amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                        amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject(mode).getDouble("DEFAULT");
                        amt_total = amount + amt_convenience;
                        amt_net = amt_total - amt_discount - coupan_amt-wallet;

                        cashback_amt = cashback_amt_total;
                        if (amt_net - cashback_amt <= 0&&amt_net>0) {
                            cashback_amt_total = amt_net; // remove only these payupoints
                            pointDialog();
                        } else {
                            amt_net = amt_net - cashback_amt;  //Autodeduct

                            DebitCardFragment fragment = new DebitCardFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("details", details.toString());
                            bundle.putDouble("cashback_amt", round((float) (cashback_amt * 100) / 100,2));
                            bundle.putDouble("amt_net", round((float) (amt_net * 100) / 100,2));
                            bundle.putDouble("amount", round((float) (amount * 100) / 100,2));
                            bundle.putDouble("amt_convenience", round((float) (amt_convenience * 100) / 100,2));
                            bundle.putDouble("amt_total", round((float) (amt_total * 100) / 100,2));
                            bundle.putDouble("amt_discount", round((float) (amt_discount * 100) / 100,2));
                            bundle.putDouble("coupan_amt", round((float) (coupan_amt * 100) / 100,2));
                            bundle.putDouble("wallet",round((float) (wallet * 100) / 100,2));

                            fragment.setArguments(bundle);

                            transaction.replace(R.id.fragmentContainer, fragment);
                            transaction.addToBackStack("a");
                            transaction.commit();
                            getActivity().getFragmentManager().executePendingTransactions();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (mode.equals("STORED_CARDS")) {
                    try {
                        amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                        //Default Convinience value
                        amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("CC").getDouble("DEFAULT");
                        amt_total = amount + amt_convenience;
                        amt_net = amt_total - amt_discount - coupan_amt-wallet;

                        cashback_amt = cashback_amt_total;
                        if (amt_net - cashback_amt <= 0&&amt_net>0)
                        {
                            cashback_amt_total = amt_net; // remove only these payupoints
                            pointDialog();
                        } else {
                            amt_net = amt_net - cashback_amt;  //Autodeduct
                            StoredCardFragment fragment = new StoredCardFragment();
                            Bundle bundle = new Bundle();
                            bundle.putDouble("cashback_amt", round((float) (cashback_amt_total * 100) / 100,2));
                            bundle.putDouble("coupan_amt", round((float) (coupan_amt * 100) / 100,2));
                            bundle.putDouble("wallet",round((float) (wallet * 100) / 100,2));
                            bundle.putString("details", details.toString());

                            fragment.setArguments(bundle);
                            FragmentTransaction transaction;
                            transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);

                            transaction.replace(R.id.fragmentContainer, fragment);
                            transaction.addToBackStack("a");
                            transaction.commit();
                            getActivity().getFragmentManager().executePendingTransactions();
                        }
                    } //try close
                    catch (Exception e) {
                    } //catch
                } //else if ends
                else if (mode.equals("PAYU_MONEY_POINTS")) {

                    //   pointDialog(); //User clicked on PayU points option


                } else {
                    return;
                }

                ((HomeActivity) getActivity()).onPaymentOptionSelected(mode, details);

            }
        });

        /*****COUPONS******/
        if (details.getJSONArray("userCouponsAvailable").length()!=0&&amount>=1) //if coupons are available
        {
            for (int i = 0; i < details.getJSONArray("userCouponsAvailable").length(); i++)
            {
                if (details.getJSONArray("userCouponsAvailable").getJSONObject(i).getBoolean("enabled"))
                {
                    getView().findViewById(R.id.couponSection).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.selectCoupon).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.selectCoupon1).setVisibility(View.VISIBLE);
                    ((TextView) getView().findViewById(R.id.selectCoupon1)).setText(R.string.select_coupon_option);
                    ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.view_coupon);

                    break;
                }
            }
            JSONArray coupanList = new JSONArray();
            if (details.getJSONArray("userCouponsAvailable") != null)
            {
                for (int i = 0; i < details.getJSONArray("userCouponsAvailable").length(); i++)
                {
                    if (details.getJSONArray("userCouponsAvailable").getJSONObject(i).getBoolean("enabled"))
                        coupanList.put(details.getJSONArray("userCouponsAvailable").getJSONObject(i));
                }
            }
            coupanAdapter = new CouponListAdapter(getActivity(), coupanList);
            getView().findViewById(R.id.selectCoupon).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if ((((TextView) getView().findViewById(R.id.selectCoupon)).getText().toString()).equals("Remove"))
                    {
                        //  Toast.makeText(getActivity(),"hi",Toast.LENGTH_LONG).show()
                        try
                        {

                            amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("CC").getDouble("DEFAULT");
                            if(!details.getString("cashbackAccumulated").toString().equals("null"))
                            amt_discount = details.getJSONObject("cashbackAccumulated").getDouble(Constants.AMOUNT); // Give back discount
                            else
                                amt_discount=0.0;
                        }catch (Exception e){}

                        if(wallet_payment.isShown())
                        {
                            if(coupan_amt>0.0)
                            {
                                wallet = wallet + coupan_amt;
                                wallet=wallet-amt_discount;
                            }
                            else
                            {

                            }
                        }
                        /***Clean the coupons**/
                        coupan_amt=0.0;
                        amtafterCoupanDiscount = amtafterDicount - coupan_amt;
                        amt_net=amount-amt_discount;
                        if(cashback_amt_total<(amt_net+amt_convenience))
                        {
                            amt_net-=cashback_amt_total;
                        }
                        else
                        {
                            pointDialog();
                        }

                        //   amtafterCoupanDiscount=amtafterCoupanDiscount-cashback_amt_total;
                   //     if(cashback_amt_total>0.0)
                     //       HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);

                        ((TextView) getView().findViewById(R.id.selectCoupon1)).setText(R.string.select_coupon_option);
                        ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.view_coupon);
                        HomeActivity.mAmount.setText("Rs." + round((amt_net),2));
                        if(amt_net<0.0) HomeActivity.mAmount.setText("Rs." + 0.0);
                        HomeActivity.savings.setText("Total Savings : INR "+round((float)(amt_discount/100)*100,2));
                        HomeActivity.savings.setVisibility(View.VISIBLE);


                        wallet_usage=(temp_wallet-amtafterCoupanDiscount-amt_convenience);
                        walletbalance.setText("Your wallet balance after this transaction: "+ round(wallet_usage,2)) ;
                        amt_convenience=0.0;


                    }
                    else //If remove is not in text i.e. u are adding some coupons
                    {
                      // try{ amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("CC").getDouble("DEFAULT");}catch (Exception e){}
                        QustomDialogBuilder alertDialog = new QustomDialogBuilder(getActivity(),R.style.PauseDialog);

                        View convertView = (View) getActivity().getLayoutInflater().inflate(R.layout.coupon_list, null);


                        alertDialog.setView(convertView);

                        lv = (ListView) convertView.findViewById(R.id.lv);
                        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                        lv.setAdapter(coupanAdapter);
                        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                            }
                        });
                        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                coupan_amt = 0.0;
                                choosedItem = -1;
                                for (int j = 0; j < lv.getCount(); j++) {

                                    if (((RadioButton) lv.getChildAt(j).findViewById(R.id.coupanSelect)).isChecked()) {
                                        /******Apply the coupons*********/
                                        x = (JSONObject) lv.getAdapter().getItem(j);
                                        try {
                                            choosedCoupan = x.getString("couponString");
                                            choosedItem = j;
                                            choosedCoupanUser = x.getString("couponStringForUser");
                                            coupan_amt = x.getDouble("amount");
                                            Log.i("Choosed coupan", choosedCoupan);

                                            if(wallet_payment.isShown())
                                            {
                                                if(amt_discount>0.0)
                                                {
                                                    wallet = wallet + amt_discount;
                                                    wallet=wallet-coupan_amt;
                                                }
                                                else
                                                {

                                                }
                                            }

                                            amount= details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                                            amtafterCoupanDiscount = amount - coupan_amt;
                                           // amt_net +=amt_discount; //Add back the normal discount
                                            amt_net=amount-coupan_amt-cashback_amt_total; //Remove

                                            //Enough PayuPoints
                                            amt_convenience = new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");

                                            if(amtafterCoupanDiscount==0.0) //100% Coupon discount
                                            {
                                                if(wallet_payment.isShown())
                                                {
                                                    HomeActivity.mAmount.setText("Rs. "+round((amt_convenience),2));
                                                    wallet_usage=(temp_wallet-amt_net-amt_convenience);
                                                    walletbalance.setText("Your wallet balance after this transaction: "+round((float)(wallet_usage/100)*100,2));
                                                }
                                                else
                                                {
                                                    HomeActivity.mAmount.setText("Rs. "+0.0);
                                                }
                                            }
                                            else if (amtafterCoupanDiscount+amt_convenience  <= cashback_amt_total) //Sufficient points
                                            {
                                                if(amt_net<0.0)amt_net=0.0;
                                                pointDialog(); //If Enough points are present after getting a coupon discount
                                            }
                                            else
                                            {
                                                amt_convenience=0.0;
                                                if(amt_net<0.0)//A scenario in which points are more than convinience+Amount
                                                {
                                                    HomeActivity.mAmount.setText("Rs. " + 0.0);
                                                    amt_net=0.0;
                                                }
                                                else   //99% of time this will be triggered , i.e. coupon applied
                                                {
                                                    HomeActivity.mAmount.setText("Rs. "+round((amt_net),2));

                                                }

                                            }
                                            //Not enough but has some
                                            coupon_string="Congrats, " + x.getString("couponStringForUser") + " Applied  ";
                                            ((TextView) getView().findViewById(R.id.selectCoupon1)).setText(coupon_string);

                                            ((TextView) getView().findViewById(R.id.selectCoupon)).setText(R.string.remove);
                                            // temp_amt_discount=amt_discount; //Just in case coupon is removed
                                            amt_discount = 0.0;
                                            HomeActivity.savings.setText("Total Savings : INR "+round((float)(coupan_amt/100)*100,2));
                                            HomeActivity.savings.setVisibility(View.VISIBLE);
                                            amt_convenience=0.0;
                                            break;
                                        } catch (JSONException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    } else
                                    {

                                    }


                                }

                            }

                        });

                        alertDialog.show();

                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                adapter.notifyDataSetChanged();
                                final JSONObject selectedCoupan = (JSONObject) adapterView.getAdapter().getItem(i);

                                if (((RadioButton) view.findViewById(R.id.coupanSelect)).isChecked()) {
                                    ((RadioButton) view.findViewById(R.id.coupanSelect)).setChecked(false);
                                } else {
                                    ((RadioButton) view.findViewById(R.id.coupanSelect)).setChecked(true);
                                }


                                for (int j = 0; j < lv.getCount(); j++) {
                                    if (j != i)
                                        ((RadioButton) lv.getChildAt(j).findViewById(R.id.coupanSelect)).setChecked(false);
                                }

                            }
                        });

                    }
                }
            });

        }


    }

    public void walletDialog()
    {
        //Now for the Math
        try {
            amount = details.getJSONObject(Constants.PAYMENT).getDouble("totalAmount"); //Fetch the total payable Amount
            amt_convenience= new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");
            if (coupan_amt == 0.0&&cashback_amt_total>0.0) //Payupoints only +discount
            {
                amt_net = amount+amt_convenience - amt_discount;
                amt_net = amt_net-cashback_amt_total;

                showWalletwithPayu(cashback_amt_total,amt_discount,amt_net);
            }
            else if(coupan_amt>0.0&&cashback_amt_total>0.0) //Points+coupan
            {

                amt_net = amount +amt_convenience- coupan_amt;
                amt_net = amt_net - cashback_amt_total;
                showWalletwithPayu(cashback_amt_total,coupan_amt,amt_net);

            }
            else if(cashback_amt_total==0.0) //No PayUPoints
            {
                if(coupan_amt==0.0) //No Coupan
                {
                    amt_net = amount+amt_convenience-amt_discount;
                    showWallet(amt_discount,amt_net);
                }
                else if(coupan_amt>0.0) //Yes Coupon
                {

                    amt_net=amount+amt_convenience-coupan_amt;
                    showWallet(coupan_amt,amt_net);
                }
                else
                {
                    Toast.makeText(getActivity(),"Something went Wrong",Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(getActivity(),"Something went Wrong",Toast.LENGTH_LONG).show();;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showWallet( double dsc, final double net)
    {
        new QustomDialogBuilder(getActivity(),R.style.PauseDialog).
                setTitleColor(Constants.greenPayU).
                setDividerColor(Constants.greenPayU)
                .setTitle("Payment using Wallet")
                .setMessage("Yoo-hoo!\n" +
                        "\n" +
                        "You have enough money in PayUMoney Wallet for this transaction. All you need to do is confirm the payment by clicking on the OK button below and that's it.\n\nOrder Amount : Rs." + amount +"\nConvenient Fees: "+amt_convenience+"\nCashback/PayUPoints used : Rs."+0+ "\nDiscount : Rs." + round(dsc,2) + "\nWallet Money Used : Rs." + round(net,2)  + "\nRemaining Money in Wallet : Rs." +  round(temp_wallet - amt_net,2))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Your code
                        try {

                            Session.getInstance(getActivity()).sendToPayU(details, "wallet", data, net); //PURE WALLEt
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).setCancelable(false)
                .show();
    }
    public void showWalletwithPayu(final double pnts, double dsc, final double net)
    {
        new QustomDialogBuilder(getActivity(),R.style.PauseDialog).
                setTitleColor(Constants.greenPayU).
                setDividerColor(Constants.greenPayU)
                .setTitle("Payment using Wallet")
                .setMessage("Yoo-hoo!\n" +
                        "\n" +
                        "You have enough money in PayUMoney Wallet for this transaction. All you need to do is confirm the payment by clicking on the OK button below and that's it.\n\nOrder Amount : Rs." + amount +"\nConvenient Fees: "+amt_convenience+ "\nCashback/PayUPoints used : Rs." + pnts + "\nDiscount : Rs." + round(dsc,2) + "\nWallet Money Used : Rs." + round(net,2) + "\nRemaining Money in Wallet : Rs." + round(temp_wallet - amt_net,2))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Your code
                        try {

                            Session.getInstance(getActivity()).sendToPayUWithWallet(details, "wallet", data, net, pnts); //wallet +pnts
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).setCancelable(false)
                .show();
    }

    public void pointDialog() //If user has PayUpoints
    {

     //   if(coupan_amt>0.0) //No coupn
        {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Payment using PayUMOney points")
                    .setMessage("Yoo-hoo!\n" +
                            "\n" +
                            "You have enough PayUMoney points for this transaction. All you need to do is confirm the payment by clicking on the OK button below and that's it")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // Your code
                            PayUMoneyPointsFragment fragment = new PayUMoneyPointsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("details", details.toString());
                            bundle.putDouble("cashback_amt_total", cashback_amt_total);
                            bundle.putDouble("coupon", coupan_amt);

                            fragment.setArguments(bundle);
                            FragmentTransaction transaction;
                            transaction = ((HomeActivity) getActivity()).getFragmentManager().beginTransaction().setCustomAnimations(
                                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
                            //((HomeActivity) getActivity()).onPaymentOptionSelected(details);
                            transaction.replace(R.id.fragmentContainer, fragment,"payumoneypoints");
                            transaction.addToBackStack("a");
                            transaction.commit();
                            getActivity().getFragmentManager().executePendingTransactions();
                        }
                    })
                    .show();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new Dialog.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event)
                {

                    if (keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        FragmentManager fragmentManager = getActivity().getFragmentManager();
                        android.app.Fragment tempFragment=fragmentManager.findFragmentByTag("paymentOptions");
                        if (tempFragment!=null)
                        {
                            Intent intent = new Intent();
                            intent.putExtra(Constants.RESULT,"cancel");
                            getActivity().setResult(getActivity().RESULT_CANCELED, intent);
                            getActivity().finish();
                        }

                    } return true;

                }
            });
        }

    }

//Two round off ;)
    public static float round(float d, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
    public static float round(double d, int decimalPlace)
    {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void setMincon()
    {

         Double cccv=0.0,dccv=0.0,nbcv=0.0,walc=0.0; String min="";
        try{
        cccv=new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("CC").getDouble("DEFAULT");
        dccv=new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("DC").getDouble("DEFAULT");
        nbcv=new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("NB").getDouble("DEFAULT");
        walc=new JSONObject(details.getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject("WALLET").getDouble("DEFAULT");
        }catch (Exception e){}

        if(temp_wallet>0.0)
        {
            minconvfee.setText("**Min Conv. Fee is: Rs. "+walc+", if mode selected is wallet");
        }
        else
        {
           min= minCV(cccv,dccv,nbcv);
            if(min.equals("cccv"))
            {
                minconvfee.setText("**Min Conv. Fee is: Rs. "+cccv+" if mode selected is Credit Card");
            }
            else if(min.equals("dccv"))
            {
                minconvfee.setText("**Min Conv. Fee is: Rs. "+dccv+" if mode selected is Debit Card");
            }
            else if(min.equals("nbcv"))
            {
                minconvfee.setText("**Min Conv. Fee is: Rs. "+nbcv+" if mode selected is Net Banking");
            }
            else
            {
                //nothing
            }
        }



    }
    public String minCV(double a, double b, double c)
    {
        String min; double t;
        t=a;
        min="cccv";
        if(t>b) {min="dccv"; t=b;}
        if(t>c) min="nbcv";
        return min;
    }

    public ProgressDialog showProgress(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        final Drawable[] drawables = {getResources().getDrawable(R.drawable.nopoint),
                getResources().getDrawable(R.drawable.onepoint),
                getResources().getDrawable(R.drawable.twopoint),
                getResources().getDrawable(R.drawable.threepoint)
        };

        View layout = mInflater.inflate(R.layout.prog_dialog, null);
        final ImageView imageView; imageView = (ImageView) layout.findViewById(R.id.imageView);
        ProgressDialog progDialog = new ProgressDialog(context, R.style.ProgressDialog);
        timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                int i = -1;

                @Override
                synchronized public void run() {
                    (getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            i++;
                            if (i >= drawables.length) {
                                i = 0;
                            }
                            imageView.setImageBitmap(null);
                            imageView.destroyDrawingCache();
                            imageView.refreshDrawableState();
                            imageView.setImageDrawable(drawables[i]);
                        }
                    });

                }
            }, 0, 500);


        progDialog.show();
        progDialog.setContentView(layout);
        progDialog.setCancelable(true);
        progDialog.setCanceledOnTouchOutside(false);

        return progDialog;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(timer!=null)
            timer.cancel();
    }

}

