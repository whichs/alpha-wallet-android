package io.stormbird.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.stormbird.token.entity.TicketRange;
import io.stormbird.wallet.entity.NetworkInfo;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.Token;
import io.stormbird.wallet.entity.Wallet;
import io.stormbird.wallet.interact.FetchTokensInteract;
import io.stormbird.wallet.router.HomeRouter;
import io.stormbird.wallet.router.MyAddressRouter;
import io.stormbird.wallet.router.TransferTicketRouter;
import io.stormbird.wallet.service.AssetDefinitionService;

public class SpawnTokenDisplayViewModel extends BaseViewModel
{
    private static final long CHECK_BALANCE_INTERVAL = 10;
    private static final String TAG = "STVM";
    private final FetchTokensInteract fetchTokensInteract;
    private final TransferTicketRouter transferTicketRouter;
    private final MyAddressRouter myAddressRouter;
    private final AssetDefinitionService assetDefinitionService;

    private final HomeRouter homeRouter;
    private Token refreshToken;

    private Map<String, Token> tokenMap = new ConcurrentHashMap<>();

    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Token> ticket = new MutableLiveData<>();

    @Nullable
    private Disposable getBalanceDisposable;

    SpawnTokenDisplayViewModel(
            FetchTokensInteract fetchTokensInteract,
            TransferTicketRouter transferTicketRouter,
            HomeRouter homeRouter,
            MyAddressRouter myAddressRouter,
            AssetDefinitionService assetDefinitionService) {
        this.fetchTokensInteract = fetchTokensInteract;
        this.transferTicketRouter = transferTicketRouter;
        this.homeRouter = homeRouter;
        this.myAddressRouter = myAddressRouter;
        this.assetDefinitionService = assetDefinitionService;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (getBalanceDisposable != null) {
            getBalanceDisposable.dispose();
        }
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }
    public LiveData<Token> ticket() {
        return ticket;
    }

    public void fetchCurrentTicketBalance() {
        if (getBalanceDisposable != null) getBalanceDisposable.dispose();
        getBalanceDisposable = Observable.interval(CHECK_BALANCE_INTERVAL, CHECK_BALANCE_INTERVAL, TimeUnit.SECONDS)
                .doOnNext(l -> fetchTokensInteract
                        .fetchSingle(defaultWallet.getValue(), ticket().getValue())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onToken, this::onError, this::finishTokenFetch)).subscribe();
    }

    private void finishTokenFetch()
    {
        Log.d(TAG, "refreshToken: " + refreshToken.tokenInfo.name );
        ticket.postValue(refreshToken);
    }

    public void prepare(ArrayList<String> contracts, String checkAddress)
    {
        tokenMap.clear();
        //fetch contracts
        //first get the tokens
        //can't scan existing database (yet!) so we need to fetch the tokens from live
        disposable = Observable.fromArray(contracts)
                .flatMapIterable(address -> address)
                .map(fetchTokensInteract.up


    }

    public AssetDefinitionService getAssetDefinitionService()
    {
        return assetDefinitionService;
    }

    private void onToken(Token t)
    {
        refreshToken = t;
    }

    public void showTransferToken(Context context, Ticket ticket) {
        if (getBalanceDisposable != null) {
            getBalanceDisposable.dispose();
        }
        transferTicketRouter.open(context, ticket);
    }

    public void showContractInfo(Context ctx, String address)
    {
        myAddressRouter.open(ctx, address);
    }

    public void showTransferToken(Context context, Ticket ticket, TicketRange range) {
//        transferTicketRouter.openRange(context, ticket, range);
    }

    private void onDefaultWallet(Wallet wallet) {
        //TODO: switch on 'use' button
        progress.postValue(false);
        defaultWallet.setValue(wallet);
        fetchCurrentTicketBalance();
    }

    public void showHome(Context context, boolean isClearStack) {
        homeRouter.open(context, isClearStack);
    }

}
