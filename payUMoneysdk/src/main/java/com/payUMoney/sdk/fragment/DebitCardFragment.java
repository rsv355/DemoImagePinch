package com.payUMoney.sdk.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.payUMoney.sdk.CobbocEvent;
import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.HomeActivity;
import com.payUMoney.sdk.Luhn;
import com.payUMoney.sdk.R;
import com.payUMoney.sdk.Session;
import com.payUMoney.sdk.SetupCardDetails;
import com.payUMoney.sdk.WebViewActivity;
import com.payUMoney.sdk.entity.Card;
import com.payUMoney.sdk.dialog.QustomDialogBuilder;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by piyush on 7/11/14.
 */
public class DebitCardFragment extends Fragment{

    private int expiryMonth = 7;
    private int expiryYear = 2025;
    private String cardNumber = "";
    private String cvv = "";
    private ProgressBar pb;

    DatePickerDialog.OnDateSetListener mDateSetListener;
    int mYear;
    int mMonth;
    int mDay;

    Boolean isCardNumberValid = false;
    Boolean isExpired = true;
    Boolean isCvvValid = false;
    Boolean card_store_check = true;

    Drawable cardNumberDrawable;
    Drawable calenderDrawable;
    Drawable cvvDrawable;
    private CheckBox mCardStore;
    private EditText mCardLabel;

    public DebitCardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View debitCardDetails = inflater.inflate(R.layout.fragment_debit_card_details, container, false);
        mYear = Calendar.getInstance().get(Calendar.YEAR);
        mMonth = Calendar.getInstance().get(Calendar.MONTH);
        mDay = Calendar.getInstance().get(Calendar.DATE);
        mCardLabel = (EditText) debitCardDetails.findViewById(R.id.label);
        mCardStore = (CheckBox) debitCardDetails.findViewById(R.id.store_card);
        pb=(ProgressBar)debitCardDetails.findViewById(R.id.pb);

