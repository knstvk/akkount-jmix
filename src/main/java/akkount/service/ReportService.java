package akkount.service;

import akkount.entity.CategoryAmount;
import akkount.entity.CategoryType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReportService {
    String NAME = "akk_ReportService";

    List<CategoryAmount> getTurnoverByCategories(LocalDate fromDate, LocalDate toDate,
                                                 CategoryType categoryType, String currencyCode,
                                                 List<UUID> excludedCategories);
}