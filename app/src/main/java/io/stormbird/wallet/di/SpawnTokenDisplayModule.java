package io.stormbird.wallet.di;

import dagger.Module;
import dagger.Provides;
import io.stormbird.wallet.interact.FetchTokensInteract;
import io.stormbird.wallet.repository.TokenRepositoryType;
import io.stormbird.wallet.router.HomeRouter;
import io.stormbird.wallet.router.MyAddressRouter;
import io.stormbird.wallet.router.TransferTicketRouter;
import io.stormbird.wallet.service.AssetDefinitionService;
import io.stormbird.wallet.viewmodel.SpawnTokenDisplayViewModelFactory;

@Module
public class SpawnTokenDisplayModule
{
    @Provides
    SpawnTokenDisplayViewModelFactory spawnTokenDisplayViewModelFactory(
            FetchTokensInteract fetchTokensInteract,
            TransferTicketRouter transferTicketRouter,
            HomeRouter homeRouter,
            MyAddressRouter myAddressRouter,
            AssetDefinitionService assetDefinitionService) {
        return new SpawnTokenDisplayViewModelFactory(
                fetchTokensInteract, transferTicketRouter, homeRouter, myAddressRouter, assetDefinitionService);
    }

    @Provides
    FetchTokensInteract providefetchTokensInteract(
            TokenRepositoryType tokenRepository) {
        return new FetchTokensInteract(tokenRepository);
    }

    @Provides
    TransferTicketRouter provideTransferTicketRouter() {
        return new TransferTicketRouter();
    }

    @Provides
    HomeRouter provideHomeRouter() {
        return new HomeRouter();
    }

    @Provides
    MyAddressRouter provideMyAddressRouter() {
        return new MyAddressRouter();
    }
}
