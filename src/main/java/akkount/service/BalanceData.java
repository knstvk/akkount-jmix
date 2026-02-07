package akkount.service;

import akkount.entity.Account;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BalanceData {

    public final List<AccountBalance> totals;
    public final List<AccountBalance> accounts;
    @Nullable
    public final AccountBalance baseTotal;

    public static class AccountBalance implements Serializable {
        public final String name;
        public final String description;
        public final String currency;
        public final BigDecimal amount;

        public AccountBalance(@Nullable String name, @Nullable String description, String currency, BigDecimal amount) {
            this.name = name;
            this.description = description;
            this.currency = currency;
            this.amount = amount;
        }
    }

    public BalanceData(Map<Account, BigDecimal> balanceByAccount, @Nullable AccountBalance baseTotal) {
        accounts = new ArrayList<>();
        Map<String, BigDecimal> balanceByCurrency = new TreeMap<>();

        for (Map.Entry<Account, BigDecimal> entry : balanceByAccount.entrySet()) {
            Account account = entry.getKey();
            accounts.add(new AccountBalance(account.getName(), account.getDescription(), account.getCurrencyCode(), entry.getValue()));

            BigDecimal val = balanceByCurrency.computeIfAbsent(account.getCurrencyCode(), s -> BigDecimal.ZERO);
            balanceByCurrency.put(account.getCurrencyCode(), val.add(entry.getValue()));
        }

        totals = balanceByCurrency.entrySet().stream()
                .map(entry -> new AccountBalance(null, null, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        this.baseTotal = baseTotal;
    }
}
