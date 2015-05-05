package com.payUMoney.sdk.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.payUMoney.sdk.CobbocEvent;
import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.HomeActivity;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.Session;
import com.payUMoney.sdk.WebViewActivity;
import com.payUMoney.sdk.adapter.StoredCardAdapter;
import com.payUMoney.sdk.dialog.QustomDialogBuilder;
import com.payUMoney.sdk.entity.Issuer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */

public class StoredCardFragment extends Fragment {


    public static ProgressDialog mProgressDialog;
    String selectedItem = "Credit card";
    boolean isDelete =false;
    String mode;

    //    static View mCvvView;
    public static Double amt_convenience=0.0, amt_total, amt_discount, amt_net=0.0, amount, cashback_amt, wallet;


    public StoredCardFragment()
    {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {

        View storedCardFragment = inflater.inflate(R.layout.fragment_stored_card, container, false);

       // pb=(ProgressBar)storedCardFragment.findViewById(R.id.pb);

        mProgressDialog = new ProgressDialog(getActivity());

        Session.getInstance(getActivity().getApplicationContext()).getMyCards();   //Called GetMyCards, which will fetch JSON for cards and fire event in EventBus

        storedCardFragment.findViewById(R.id.useNewCardButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getActivity().getFragmentManager().popBackStack();
            }
        });
        return storedCardFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
     //   mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventBus.getDefault().unregister(this);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void onEventMainThread(final CobbocEvent event)
    {
        if (event.getType() == CobbocEvent.CARDS)      //Event fired from OncreateView() -> Sessoion.getmycard()
        {
            if (event.getStatus())
            {
                onGetStoreCardDetails((JSONArray) event.getValue());  //Call function to set cards from JsonArray
            }
        }
        else if (event.getType() == CobbocEvent.PAYMENT)
        {
            if (event.getStatus())
            {
                Log.i("reached", "credit");
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(Constants.RESULT, event.getValue().toString());
                getActivity().startActivityForResult(intent, ((HomeActivity) getActivity()).WEB_VIEW);
            }
            else
            {
                Log.i("reached", "failed");
                //If not status do nothing
                Toast.makeText(getActivity(),"Payment Failed",Toast.LENGTH_LONG).show();
            }
        }
        if(event.getType() == CobbocEvent.CARD_DELETED) //If card s deleted
        {
            if(event.getStatus())
            {
               Session.getInstance(getActivity().getApplicationContext()).getMyCards();
                isDelete=true;
            }
        }
    }

    public void onGetStoreCardDetails(JSONArray storedCards)
    {

        final StoredCardAdapter adapter = new StoredCardAdapter(getActivity(), storedCards); //Initialize the adapter

        if (storedCards.length() < 1)  //If zero cards found
        {
            HomeActivity.mAmount.setText("n/a");
            getActivity().findViewById(R.id.noCardFoundTextView).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.savedCardTextView).setVisibility(View.GONE);
           /*HomeActivity.mPayUpoints.setVisibility(View.GONE);*/
            HomeActivity.savings.setVisibility(View.GONE);
           /* if(isDelete) {
                amt_net = amt_net - amt_convenience;
                amt_convenience = 0.0;
                isDelete=false;
                HomeActivity.mAmount.setText("Rs. " + round(amt_net, 2));
                HomeActivity.mPayUpoints.setText("Convenience Charge added : " + round((float) (amt_convenience / 100) * 100, 2) + " ,PayUPoints charged : " + round(cashback_amt, 2) + " and Discount of: " + round(amt_discount, 2));
            }*/
        }

        final ListView listView = (ListView) getActivity().findViewById(R.id.storedCardListView);  //Initialize ListView

        listView.setAdapter(adapter); //Adapter given

        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        mProgressDialog.dismiss();  //Hide progressbar

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final JSONObject selectedCard = (JSONObject) adapterView.getAdapter().getItem(i);

