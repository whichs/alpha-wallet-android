package io.stormbird.wallet.ui.widget.holder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import io.stormbird.token.entity.TicketRange;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.Token;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.TokensService;
import io.stormbird.wallet.ui.widget.OnTicketIdClickListener;

public class GenericNFTHolder extends BinderViewHolder<TicketRange> implements View.OnClickListener
{
    private final AssetDefinitionService assetService;
    private final TokensService tokensService;
    private final View activityView;
    private final OnTicketIdClickListener listener;
    private TicketRange thisData;

    public GenericNFTHolder(OnTicketIdClickListener tokenListener, int resId, ViewGroup parent, AssetDefinitionService service, TokensService tService)
    {
        super(resId, parent);
        activityView = this.itemView;
        assetService = service;
        tokensService = tService;
        listener = tokenListener;
        activityView.setOnClickListener(this);
    }

    @Override
    public void bind(@Nullable TicketRange data, @NonNull Bundle addition)
    {
        thisData = data;
        Token t = tokensService.getToken(data.contractAddress);
        if (data.tokenIds.size() > 0 && t != null && t instanceof Ticket)
        {
            ((Ticket)t).displayTicketHolder(data, activityView, assetService, getContext());
        }
    }

    @Override
    public void onClick(View v)
    {
        listener.onTicketIdClick(v, thisData);
    }
}
