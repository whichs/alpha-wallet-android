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

public class GenericNFTHolder extends BinderViewHolder<TicketRange>
{
    private final AssetDefinitionService assetService;
    private final TokensService tokensService;
    private final View activityView;

    public GenericNFTHolder(int resId, ViewGroup parent, AssetDefinitionService service, TokensService tService)
    {
        super(resId, parent);
        activityView = this.itemView;
        assetService = service;
        tokensService = tService;
    }

    @Override
    public void bind(@Nullable TicketRange data, @NonNull Bundle addition)
    {
        Token t = tokensService.getToken(data.contractAddress);
        if (data.tokenIds.size() > 0 && t != null && t instanceof Ticket)
        {
            ((Ticket)t).displayTicketHolder(data, activityView, assetService, getContext());
        }
    }
}
