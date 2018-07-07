package io.stormbird.wallet.ui.widget.holder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.math.BigInteger;
import java.util.Locale;

import io.stormbird.token.tools.TokenDefinition;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.TicketFunction;
import io.stormbird.wallet.entity.Token;

import io.stormbird.wallet.ui.widget.OnTicketIdClickListener;
import io.stormbird.token.entity.NonFungibleToken;
import io.stormbird.token.entity.TicketRange;

public class BaseTicketHolder extends BinderViewHolder<TicketRange> implements View.OnClickListener
{
    private TicketRange thisData;
    private Ticket ticket;
    private OnTicketIdClickListener onTicketClickListener;
    private final TokenDefinition assetDefinition; //need to cache this locally, unless we cache every string we need in the constructor

    private final TextView name;
    private final TextView count; // word choice: "amount" would imply the amount of money it costs
    private final TextView ticketDate;
    private final TextView ticketTime;
    private final TextView venue;
    private final TextView ticketText;
    private final TextView ticketCat;
    protected final TextView ticketRedeemed;
    private final TextView ticketDetails;
    protected final LinearLayout ticketDetailsLayout;
    protected final LinearLayout ticketLayout;
    protected final ImageView ticketContainer;
    private NonFungibleToken nonFungibleToken;

    public BaseTicketHolder(int resId, ViewGroup parent, TokenDefinition definition, Token ticket) {
        super(resId, parent);
        name = findViewById(R.id.name);
        count = findViewById(R.id.amount);
        venue = findViewById(R.id.venue);
        ticketDate = findViewById(R.id.date);
        ticketTime = findViewById(R.id.time);
        ticketText = findViewById(R.id.tickettext);
        ticketCat = findViewById(R.id.cattext);
        ticketDetails = findViewById(R.id.ticket_details);
        itemView.setOnClickListener(this);
        ticketRedeemed = findViewById(R.id.redeemed);
        ticketDetailsLayout = findViewById(R.id.layout_ticket_details);
        ticketLayout = findViewById(R.id.layout_select_ticket);
        ticketContainer = findViewById(R.id.ticketContainer);
        assetDefinition = definition;
        this.ticket = (Ticket)ticket;
    }

    @Override
    public void bind(@Nullable TicketRange data, @NonNull Bundle addition)
    {
        DateFormat date = android.text.format.DateFormat.getLongDateFormat(getContext());
        date.setTimeZone(TimeZone.getTimeZone("Europe/Moscow")); // TODO: use the timezone defined in XML
        DateFormat time = android.text.format.DateFormat.getTimeFormat(getContext());
        time.setTimeZone(TimeZone.getTimeZone("Europe/Moscow")); // TODO: use the timezone defined in XML
        this.thisData = data;
        name.setText(ticket.tokenInfo.name);

        //1. load the Lua script by passing the contract addr.
        //2. if already present then return it. (need to re-check every minute or so)

        if (data.tokenIds.size() > 0)
        {
            BigInteger firstTokenId = data.tokenIds.get(0);
            String seatCount = String.format(Locale.getDefault(), "x%d", data.tokenIds.size());
            count.setText(seatCount);
            try
            {
                nonFungibleToken = new NonFungibleToken(firstTokenId, assetDefinition);
                String nameStr = nonFungibleToken.getAttribute("category").text;

                String venueStr = nonFungibleToken.getAttribute("venue").text;
                Date startTime = new Date(nonFungibleToken.getAttribute("time").value.longValue()*1000L);
                int cat = nonFungibleToken.getAttribute("category").value.intValue();

                name.setText(nameStr);
                count.setText(seatCount);
                venue.setText(venueStr);
                ticketDate.setText(date.format(startTime));
                ticketTime.setText(time.format(startTime));
                ticketText.setText(
                        nonFungibleToken.getAttribute("countryA").text + "-" +
                                nonFungibleToken.getAttribute("countryB").text
                );
                ticketCat.setText("M" + nonFungibleToken.getAttribute("match").text);
                ticketDetails.setText(
                        nonFungibleToken.getAttribute("locality").name + ": " +
                                nonFungibleToken.getAttribute("locality").text
                );
            } catch (NullPointerException e) {
                /* likely our XML token definition is outdated, just
                 * show raw data here. TODO: only the fields not
                 * understood are displayed as raw
                 */
                name.setText(firstTokenId.toString(16));
            }
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
                String nameStr = nonFungibleToken.getAttribute("category").text;

                try
                {
                    LuaState lua = Token.getLua();
                    if (lua != null && TicketFunction.hasLuaScript())
                    {
                        TicketFunction.setTargetLayout(ticketContainer);
                        lua.getGlobal("tickettrial");
                        lua.pushObjectValue(TicketFunction.class);
                        lua.setGlobal("Tickfun");
                        lua.pushString(nameStr);
                        lua.pushNumber(nonFungibleToken.getAttribute("numero").value.intValue());
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
