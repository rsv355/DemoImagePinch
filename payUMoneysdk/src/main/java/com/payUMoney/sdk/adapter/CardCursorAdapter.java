package com.payUMoney.sdk.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.payUMoney.sdk.R;
import com.payUMoney.sdk.database.Cards;
import com.payUMoney.sdk.entity.Card;

public class CardCursorAdapter extends EMCursorAdapter {

    private final Context mContext;
    private boolean mDeletable = false;

    public CardCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
    }

    public CardCursorAdapter(Context context, Cursor c, boolean deletable) {
        super(context, c);
        mContext = context;
        mDeletable = deletable;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(mContext).inflate(null, null);
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        Card card = Cards.getInstance(mContext).getCard(cursor);

        //\Tools.makeCardView(mContext, card, convertView, mDeletable);
    }
}
