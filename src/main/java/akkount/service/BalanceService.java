package akkount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BalanceService {
    String NAME = "akk_BalanceService";

    BigDecimal getBalance(UUID accountId, LocalDate date);

    void recalculateBalance(UUID accountId);

    List<BalanceData> getBalanceData(LocalDate date);

}