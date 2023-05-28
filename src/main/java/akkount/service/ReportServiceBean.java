package akkount.service;

import akkount.entity.Category;
import akkount.entity.CategoryAmount;
import akkount.entity.CategoryType;
import io.jmix.core.DataManager;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.*;

@Service(ReportService.NAME)
public class ReportServiceBean implements ReportService {

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected DataManager dataManager;

    @Override
    @Transactional
    public List<CategoryAmount> getTurnoverByCategories(Date fromDate, Date toDate,
                                                        CategoryType categoryType, String currencyCode,
                                                        List<UUID> excludedCategories) {
        List<CategoryAmount> list = new ArrayList<>();

        String catQueryString = "select c from akk_Category c where c.catType = ?1";
        if (!excludedCategories.isEmpty())
            catQueryString += " and c.id not in ?2";

        TypedQuery<Category> catQuery = entityManager.createQuery(catQueryString, Category.class);

        catQuery.setParameter(1, categoryType.getId());
        if (!excludedCategories.isEmpty())
            catQuery.setParameter(2, excludedCategories);

        for (Category category : catQuery.getResultList()) {

            String suffix = categoryType == CategoryType.EXPENSE ? "1" : "2";
            Query amountQuery = entityManager.createQuery(
                    "select sum(o.amount" + suffix + ") from akk_Operation o " +
                            "where @dateAfter(o.opDate, :fromDate) and @dateBefore(o.opDate, :toDate) " +
                            " and o.category.id = :category " +
                            " and o.acc" + suffix + ".currencyCode = :currency");
            amountQuery.setParameter("fromDate", fromDate)
                    .setParameter("toDate", DateUtils.addDays(toDate, 1))
                    .setParameter("category", category.getId())
                    .setParameter("currency", currencyCode);

            BigDecimal amount = (BigDecimal) amountQuery.getSingleResult();
            if (amount == null)
                amount = BigDecimal.ZERO;
            CategoryAmount categoryAmount = dataManager.create(CategoryAmount.class);
            categoryAmount.setCategory(category);
            categoryAmount.setAmount(amount.abs());
            list.add(categoryAmount);
        }

        Collections.sort(list, new Comparator<CategoryAmount>() {
            @Override
            public int compare(CategoryAmount o1, CategoryAmount o2) {
                return o2.getAmount().compareTo(o1.getAmount());
            }
        });
        return list;
    }
}