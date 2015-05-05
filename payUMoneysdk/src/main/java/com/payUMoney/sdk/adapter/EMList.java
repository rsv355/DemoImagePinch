package com.payUMoney.sdk.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;

import com.payUMoney.sdk.Constants;
import com.payUMoney.sdk.entity.Entity;

import java.util.List;

/**
 * {@link android.widget.Adapter} for {@link Entity} {@link java.util.List}s
 */
public abstract class EMList<A extends Entity> extends BaseAdapter {
    private final LayoutInflater inflater;
    private final List<A> entities;
    private final Context mContext;

    EMList(Context context, List<A> list) {
        entities = list;
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    Context getContext() {
        return mContext;
    }

    /**
     * Get {@link android.view.View} from view id using the {@link android.view.LayoutInflater}
     */
    View inflateView(int viewId) {
        return inflater.inflate(viewId, null);
    }

    @Override
    public long getItemId(int position) {
        return entities.get(position).getId();
    }

    @Override
    public int getCount() {
        return entities.size();
    }

    @Override
    public A getItem(int position) {
        A entity = null;
        try {
            entity = entities.get(position);
        } catch (Throwable e) {
            if (Constants.DEBUG) {
                Log.w(Constants.TAG, "EMListAdapter.getItem: tried to fetch inexistant item at position " + position);
            }
        }
        return entity;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * replaces the current {@link java.util.List} of {@link Entity}s by a new one
     */
    public void replaceAll(List<A> entities) {
        this.entities.clear();
        for (A entity : entities) {
            this.entities.add(entity);
        }
        notifyDataSetChanged();
    }
}
