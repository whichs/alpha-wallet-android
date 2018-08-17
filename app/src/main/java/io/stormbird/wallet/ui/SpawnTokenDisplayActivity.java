package io.stormbird.wallet.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.stormbird.wallet.R;
import dagger.android.AndroidInjection;
import io.stormbird.token.entity.TicketRange;
import io.stormbird.wallet.entity.FinishReceiver;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.Token;
import io.stormbird.wallet.entity.TokenInfo;
import io.stormbird.wallet.router.HomeRouter;
import io.stormbird.wallet.ui.widget.adapter.GenericNFTAdapter;
import io.stormbird.wallet.ui.widget.adapter.TicketAdapter;
import io.stormbird.wallet.viewmodel.SpawnTokenDisplayViewModel;
import io.stormbird.wallet.viewmodel.SpawnTokenDisplayViewModelFactory;
import io.stormbird.wallet.widget.ProgressView;
import io.stormbird.wallet.widget.SystemView;

import static io.stormbird.wallet.C.EXTRA_ADDRESS;
import static io.stormbird.wallet.C.IMPORT_STRING;
import static io.stormbird.wallet.C.Key.TICKET;

public class SpawnTokenDisplayActivity extends BaseActivity implements View.OnClickListener
{
    @Inject
    protected SpawnTokenDisplayViewModelFactory spawnTokenDisplayViewModelFactory;
    private SpawnTokenDisplayViewModel viewModel;
    private SystemView systemView;
    private ProgressView progressView;
    private RecyclerView list;

    private FinishReceiver finishReceiver;

    private ArrayList<String> contractAddresses;
    private String remoteAddress;
    private String importOrder;
    private GenericNFTAdapter adapter;

    private List<Ticket> ticketList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        AndroidInjection.inject(this);

        contractAddresses = getIntent().getStringArrayListExtra(TICKET);
        remoteAddress = getIntent().getStringExtra(EXTRA_ADDRESS);
        importOrder =  getIntent().getStringExtra(IMPORT_STRING);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_asset_display);
        toolbar();

        setTitle(getString(R.string.title_show_tickets));
        //TokenInfo info = ticket.tokenInfo;

        systemView = findViewById(R.id.system_view);
        systemView.hide();
        progressView = findViewById(R.id.progress_view);
        progressView.hide();

        list = findViewById(R.id.listTickets);

        viewModel = ViewModelProviders.of(this, spawnTokenDisplayViewModelFactory)
                .get(SpawnTokenDisplayViewModel.class);

        viewModel.queueProgress().observe(this, progressView::updateProgress);
        viewModel.pushToast().observe(this, this::displayToast);
        viewModel.ticket().observe(this, this::onTokenUpdate);

        adapter = new GenericNFTAdapter(this::onTicketIdClick, viewModel.getAssetDefinitionService(), viewModel.getTokensService());
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        list.setHapticFeedbackEnabled(true);

        findViewById(R.id.button_use).setOnClickListener(this);
        findViewById(R.id.button_sell).setOnClickListener(this);
        findViewById(R.id.button_transfer).setOnClickListener(this);

        Button use = findViewById(R.id.button_use);
        Button sell = findViewById(R.id.button_sell);
        Button transfer = findViewById(R.id.button_transfer);

        use.setText("Claim");
        sell.setText("Place");
        transfer.setText("Cancel");

        finishReceiver = new FinishReceiver(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        viewModel.prepare(contractAddresses, remoteAddress);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(finishReceiver);
    }

    private void onTokenUpdate(Token t)
    {
        if (t != null && t instanceof Ticket && !ticketList.contains(t))
        {
            ticketList.add((Ticket)t);
            //add these tokens to the view
            adapter.addTicket((Ticket) t);
            list.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_qr, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                new HomeRouter().open(this, true);
                break;
            case R.id.action_qr:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        new HomeRouter().open(this, true);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_use:
            {
                //this is now claim
                //pick up the bug
            }
            break;
            case R.id.button_sell:
            {
                //this is now place - TODO: go to token view and switch on filter for spawn custom tokens
            }
            break;
            case R.id.button_transfer:
            {
                new HomeRouter().open(this, true);
                finish();
            }
            break;
        }
    }

    private void onTicketIdClick(View view, TicketRange index) {
        //on clicking the ticket simply take user to import(claim) page
        Ticket t = null;
        for (Ticket ticket : ticketList)
        {
            if (ticket.getAddress().equals(index.contractAddress))
            {
                t = ticket;
                break;
            }
        }

        if (t != null)
        {
            //get index we clicked on
            BigInteger ourToken = index.tokenIds.get(0);
            String idListStr = Numeric.toHexStringNoPrefix(ourToken);// t.intArrayToString(ourToken, false); //list of B32 ID's

            viewModel.openTransferState(this, t, idListStr, importOrder);
        }
    }
}
