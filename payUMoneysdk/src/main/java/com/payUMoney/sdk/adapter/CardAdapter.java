package com.payUMoney.sdk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.payUMoney.sdk.R;
import com.payUMoney.sdk.entity.Card;

import java.util.List;

/**
 * Created by amit on 10/07/13.
 */
public class CardAdapter extends EMList<Card> {

    private boolean mDeletable = true;

    public CardAdapter(Context context, List<Card> list) {
        super(context, list);
    }

    public CardAdapter(Context context, List<Card> list, boolean deletable) {
        super(context, list);
        mDeletable = deletable;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            convertView = inflateView(R.layout.card);
//        }
//
//        return Tools.makeCardView(getContext(), getItem(position), convertView, mDeletable);
        return convertView;
    }
}
