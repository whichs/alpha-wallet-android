package io.stormbird.wallet.ui.widget.holder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.keplerproject.luajava.LuaState;

import io.stormbird.token.entity.NonFungibleToken;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.TicketFunction;
import io.stormbird.wallet.entity.Token;

import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.ui.widget.OnTicketIdClickListener;
import io.stormbird.token.entity.TicketRange;

public class BaseTicketHolder extends BinderViewHolder<TicketRange> implements View.OnClickListener
{
    private TicketRange thisData;
    private Ticket ticket;
    private OnTicketIdClickListener onTicketClickListener;
    private final AssetDefinitionService assetService; //need to cache this locally, unless we cache every string we need in the constructor
    private final View activityView;

    private final TextView name;
    protected final TextView ticketRedeemed;
    protected final LinearLayout ticketDetailsLayout;
    protected final LinearLayout ticketLayout;
    protected final ImageView ticketContainer;

    public BaseTicketHolder(int resId, ViewGroup parent, Token ticket, AssetDefinitionService service)
    {
        super(resId, parent);
        name = findViewById(R.id.name);

        activityView = this.itemView;

        itemView.setOnClickListener(this);
        ticketRedeemed = findViewById(R.id.redeemed);
        ticketDetailsLayout = findViewById(R.id.layout_ticket_details);
        ticketLayout = findViewById(R.id.layout_select_ticket);
        assetService = service;
        ticketContainer = findViewById(R.id.ticketContainer);
        this.ticket = (Ticket) ticket;
    }

    @Override
    public void bind(@Nullable TicketRange data, @NonNull Bundle addition)
    {
        this.thisData = data;
        if (data.tokenIds.size() > 0)
        {
            ticket.displayTicketHolder(data, activityView, assetService, getContext());
        }

        final ViewTreeObserver.OnPreDrawListener drawListener = new ViewTreeObserver.OnPreDrawListener()
        {
            @Override
            public boolean onPreDraw()
            {
                ticketLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                int width = ticketLayout.getWidth();
                int height = ticketLayout.getHeight();
                TicketFunction.setXY(width, height);
                String nameStr = ticket.getTokenName(assetService);

                try
                {
                    LuaState lua = Token.getLua();
                    if (lua != null && TicketFunction.hasLuaScript())
                    {
                        NonFungibleToken nonFungibleToken = assetService.getNonFungibleToken(thisData.tokenIds.get(0));
                        TicketFunction.setTargetLayout(ticketContainer);
                        lua.getGlobal("ticketElement");
                        lua.pushObjectValue(TicketFunction.class);
                        lua.setGlobal("Tickfun");
                        lua.pushString(ticket.getTokenTitle(nonFungibleToken));
                        lua.pushNumber(nonFungibleToken.getAttribute("numero").value.intValue());// ticket.getTokenTitle(nonFungibleToken));// assetService.getNonFungibleToken(thisData.tokenIds.get(0)).getAttribute("numero").value.intValue());
                        lua.call(2, 1);
                        nameStr = lua.toString(-1);
                        name.setText(nameStr);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return true;
            }
        };
        ticketLayout.getViewTreeObserver().addOnPreDrawListener(drawListener);
    }

    private void setXY()
    {
        ViewGroup.LayoutParams params = ticketLayout.getLayoutParams();

        TicketFunction.setXY(params.width, params.height);
    }

    @Override
    public void onClick(View v) {
        if (onTicketClickListener != null) {
            onTicketClickListener.onTicketIdClick(v, thisData);
        }
    }

    public void setOnTokenClickListener(OnTicketIdClickListener onTokenClickListener) {
        this.onTicketClickListener = onTokenClickListener;
    }
}