        float a = (float)(getArguments().getDouble("amt_net") * 100)/100;
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
                                            "Order Amount : Rs." + round(getArguments().getDouble("amount"),2)+ "\nConvenience Fee : Rs." + round(getArguments().getDouble("amt_convenience"),2)
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
        mCardStore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mCardStore.isChecked()) {
                    card_store_check = true;
                    mCardLabel.setVisibility(View.VISIBLE);
                } else {
                    card_store_check = false;
                    mCardLabel.setVisibility(View.GONE);
                }
            }
        });


        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                ((TextView) debitCardDetails.findViewById(R.id.expiryDatePickerEditText)).setText("" + (i2 + 1) + " / " + i);

                expiryMonth = i2 + 1;
                expiryYear = i;
                if (expiryYear > Calendar.getInstance().get(Calendar.YEAR)) {
                    isExpired = false;
                    valid(((EditText) getActivity().findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                } else if (expiryYear == Calendar.getInstance().get(Calendar.YEAR) && expiryMonth - 1 >= Calendar.getInstance().get(Calendar.MONTH)) {
                    isExpired = false;
                    valid(((EditText) getActivity().findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                } else {
                    isExpired = true;
                    invalid(((EditText) getActivity().findViewById(R.id.expiryDatePickerEditText)), calenderDrawable);
                }
            }
        };

        debitCardDetails.findViewById(R.id.expiryDatePickerEditText).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    SetupCardDetails.customDatePicker(getActivity(), mDateSetListener, mYear, mMonth, mDay).show();
                }
                return false;
            }
        });
        debitCardDetails.findViewById(R.id.useNewCardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });
        debitCardDetails.findViewById(R.id.makePayment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cardNumber = ((TextView) debitCardDetails.findViewById(R.id.cardNumberEditText)).getText().toString();

                final HashMap<String, Object> data = new HashMap<String, Object>();
                try {
                    if(cvv.equals("") || cvv==null)
                        data.put(Constants.CVV, "123");
                    else
                    data.put(Constants.CVV, cvv);
                    data.put(Constants.EXPIRY_MONTH, expiryMonth);
                    data.put(Constants.EXPIRY_YEAR, expiryYear);
                    data.put(Constants.NUMBER, cardNumber);
                    data.put("key",((HomeActivity)getActivity()).getBankObject().getJSONObject("paymentOption").getString("publicKey").replaceAll("\\r", ""));
                    if (Card.isAmex(cardNumber))
                    {
                        data.put("bankcode", Constants.AMEX);
                    } else {
                        data.put("bankcode", SetupCardDetails.findIssuer(cardNumber,"DC"));
                     //  data.put("bankcode", Card.getType(cardNumber));
                    }
                    if (card_store_check == true)
                    {
                        if (mCardLabel.getText().toString().trim().length() == 0)
                        {
                            data.put(Constants.LABEL, "payu");
                            data.put(Constants.STORE, "1");
                        }
                        else
                        {
                            data.put(Constants.LABEL, mCardLabel.getText().toString());
                            data.put(Constants.STORE, "1");
                        }
                    }
                    pb.setVisibility(View.VISIBLE);

                    Session.getInstance(getActivity()).sendToPayUWithWallet(((HomeActivity)getActivity()).getBankObject(),"DC",data,getArguments().getDouble("cashback_amt"),getArguments().getDouble("wallet"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

        return debitCardDetails;

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
    public void onEventMainThread(final CobbocEvent event)
    {
        if (event.getType() == CobbocEvent.PAYMENT)
        {
            if (event.getStatus())
            {
                pb.setVisibility(View.GONE);
                Log.i("reached","here");
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                intent.putExtra(Constants.RESULT, event.getValue().toString());
                getActivity().startActivityForResult(intent,((HomeActivity)getActivity()).WEB_VIEW);
            } else
            {
                //Log.i("faile")
                Log.i("failed","failed");
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cardNumberDrawable = getResources().getDrawable(R.drawable.card);
        calenderDrawable = getResources().getDrawable(R.drawable.calendar);
        cvvDrawable = getResources().getDrawable(R.drawable.lock);

        cardNumberDrawable.setAlpha(100);
        calenderDrawable.setAlpha(100);
        cvvDrawable.setAlpha(100);

        ((TextView)getActivity().findViewById(R.id.enterCardDetailsTextView)).setText(getString(R.string.enter_debit_card_details));

        ((EditText) getActivity().findViewById(R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cardNumberDrawable, null);
        ((EditText) getActivity().findViewById(R.id.expiryDatePickerEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, calenderDrawable, null);
        ((EditText) getActivity().findViewById(R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, cvvDrawable, null);



        ((EditText) getActivity().findViewById(R.id.cardNumberEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cardNumber = ((EditText) getActivity().findViewById(R.id.cardNumberEditText)).getText().toString();

                if (cardNumber.startsWith("34") || cardNumber.startsWith("37"))
                    ((EditText) getActivity().findViewById(R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                else
                    ((EditText) getActivity().findViewById(R.id.cvvEditText)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});


                if (SetupCardDetails.findIssuer(cardNumber, "DC") == "MAES")
                {
                    // disable cvv and expiry
                    getActivity().findViewById(R.id.expiryCvvLinearLayout).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.haveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    if (cardNumber.length() > 11 && Luhn.validate(cardNumber)) {
                        isCardNumberValid = true;
                        valid(((EditText) getActivity().findViewById(R.id.cardNumberEditText)), SetupCardDetails.getCardDrawable(getResources(), cardNumber));
                    } else {
                        isCardNumberValid = false;
                        invalid(((EditText) getActivity().findViewById(R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        resetHeader();
                    }
                } else {
                    // enable cvv and expiry
                    getActivity().findViewById(R.id.expiryCvvLinearLayout).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.haveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    getActivity().findViewById(R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                    if (cardNumber.length() > 11 && Luhn.validate(cardNumber)) {
                        isCardNumberValid = true;
                        valid(((EditText) getActivity().findViewById(R.id.cardNumberEditText)), SetupCardDetails.getCardDrawable(getResources(), cardNumber));
                    } else {
                        isCardNumberValid = false;
                        invalid(((EditText) getActivity().findViewById(R.id.cardNumberEditText)), cardNumberDrawable);
                        cardNumberDrawable.setAlpha(100);
                        resetHeader();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ((EditText) getActivity().findViewById(R.id.cvvEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                cvv = ((EditText) getActivity().findViewById(R.id.cvvEditText)).getText().toString();
                if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
                    if (cvv.length() == 4) {
                        //valid
                        isCvvValid = true;
                        valid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);

                    } else {
                        //invalid
                        isCvvValid = false;
                        invalid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);
                    }
                } else {
                    if (cvv.length() == 3) {
                        //valid
                        isCvvValid = true;
                        valid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);
                    } else {
                        //invalid
                        isCvvValid = false;
                        invalid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });
        getActivity().findViewById(R.id.haveClickHereTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.expiryCvvLinearLayout).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.haveCvvExpiryLinearLayout).setVisibility(View.GONE);
                getActivity().findViewById(R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                isCvvValid = false;
                isExpired = true;
                ((EditText) getActivity().findViewById(R.id.expiryDatePickerEditText)).getText().clear();
                ((EditText) getActivity().findViewById(R.id.cvvEditText)).getText().clear();
                invalid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);

            }
        });

        getActivity().findViewById(R.id.dontHaveClickHereTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().findViewById(R.id.expiryCvvLinearLayout).setVisibility(View.GONE);
                getActivity().findViewById(R.id.haveCvvExpiryLinearLayout).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.dontHaveCvvExpiryLinearLayout).setVisibility(View.GONE);
                valid(((EditText) getActivity().findViewById(R.id.cvvEditText)), cvvDrawable);
            }
        });

        getActivity().findViewById(R.id.cardNumberEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });


        getActivity().findViewById(R.id.cvvEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

        getActivity().findViewById(R.id.expiryDatePickerEditText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    makeInvalid();
                }
            }
        });

    }

    private void valid(EditText editText, Drawable drawable) {
        drawable.setAlpha(255);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        if (getActivity().findViewById(R.id.expiryCvvLinearLayout).getVisibility() == View.GONE) {
            isExpired = false;
            isCvvValid = true;
        }
        if (isCardNumberValid && !isExpired && isCvvValid) {
            getActivity().findViewById(R.id.makePayment).setEnabled(true);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button_enabled);
        } else {
            getActivity().findViewById(R.id.makePayment).setEnabled(false);
//            getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button);
        }
    }

    private void invalid(EditText editText, Drawable drawable) {
        drawable.setAlpha(100);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        getActivity().findViewById(R.id.makePayment).setEnabled(false);
        getActivity().findViewById(R.id.makePayment).setBackgroundResource(R.drawable.button);
    }

    private void makeInvalid() {
        if (!isCardNumberValid && cardNumber.length() > 0 && !getActivity().findViewById(R.id.cardNumberEditText).isFocused())
            ((EditText) getActivity().findViewById(R.id.cardNumberEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.error_icon), null);
        if (!isCvvValid && cvv.length() > 0 && !getActivity().findViewById(R.id.cvvEditText).isFocused())
            ((EditText) getActivity().findViewById(R.id.cvvEditText)).setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.error_icon), null);
    }

    private void resetHeader() {
        if (getActivity().findViewById(R.id.offerMessageTextView) != null && getActivity().findViewById(R.id.offerMessageTextView).getVisibility() == View.VISIBLE) {
            ((TextView) getActivity().findViewById(R.id.offerMessageTextView)).setText("");
            ((TextView) getActivity().findViewById(R.id.amountTextView)).setGravity(Gravity.CENTER);
            ((TextView) getActivity().findViewById(R.id.amountTextView)).setTextColor(Color.BLACK);
//                        ((TextView) getActivity().findViewById(R.id.amountTextView)).setPaintFlags(((TextView) getActivity().findViewById(R.id.amountTextView)).getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            ((TextView) getActivity().findViewById(R.id.amountTextView)).setPaintFlags(0);
        }
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
        /*HomeActivity.mPayUpoints.setText("Convenience Charge added : "+round((float) (getArguments().getDouble("amt_convenience") / 100) * 100, 2)+" ,PayUPoints charged : "+round((float) (getArguments().getDouble("cashback_amt") / 100) * 100, 2)+" and Discount of: "+round((float) (getArguments().getDouble("amt_discount") / 100) * 100, 2));
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

