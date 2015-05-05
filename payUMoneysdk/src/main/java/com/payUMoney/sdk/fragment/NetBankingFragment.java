package com.payUMoney.sdk.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.payUMoney.sdk.CobbocEvent;
import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.HomeActivity;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.Session;
import com.payUMoney.sdk.WebViewActivity;
import com.payUMoney.sdk.adapter.NetBankingAdapter;
import com.payUMoney.sdk.dialog.QustomDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;

import de.greenrobot.event.EventBus;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class NetBankingFragment extends Fragment  {



    ProgressDialog mProgressDialog;
    String mid,amt,banklist,bankCode;
    JSONArray bankName = new JSONArray();
    JSONObject bankObject;
    private ProgressBar pb;
    JSONObject details;

    public NetBankingFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

          mid = ((HomeActivity)getActivity()).getMid();
          amt = ((HomeActivity)getActivity()).getAmt();
         details = ((HomeActivity)getActivity()).getBankObject();


        float a = (float)Math.round(getArguments().getDouble("amt_net")*100)/100;
        HomeActivity.mAmoutDetails.setVisibility(View.VISIBLE);

        HomeActivity.mAmount.setText("Rs." + a);

        HomeActivity.mAmoutDetails.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                if(getArguments().getDouble("coupan_amt")==0.0) {
                    new QustomDialogBuilder(getActivity(), R.style.PauseDialog).
                            setTitleColor(Constants.greenPayU).
                            setDividerColor(Constants.greenPayU)
                            .setTitle("Payment Details")
                            .setMessage(
                                    "**Bill Break Down**\n\n"+
                                            "Order Amount : Rs." +round(getArguments().getDouble("amount"),2) + "\nConvenience Fee : Rs." + round(getArguments().getDouble("amt_convenience"),2)
                                            + "\nTotal : Rs." + round(getArguments().getDouble("amt_total"),2)
                                            +"\n\n**Payment Break Down**\n"
                                            + "\nDiscount : Rs." + round(getArguments().getDouble("amt_discount"),2)
                                            + "\nPayUMoney points : Rs." + round(getArguments().getDouble("cashback_amt"),2)
                                            + "\nNet Amount : Rs." + round(getArguments().getDouble("amt_net"),2)
                                            + "\nWallet : Rs."+round(getArguments().getDouble("wallet"),2))
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
                            .setMessage( "**Bill Break Down**\n\n"+
                                    "Order Amount : Rs." + round(getArguments().getDouble("amount"),2) +
                                    "\nConvenience Fee : Rs." + round(getArguments().getDouble("amt_convenience"),2) +
                                    "\nTotal : Rs." + round(getArguments().getDouble("amt_total"),2) +
                                    "\n\n**Payment Break Down**\n"+
                                    "\nPayUMoney points : Rs." + round(getArguments().getDouble("cashback_amt"),2)
                                    + "\nCoupon Discount :" + round(getArguments().getDouble("coupan_amt"),2)
                                    + "\nNet Amount : Rs." + round(getArguments().getDouble("amt_net"),2)
                                    + "\nWallet" +round(getArguments().getDouble("wallet"),2))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Your code
                                }
                            })

                            .show();
                }            }
        });

        final View netBankingFragment = inflater.inflate(R.layout.fragment_net_banking, container, false);

        pb=(ProgressBar)netBankingFragment.findViewById(R.id.pb);

        netBankingFragment.findViewById(R.id.nbPayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final HashMap<String, Object> data = new HashMap<String, Object>();
                try {

                    data.put("bankcode", bankCode);
                    data.put("key",((HomeActivity)getActivity()).getBankObject().getJSONObject("paymentOption").getString("publicKey").replaceAll("\\r", ""));

                    pb.setVisibility(View.VISIBLE);
                      Session.getInstance(getActivity()).sendToPayUWithWallet(((HomeActivity)getActivity()).getBankObject(),"NB",data,getArguments().getDouble("cashback_amt"),getArguments().getDouble("wallet"));
                } catch (JSONException e) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


            }
        });
        netBankingFragment.findViewById(R.id.useNewCardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });

                return netBankingFragment;

    }
    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);


    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    public void onEventMainThread(final CobbocEvent event) {
        if (event.getType() == CobbocEvent.PAYMENT) {
            if (event.getStatus()) {
                pb.setVisibility(View.VISIBLE);
                Log.i("reached","here");

               Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(Constants.RESULT, event.getValue().toString());
                getActivity().startActivityForResult(intent,((HomeActivity)getActivity()).WEB_VIEW);
            } else {
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // fetch the data once again if last fetched at is less than 15 min
        try {
            bankObject = new JSONObject(((HomeActivity)getActivity()).getBankObject().getJSONObject(Constants.PAYMENT_OPTION).getString("nb"));
            JSONArray keyNames = bankObject.names();

            final String[][] banks1 = new String[102][2];

            for (int j = 0; j < keyNames.length(); j++) {
                String code = keyNames.getString(j);
                JSONObject object = bankObject.getJSONObject(code);

                banks1[j][0] = object.getString("pt_priority");
                banks1[j][1] = object.getString("title");
                //map.put(Integer.valueOf(object.getString("pt_priority")), object.getString("title"));
            }

            for (int j = 0; j < keyNames.length(); j++) {
                for (int k = j + 1; k < keyNames.length(); k++) {
                    if (Integer.valueOf(banks1[k][0]) < Integer.valueOf(banks1[j][0])) {
                        String tmpRow[] = banks1[k];
                        banks1[k] = banks1[j];
                        banks1[j] = tmpRow;
                    }
                }
            }
            final String banks[] = new String[keyNames.length()];

            for (int j = 0; j < keyNames.length(); j++) {
                banks[j] = banks1[j][1];
            }
            setupAdapter(banks);
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private void setupAdapter(String banks[]) {

        NetBankingAdapter adapter = new NetBankingAdapter(getActivity(), banks);

        Spinner netBankingSpinner = (Spinner) getActivity().findViewById(R.id.netBankingSpinner);
        netBankingSpinner.setAdapter(adapter);

        //String text = netBankingSpinner.getSelectedItem().toString();
        netBankingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
Log.i("Item",item.toString());
                Iterator bankCodes = bankObject.keys();
                try {
                    while (bankCodes.hasNext()) {
                        final String code = (String) bankCodes.next();
                        JSONObject object = bankObject.getJSONObject(code);
                        if (object.getString("title").equals(item.toString())) {
                            bankCode = code;
                            getActivity().findViewById(R.id.nbPayButton).setEnabled(true);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if(getArguments().getDouble("cashback_amt")==0.0);
            /*HomeActivity.mPayUpoints.setVisibility(View.GONE);*/
    }
    @Override
    public void onAttach(Activity a)
    {
      /*  HomeActivity.mPayUpoints.setText("Convenience Charge added : "+round((float) (getArguments().getDouble("amt_convenience") / 100) * 100, 2)+" ,PayUPoints charged : "+round((float) (getArguments().getDouble("cashback_amt") / 100) * 100, 2)+" and Discount of: "+round((float) (getArguments().getDouble("amt_discount") / 100) * 100, 2));
        HomeActivity.mPayUpoints.setVisibility(View.VISIBLE);*/
        a=getActivity();
        super.onAttach(a);
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
