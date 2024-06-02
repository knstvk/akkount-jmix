package akkount.view.operation;

import akkount.entity.Account;
import akkount.entity.Category;
import akkount.entity.CategoryGuessRule;
import akkount.entity.CategoryType;
import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CategoryGuesser {

    private static final Logger log = LoggerFactory.getLogger(CategoryGuesser.class);

    private final DataManager dataManager;

    private final ScriptEngineManager scriptEngineManager;

    public CategoryGuesser(DataManager dataManager) {
        this.dataManager = dataManager;
        scriptEngineManager = new ScriptEngineManager();
    }

    @Nullable
    public Category guessCategory(Account account, CategoryType categoryType, BigDecimal amount) {
        List<CategoryGuessRule> rules = dataManager.load(CategoryGuessRule.class)
                .query("e.catType = ?1", categoryType.getId())
                .sort(Sort.by("priority"))
                .list();

        for (CategoryGuessRule rule : rules) {
            if (rule.getAccount() == null || rule.getAccount().equals(account)) {
                if (evaluateExpression(account, amount, rule)) {
                    return rule.getCategory();
                }
            }
        }

        return null;
    }

    private boolean evaluateExpression(Account account, BigDecimal amount, CategoryGuessRule rule) {
        ScriptEngine engine = scriptEngineManager.getEngineByName("Groovy");
        try {
            engine.put("amount", amount);
            Boolean result = (Boolean) engine.eval(rule.getExpression());
            return result != null && result;
        } catch (ScriptException e) {
            log.error("Error evaluating expression: {}", rule.getExpression(), e);
            return false;
        }
    }
}
