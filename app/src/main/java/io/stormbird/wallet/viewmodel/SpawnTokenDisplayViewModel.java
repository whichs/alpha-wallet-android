package io.stormbird.wallet.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.stormbird.token.entity.TicketRange;
import io.stormbird.wallet.entity.NetworkInfo;
import io.stormbird.wallet.entity.Ticket;
import io.stormbird.wallet.entity.Token;
import io.stormbird.wallet.entity.TokenFactory;
import io.stormbird.wallet.entity.TokenInfo;
import io.stormbird.wallet.entity.Wallet;
import io.stormbird.wallet.interact.FetchTokensInteract;
import io.stormbird.wallet.router.HomeRouter;
import io.stormbird.wallet.router.MyAddressRouter;
import io.stormbird.wallet.router.TransferTicketRouter;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.TokensService;

public class SpawnTokenDisplayViewModel extends BaseViewModel
{
    private static final String TAG = "STVM";
    private final FetchTokensInteract fetchTokensInteract;
    private final TransferTicketRouter transferTicketRouter;
    private final MyAddressRouter myAddressRouter;
    private final AssetDefinitionService assetDefinitionService;
    private final TokensService tokensService;

    private final HomeRouter homeRouter;

    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<Token> ticket = new MutableLiveData<>();

    SpawnTokenDisplayViewModel(
            FetchTokensInteract fetchTokensInteract,
            TransferTicketRouter transferTicketRouter,
            HomeRouter homeRouter,
            MyAddressRouter myAddressRouter,
            AssetDefinitionService assetDefinitionService,
            TokensService tokensService) {
        this.fetchTokensInteract = fetchTokensInteract;
        this.transferTicketRouter = transferTicketRouter;
        this.homeRouter = homeRouter;
        this.myAddressRouter = myAddressRouter;
        this.assetDefinitionService = assetDefinitionService;
        this.tokensService = tokensService;
    }

    @Override
    protected void onCleared()
    {
        super.onCleared();
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }
    public LiveData<Token> ticket() {
        return ticket;
    }

    public void prepare(ArrayList<String> contracts, String checkAddress)
    {
        final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(3);
        final Wallet wallet = new Wallet(checkAddress);
        //fetch contracts
        //first get the tokens
        //can't scan existing database (yet!) so we need to fetch the tokens from live

        disposable = Observable.fromArray(contracts)
                .flatMapIterable(address -> address)
                .flatMap(fetchTokensInteract::getTokenInfo)
                .flatMap(this::createNewToken)// now we have token info we need to get the balance here
                .flatMap(token -> fetchTokensInteract.updateBalance(wallet, token))
                .subscribeOn(Schedulers.from(threadPoolExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::receiveTokenBalance, this::onError);

    }

    public TokensService getTokensService()
    {
        return tokensService;
    }

    private void receiveTokenBalance(Token token)
    {
        //hopefully we got the token balance at this address
        ticket.postValue(token);
    }

    private Observable<Token> createNewToken(TokenInfo tokenInfo)
    {
        return Observable.fromCallable(() -> {
            TokenFactory tf = new TokenFactory();
            return tf.createToken(tokenInfo);
        });
    }

    public AssetDefinitionService getAssetDefinitionService()
    {
        return assetDefinitionService;
    }

    public void showHome(Context context, boolean isClearStack) {
        homeRouter.open(context, isClearStack);
    }

}