                try
                {
                    if (selectedCard.getString("cardMode").equals("CC"))
                        mode = "CC";
                    else
                        mode = "DC";
                    amount = ((HomeActivity) getActivity()).getBankObject().getJSONObject(Constants.PAYMENT).getDouble("totalAmount");
                    amt_convenience = new JSONObject(((HomeActivity) getActivity()).getBankObject().getJSONObject(Constants.TRANSACTION_DTO).getString("convenienceFeeCharges")).getJSONObject(mode).getDouble("DEFAULT");
                    amt_total = amount + amt_convenience;
                    if(!((HomeActivity) getActivity()).getBankObject().getString("cashbackAccumulated").equals("null"))
                    amt_discount = ((HomeActivity) getActivity()).getBankObject().getJSONObject("cashbackAccumulated").getDouble(Constants.AMOUNT);
                    else
                    amt_discount=0.0;
                   if(getArguments().getDouble("coupan_amt")==0.0) {
                       amt_net = amt_total - amt_discount;
                   }
                    else {
                       amt_discount=0.0;
                       amt_net = amt_total - getArguments().getDouble("coupan_amt");
                   }


                        cashback_amt = getArguments().getDouble("cashback_amt");
                     wallet=getArguments().getDouble("wallet");
                        amt_net = amt_net - cashback_amt-wallet;

                    /*HomeActivity.mPayUpoints.setText("Convenience Charge added : "+round((float)(amt_convenience/100)*100,2)+" ,PayUPoints charged : "+round(cashback_amt,2)+" and Discount of: "+round(amt_discount,2));
                    HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Double x = getArguments().getDouble("amt_net");

                HomeActivity.mAmoutDetails.setVisibility(View.VISIBLE);
                HomeActivity.mAmount.setText("Rs." + round(amt_net,2));
                HomeActivity.mAmoutDetails.setOnClickListener(new View.OnClickListener() {
                    //@Override
                    public void onClick(View v) {
                        if(getArguments().getDouble("coupan_amt")==0.0) {

                            new QustomDialogBuilder(getActivity(), R.style.PauseDialog).
                                    setTitleColor(Constants.greenPayU).
                                    setDividerColor(Constants.greenPayU)
                                    .setTitle("Payment Details")
                                    .setMessage( "**Bill Break Down**\n\n"+"Order Amount : Rs." + round((float)(amount * 100) / 100,2) + "\nConvenience Fee : Rs." + round((float)(amt_convenience * 100) / 100,2) + "\nTotal : Rs." + round((float) (amt_total * 100) / 100,2) + "\n\n**Payment Break Down**\n"+"\nDiscount : Rs." + round((float) (amt_discount * 100) / 100,2) + "\nPayUMoney points : Rs." + round(cashback_amt,2)  + "\nNet Amount : Rs." + round((float) (amt_net * 100) / 100,2)+"\nWallet : Rs."+round(wallet,2))

                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Your code
                                        }
                                    })

                                    .show();
                        }else{
                            new QustomDialogBuilder(getActivity(), R.style.PauseDialog).
                                    setTitleColor(Constants.greenPayU).
                                    setDividerColor(Constants.greenPayU)
                                    .setTitle("Payment Details")
                                    .setMessage( "**Bill Break Down**\n\n"+"Order Amount : Rs." + round((float)(amount * 100) / 100,2) + "\nConvenience Fee : Rs." + round((float)(amt_convenience * 100) / 100,2) + "\nTotal : Rs." + round((float) (amt_total * 100) / 100,2) + "\n\n**Payment Break Down**\n"+"\nPayUMoney points : Rs." + round(cashback_amt,2)  + "\nCoupon Discount :" + round(getArguments().getDouble("coupan_amt"),2) + "\nNet Amount : Rs." + round((float) (amt_net * 100) / 100,2)+"\nWallet : Rs."+round(wallet,2))

                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Your code
                                        }
                                    })

                                    .show();
                        }
                    }
                });
                    if (adapter.getSelectedCard() != i) {
                        adapter.setSelectedCard(i);
                        adapter.notifyDataSetInvalidated();
                    }
            }
        });

        HomeActivity.mAmoutDetails.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"Please select a card first",Toast.LENGTH_LONG).show();
            }

            });
    }

    public static Issuer getIssuer(String mNumber, String cardMode) {
        if(mNumber.length()>3) {
            if (mNumber.startsWith("4")) {
                return Issuer.VISA;
            } else if (Arrays.asList("6304", "6706", "6771", "6709").contains(mNumber.substring(0, 4))) {
                return Issuer.LASER;
            }/* else if(mNumber.matches("6(?:011|5[0-9]{2})[0-9]{12}[\\d]+")) {
            return "DISCOVER";
        }*/ else if (mNumber.matches("(5[06-8]|6\\d|\\D)\\d{14}|\\D{14}(\\d{2,3}|\\D{2,3})?[\\d|\\D]+") || mNumber.matches("(5[06-8]|6\\d|\\D)[\\d|\\D]+") || mNumber.matches("((504([435|645|774|775|809|993]))|(60([0206]|[3845]))|(622[018])\\d|\\D)[\\d|\\D]+")) {
                return Issuer.MAESTRO;
            } else if (mNumber.matches("^5[1-5][\\d|\\D]+")) {
                return Issuer.MASTERCARD;
            } else if (mNumber.matches("^3[47][\\d|\\D]+")) {
                return Issuer.AMEX;
            } else if (mNumber.startsWith("36") || mNumber.matches("^30[0-5][\\d|\\D]+")) {
                return Issuer.DINER;
            } else if (mNumber.matches("2(014|149)[\\d|\\D]+")) {
                return Issuer.DINER;
            } else if(mNumber.matches("^35(2[89]|[3-8][0-9])[\\d|\\D]+")) {
                return Issuer.JCB;
            } else {
                if (cardMode.contentEquals("CC"))
                    return Issuer.UNKNOWN;
                else if (cardMode.contentEquals("DC"))
                    return Issuer.MASTERCARD;
            }
        }
        return null;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
           // HomeActivity.mPayUpoints.setVisibility(View.GONE);
    }

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
}
