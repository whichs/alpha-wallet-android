package io.stormbird.wallet.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.stormbird.token.tools.ParseMagicLink;
import io.stormbird.wallet.entity.Token;
import io.stormbird.wallet.entity.Wallet;
import io.stormbird.wallet.interact.FetchTokensInteract;
import io.stormbird.wallet.repository.TokenRepositoryType;

public class TokensService
{
    //private final FetchTokensInteract fetchTokensInteract;
    //private final TokenRepositoryType tokenRepository;

    //stores all the tokens
    private Map<String, Token> tokenMap = new ConcurrentHashMap<>();
    private List<String> terminationList = new ArrayList<>();

//    public TokensService(TokenRepositoryType tokenRepository) {
//        this.tokenRepository = tokenRepository;
//    }

    public TokensService() {

    }

    /**
     * Add the token to the service map and return token in case we use this call in a reactive element
     * @param t
     * @return
     */
    public Token addToken(Token t)
    {
        tokenMap.put(t.getAddress(), t);
        return t;
    }

    public Token getToken(String addr)
    {
        return tokenMap.get(addr);
    }

    public void clearTokens()
    {
        tokenMap.clear();
    }

    public List<Token> getAllTokens()
    {
        return new ArrayList<Token>(tokenMap.values());
    }

    public List<Token> getAllNonNullNonTerminatedTokens()
    {
        List<Token> tokens = new ArrayList<>();
        for (Token t : tokenMap.values())
        {
            if (!t.isTerminated() && t.tokenInfo.name != null) tokens.add(t);
        }

        return tokens;
    }

    public List<Token> getAllTerminated()
    {
        List<Token> tokens = new ArrayList<>();
        for (Token t : tokenMap.values())
        {
            if (t.isTerminated()) tokens.add(t);
        }

        return tokens;
    }

    public List<Token> getAllNullNonterminatedTokens()
    {
        List<Token> tokens = new ArrayList<>();
        for (Token t : tokenMap.values())
        {
            if (!t.isTerminated() && t.tokenInfo.name == null) tokens.add(t);
        }

        return tokens;
    }

    public void scheduleForTermination(String address)
    {
        if (!terminationList.contains(address)) terminationList.add(address);
    }

    public List<String> getTerminationList()
    {
        return terminationList;
    }

    public void clearTerminationList()
    {
        terminationList.clear();
    }

    public void addTokens(Token[] tokens)
    {
        for (Token t : tokens)
        {
            tokenMap.put(t.getAddress(), t);
        }
    }
}
