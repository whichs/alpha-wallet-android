package io.stormbird.wallet.router;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import io.stormbird.wallet.ui.SpawnTokenDisplayActivity;

import static io.stormbird.wallet.C.EXTRA_ADDRESS;
import static io.stormbird.wallet.C.Key.TICKET;

public class SpawnTokenDisplayRouter
{
    public void open(Context context, ArrayList<String> tokenAddresses, String remoteAddress)
    {
        Intent intent = new Intent(context, SpawnTokenDisplayActivity.class);
        intent.putStringArrayListExtra(TICKET, tokenAddresses);
        intent.putExtra(EXTRA_ADDRESS, remoteAddress);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
