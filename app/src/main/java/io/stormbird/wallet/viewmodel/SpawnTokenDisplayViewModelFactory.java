package io.stormbird.wallet.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import io.stormbird.wallet.interact.FetchTokensInteract;
import io.stormbird.wallet.router.HomeRouter;
import io.stormbird.wallet.router.MyAddressRouter;
import io.stormbird.wallet.router.TransferTicketRouter;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.service.TokensService;

public class SpawnTokenDisplayViewModelFactory implements ViewModelProvider.Factory
{
    private final FetchTokensInteract fetchTokensInteract;
    private final TransferTicketRouter transferTicketRouter;
    private final MyAddressRouter myAddressRouter;
    private final AssetDefinitionService assetDefinitionService;
    private final HomeRouter homeRouter;
    private final TokensService tokensService;

    public SpawnTokenDisplayViewModelFactory(
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

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SpawnTokenDisplayViewModel(fetchTokensInteract, transferTicketRouter, homeRouter, myAddressRouter, assetDefinitionService, tokensService);
    }
}
