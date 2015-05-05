package com.payUMoney.sdk.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.HomeActivity;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.Session;
import com.payUMoney.sdk.SetupCardDetails;
import com.payUMoney.sdk.dialog.QustomDialogBuilder;
import com.payUMoney.sdk.entity.Issuer;
import com.payUMoney.sdk.fragment.PaymentOptionsFragment;
import com.payUMoney.sdk.fragment.StoredCardFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by franklin on 10/7/14.
 */
public class StoredCardAdapter extends BaseAdapter

{
    Context mContext;
    JSONArray mStoredCards;
    int toggle_flag = 0;
    String mode;

    private int mSelectedCard = -1;

    public StoredCardAdapter(Context context, JSONArray storedCards)
    {
        this.mContext = context;
        this.mStoredCards = storedCards;
    }

    @Override
    public int getCount() {
        return mStoredCards.length();
    }

    @Override
    public Object getItem(int i)
    {
        try
        {
            return mStoredCards.get(i);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup)
    {
        if (view == null)
        {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.card, null);
        }

        if(view.findViewById(R.id.cvvBox) != null)
        {
            ((ViewGroup) view.findViewById(R.id.cvvBox).getParent()).removeView(view.findViewById(R.id.cvvBox));
        }

        final View cvvBox = ((LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.enter_cvv, null);

        final JSONObject jsonObject1 = (JSONObject) getItem(position);

        // set text here
        try {
            int img;
            switch (StoredCardFragment.getIssuer(jsonObject1.getString("cardNumber"),jsonObject1.getString("cardMode")))
            {
                case LASER:
                    img = R.drawable.laser;
                    break;
                case VISA:
                    img = R.drawable.visa;
                    break;
                case MASTERCARD:
                    img = R.drawable.mastercard;
                    break;
                case MAESTRO:
                    img = R.drawable.maestro;
                    break;
                case JCB:
                    img = R.drawable.jcb;
                    break;
                case DINER:
                    img = R.drawable.diner;
                    break;
                case AMEX:
                    img = R.drawable.amex;
                    break;
                default:
                    img = R.drawable.card;
                    break;
            }
            ImageView imageview = (ImageView) view.findViewById(R.id.icon);
            imageview.setImageDrawable(view.getResources().getDrawable(img));

            ((TextView) view.findViewById(R.id.label)).setText(jsonObject1.getString("cardName"));
            ((TextView) view.findViewById(R.id.number)).setText(jsonObject1.getString("cardNumber"));


             view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    new QustomDialogBuilder(mContext, R.style.PauseDialog).
                            setTitleColor(Constants.greenPayU).
                            setDividerColor(Constants.greenPayU)
                            .setTitle("Delete Card")
                            .setMessage("Are you sure you want to delete this card?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    try
                                    {
                                       // Toast.makeText(mContext, .get("storeCardInfoId").toString(), Toast.LENGTH_LONG).show();
                                        Session.getInstance(mContext).deleteCard(Session.getInstance(mContext).cards.getJSONObject(position).getInt("storeCardInfoId"));



                                    }
                                    catch(Exception e)
                                    {

                                    }

                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(R.drawable.error_icon)
                            .show();
                }
            });

            //If Card layout is clicked
            if (position == getSelectedCard())
            {

                ((ViewGroup) view).addView(cvvBox, 1);

                final JSONObject jsonObject = (JSONObject) getItem(getSelectedCard());

                final HashMap<String, Object> data = new HashMap<String, Object>();

                data.put("storeCardId", jsonObject.getString("storeCardInfoId"));

                data.put("store_card_token", jsonObject.getString("cardToken"));

                data.put(Constants.LABEL, jsonObject.getString("cardName"));

                data.put(Constants.NUMBER, "");

                if (jsonObject.getString("cardMode").equals("CC"))
                    mode = "CC";
                else
                    mode = "DC";

                data.put("key", ((HomeActivity) mContext).getBankObject().getJSONObject("paymentOption").getString("publicKey").replaceAll("\\r", ""));


                if (!SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode).equals("MAES"))
                {
                    cvvBox.findViewById(R.id.makePayment).setEnabled(false);

                    ((EditText) cvvBox.findViewById(R.id.cvv)).setHint("Cvv");

                    if (SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode).equals("AMEX"))
                    {
                        data.put("bankcode", Constants.AMEX);
                        ((EditText) cvvBox.findViewById(R.id.cvv)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                    }
                    else
                    {
                        data.put("bankcode", SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode));

                        ((EditText) cvvBox.findViewById(R.id.cvv)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                    }

                    ((EditText) cvvBox.findViewById(R.id.cvv)).addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable)
                        {
                         try {
                                if (SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode).equals("AMEX") && editable.toString().length() >= 4)
                                {
                                    cvvBox.findViewById(R.id.makePayment).setEnabled(true);
                                }
                                else if (!SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode).equals("AMEX")&& editable.toString().length() >= 3)
                                {
                                    cvvBox.findViewById(R.id.makePayment).setEnabled(true);
                                }
                                else {
                                    cvvBox.findViewById(R.id.makePayment).setEnabled(false);
                                }
                             }
                         catch (JSONException e)
                             {
                                    e.printStackTrace();
                             }
                        }
                    });

                    cvvBox.findViewById(R.id.makePayment).setOnClickListener(new View.OnClickListener()
                      {
                        @Override
                        public void onClick(View view)
                        {
                          //  Toast.makeText(mContext,String.valueOf(position),Toast.LENGTH_LONG).show();
                            data.put(Constants.CVV, ((EditText) cvvBox.findViewById(R.id.cvv)).getText().toString());
                            data.put(Constants.EXPIRY_MONTH, "");
                            data.put(Constants.EXPIRY_YEAR, "");
                            StoredCardFragment.mProgressDialog.setMessage("Please wait");
                            StoredCardFragment.mProgressDialog.setIndeterminate(true);
                            StoredCardFragment.mProgressDialog.show();
                            try
                                {
                                    //if(PaymentOptionsFragment.wallet==0.0)
                                 Session.getInstance(mContext).sendToPayUWithWallet(((HomeActivity) mContext).getBankObject(), mode, data, StoredCardFragment.cashback_amt,PaymentOptionsFragment.wallet);
                                 //   Toast.makeText(mContext,"no error",Toast.LENGTH_LONG).show();
                                }
                            catch (JSONException e)
                                {
                                    e.printStackTrace();
                                   // Tast.makeText(mContext,e.toString(),Toast.LENGTH_LONG).show();
                                }

                        }
                    });

                }

                else
                   {
                    cvvBox.findViewById(R.id.makePayment).setEnabled(true);

                    ((EditText) cvvBox.findViewById(R.id.cvv)).setHint("Cvv(Optional)");

                    data.put("bankcode", SetupCardDetails.findIssuer(jsonObject.getString("cardNumber"),mode));

                    ((EditText) cvvBox.findViewById(R.id.cvv)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});


                    ((EditText) cvvBox.findViewById(R.id.cvv)).addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
                        {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable)
                        {
                            if (editable.toString().length() == 0)
                                {
                                  cvvBox.findViewById(R.id.makePayment).setEnabled(true);
                                }
                            else if (editable.toString().length() > 0 && editable.toString().length() < 3)
                                {
                                  cvvBox.findViewById(R.id.makePayment).setEnabled(false);
                                }
                            else if (editable.toString().length() > 0 && editable.toString().length() >= 3)
                                {
                                  cvvBox.findViewById(R.id.makePayment).setEnabled(true);
                                }
                        }
                    });

                    cvvBox.findViewById(R.id.makePayment).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {


                            StoredCardFragment.mProgressDialog.setMessage("Please wait");
                            StoredCardFragment.mProgressDialog.setIndeterminate(true);
                            //   mProgressDialog.setCancelable(false);
                            StoredCardFragment.mProgressDialog.show();

                            if (((EditText) cvvBox.findViewById(R.id.cvv)).getText().toString().length() > 0)
                            {
                                data.put(Constants.CVV, ((EditText) cvvBox.findViewById(R.id.cvv)).getText().toString());
                            }
                            else
                            {
                                data.put(Constants.CVV, "123");
                            }

                            data.put(Constants.EXPIRY_MONTH, "");
                            data.put(Constants.EXPIRY_YEAR, "");

                            try
                            {
                                Session.getInstance(mContext).sendToPayUWithWallet(((HomeActivity) mContext).getBankObject(), mode, data, StoredCardFragment.cashback_amt,StoredCardFragment.wallet);
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
//
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return view;

    }

    public int getSelectedCard() {
        return mSelectedCard;
    }

    public void setSelectedCard(int selectedCard) {
        mSelectedCard = selectedCard;
    }
}
