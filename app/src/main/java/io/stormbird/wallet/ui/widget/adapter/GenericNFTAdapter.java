package io.stormbird.wallet.ui.widget.adapter;

import android.util.Log;
import android.view.ViewGroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import io.stormbird.token.entity.NonFungibleToken;
import io.stormbird.token.entity.TicketRange;
import io.stormbird.token.tools.TokenDefinition;
import io.stormbird.wallet.R;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.TicketRangeElement;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.TokensService;
import io.stormbird.wallet.ui.widget.entity.TokenIdSortedItem;
import io.stormbird.wallet.ui.widget.holder.BinderViewHolder;
import io.stormbird.wallet.ui.widget.holder.GenericNFTHolder;
import io.stormbird.wallet.ui.widget.holder.TicketHolder;
import io.stormbird.wallet.ui.widget.holder.TokenDescriptionHolder;

public class GenericNFTAdapter extends TokensAdapter
{
    private static final String TAG = "GNFTA";
    private final AssetDefinitionService assetService;
    private final TokensService tokensService;

    public GenericNFTAdapter(AssetDefinitionService service, TokensService tService)
    {
        super();
        assetService = service;
        tokensService = tService;
        items.clear();
    }

    @Override
    public BinderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BinderViewHolder holder = null;
        switch (viewType) {
            case TicketHolder.VIEW_TYPE: {
                GenericNFTHolder gHolder = new GenericNFTHolder(R.layout.item_ticket, parent, assetService, tokensService);
                holder = gHolder;
            } break;
            case TokenDescriptionHolder.VIEW_TYPE: {
                Log.d(TAG, "Aiya!");
                //holder = new TokenDescriptionHolder(R.layout.item_token_description, parent, ticket, assetService);
            } break;
        }

        return holder;
    }

    public void addTicket(Ticket t)
    {
        //add all of the items in this ticket
        items.beginBatchedUpdates();

        //order is not important in this case at the moment, although for normal tickets we'll simply use the ticket value
        for (BigInteger v : t.balanceArray)
        {
            if (v.compareTo(BigInteger.ZERO) == 0) continue;
            //se what kind of token it is
            NonFungibleToken nft = assetService.getNonFungibleToken(t.getAddress(), v);
            TokenDefinition definition = assetService.getAssetDefinition(t.getAddress());
            TicketRange range = new TicketRange(v, t.getAddress());
            if (definition.hasCustomSpawn())
            {
                int weight = calculateWeight(nft.getAttribute("category").text);
                items.add(new TokenIdSortedItem(range, weight));
            }
            else
            {
                //normal token
                int weight = nft.getAttribute("numero").value.intValue() + (short)nft.getAttribute("category").value.intValue() * 16384;
                items.add(new TokenIdSortedItem(range, 99999 + weight));
            }
        }
        
        items.endBatchedUpdates();
    }

    private void setTicketRange(Ticket t, String ticketIds)
    {
        items.beginBatchedUpdates();
        items.clear();

        /* as why there are 2 for loops immediately following: the
         * sort that's required to get groupings. Splitting it in two
         * makes the algorithm n*2 complexity (plus a log n for sort),
         * rather than a n^2 complexity which you'd need to do it in
         * one go. The code produced is simple enough for anyone
         * looking at it in future. - James Brown
         */
        List<BigInteger> idList = t.stringHexToBigIntegerList(ticketIds);
        List<TicketRangeElement> sortedList = new ArrayList<>();
        for (BigInteger v : idList)
        {
            if (v.compareTo(BigInteger.ZERO) == 0) continue;

            TicketRangeElement e = new TicketRangeElement();
            e.id = v;
            NonFungibleToken nft = assetService.getNonFungibleToken(t.getAddress(), v);
            e.ticketNumber = nft.getAttribute("numero").value.intValue();
            e.category = (short)nft.getAttribute("category").value.intValue();
            e.match = (short)nft.getAttribute("match").value.intValue();
            e.venue = (short)nft.getAttribute("venue").value.intValue();
            sortedList.add(e);
        }
        TicketRangeElement.sortElements(sortedList);

        int currentCat = 0;
        int currentNumber = -1;
        TicketRange currentRange = null;

        for (int i = 0; i < sortedList.size(); i++)
        {
            TicketRangeElement e = sortedList.get(i);
            if (currentRange == null || e.ticketNumber != currentNumber + 1 || e.category != currentCat) //check consecutive seats and zone is still the same, and push final ticket
            {
                currentRange = new TicketRange(e.id, t.getAddress());
                items.add(new TokenIdSortedItem(currentRange, 10 + i));
                currentCat = e.category;
            }
            else
            {
                //update
                currentRange.tokenIds.add(e.id);
            }
            currentNumber = e.ticketNumber;
        }


        items.endBatchedUpdates();
    }

    public void clearItems()
    {
        items.clear();
    }

    /* This one look similar to the one in TicketAdapter, it needs a
     * bit more abstraction to merge - the types produced are
     * different.*/
    private void addRanges(Ticket t)
    {
        TicketRange currentRange = null;
        int currentNumber = -1;

        //first sort the balance array
        List<TicketRangeElement> sortedList = new ArrayList<>();
        for (BigInteger v : t.balanceArray)
        {
            if (v.compareTo(BigInteger.ZERO) == 0) continue;
            TicketRangeElement e = new TicketRangeElement();
            e.id = v;
            NonFungibleToken nft = assetService.getNonFungibleToken(t.getAddress(), v);
            e.ticketNumber = nft.getAttribute("numero").value.intValue();
            e.category = (short)nft.getAttribute("category").value.intValue();
            e.match = (short)nft.getAttribute("match").value.intValue();
            e.venue = (short)nft.getAttribute("venue").value.intValue();
            sortedList.add(e);
        }
        TicketRangeElement.sortElements(sortedList);

        int currentCat = 0;

        for (int i = 0; i < sortedList.size(); i++)
        {
            TicketRangeElement e = sortedList.get(i);
            if (currentRange == null || e.ticketNumber != currentNumber + 1 || e.category != currentCat) //check consecutive seats and zone is still the same, and push final ticket
            {
                currentRange = new TicketRange(e.id, t.getAddress());
                items.add(new TokenIdSortedItem(currentRange, 10 + i));
                currentCat = e.category;
            }
            else
            {
                //update
                currentRange.tokenIds.add(e.id);
            }
            currentNumber = e.ticketNumber;
        }
    }

    private int calculateWeight(String name)
    {
        int weight = 0;
        int i = 4;
        int pos = 0;

        while (i >= 0 && pos < name.length())
        {
            char c = name.charAt(pos++);
            //Character.isIdeographic()
            int w = tokeniseCharacter(c);
            if (w > 0)
            {
                int component = (int)Math.pow(26, i)*w;
                weight += component;
                i--;
            }
        }

        if (weight < 2) weight = 2;

        return weight;
    }

    private int tokeniseCharacter(char c)
    {
        int w = Character.toLowerCase(c) - 'a' + 1;
        if (w > 'z')
        {
            //could be ideographic, in which case we may want to display this first
            //just use a modulus
            w = w % 10;
        }
        else if (w < 0)
        {
            //must be a number
            w = 1 + (c - '0');
        }
        else
        {
            w += 10;
        }

        return w;
    }
}
