package nxt.http;

import nxt.Account;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

import static nxt.http.JSONResponses.INCORRECT_ACCOUNT;
import static nxt.http.JSONResponses.MISSING_ACCOUNT;
import static nxt.http.JSONResponses.UNKNOWN_ACCOUNT;

final class GetAccount extends HttpRequestHandler {

    static final GetAccount instance = new GetAccount();

    private GetAccount() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        String account = req.getParameter("account");
        if (account == null) {
            return MISSING_ACCOUNT;
        }

        Account accountData;
        try {
            accountData = Account.getAccount(Convert.parseUnsignedLong(account));
            if (accountData == null) {
                return UNKNOWN_ACCOUNT;
            }
        } catch (RuntimeException e) {
            return INCORRECT_ACCOUNT;
        }

        JSONObject response = new JSONObject();
        synchronized (accountData) {
            if (accountData.getPublicKey() != null) {
                response.put("publicKey", Convert.convert(accountData.getPublicKey()));
            }

            response.put("balance", accountData.getBalance());
            response.put("effectiveBalance", accountData.getEffectiveBalance() * 100L);

            JSONArray assetBalances = new JSONArray();
            for (Map.Entry<Long, Integer> assetBalanceEntry : accountData.getAssetBalances().entrySet()) {

                JSONObject assetBalance = new JSONObject();
                assetBalance.put("asset", Convert.convert(assetBalanceEntry.getKey()));
                assetBalance.put("balance", assetBalanceEntry.getValue());
                assetBalances.add(assetBalance);

            }
            if (assetBalances.size() > 0) {

                response.put("assetBalances", assetBalances);

            }
        }
        return response;
    }

}
