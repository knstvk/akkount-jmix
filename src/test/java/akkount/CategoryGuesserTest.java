package akkount;

import akkount.entity.*;
import akkount.test_support.AuthenticatedAsAdmin;
import akkount.view.operation.CategoryGuesser;
import io.jmix.core.DataManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
@ActiveProfiles("test")
public class CategoryGuesserTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    CategoryGuesser categoryGuesser;

    private Category cat1, cat2;
    private Currency usd;
    private Account acc1;
    private CategoryGuessRule rule1, rule2;

    @BeforeEach
    void setUp() {
        cat1 = createCategory("cat1");
        cat2 = createCategory("cat2");

        usd = createCurrency("usd");

        acc1 = createAccount("acc1", usd);

        rule1 = dataManager.create(CategoryGuessRule.class);
        rule1.setCategory(cat1);
        rule1.setCatType(CategoryType.EXPENSE);
        rule1.setAccount(acc1);
        rule1.setPriority(10);
        rule1.setExpression("amount >= 0 && amount < 1000");

        rule2 = dataManager.create(CategoryGuessRule.class);
        rule2.setCategory(cat2);
        rule2.setCatType(CategoryType.EXPENSE);
        rule2.setPriority(5);
        rule2.setExpression("amount == 50");

        dataManager.save(cat1, cat2, acc1, rule1, rule2);
    }

    private Account createAccount(String name, Currency currency) {
        Account account = dataManager.create(Account.class);
        account.setName(name);
        account.setCurrency(currency);
        account.setActive(true);
        return dataManager.save(account);
    }

    private Category createCategory(String name) {
        Category category = dataManager.create(Category.class);
        category.setName(name);
        category.setCatType(CategoryType.EXPENSE);
        return dataManager.save(category);
    }

    private Currency createCurrency(String code) {
        Currency currency = dataManager.create(Currency.class);
        currency.setCode(code);
        return dataManager.save(currency);
    }

    @AfterEach
    void tearDown() {
        dataManager.remove(rule1, rule2, acc1, usd, cat2, cat1);
    }

    @Test
    void test() {
        Category category;

        category = categoryGuesser.guessCategory(acc1, CategoryType.EXPENSE, new BigDecimal("123"));
        assertThat(category).isEqualTo(cat1);

        category = categoryGuesser.guessCategory(acc1, CategoryType.EXPENSE, new BigDecimal("1001"));
        assertThat(category).isNull();

        category = categoryGuesser.guessCategory(null, CategoryType.EXPENSE, new BigDecimal("50"));
        assertThat(category).isEqualTo(cat2);
    }
}