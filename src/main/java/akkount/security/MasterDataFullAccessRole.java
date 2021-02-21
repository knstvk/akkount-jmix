package akkount.security;

import akkount.entity.Account;
import akkount.entity.Category;
import akkount.entity.Currency;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Master Data Full Access", code = "master-data-full-access")
public interface MasterDataFullAccessRole {

    @MenuPolicy(menuIds = {"application"})
    void commonMenu();

    @EntityPolicy(entityClass = Account.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Account.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = {"akk_Account.lookup", "akk_Account.edit"})
    @MenuPolicy(menuIds = {"akk_Account.lookup"})
    void account();

    @EntityPolicy(entityClass = Category.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Category.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = {"akk_Category.lookup", "akk_Category.edit"})
    @MenuPolicy(menuIds = {"akk_Category.lookup"})
    void category();

    @EntityPolicy(entityClass = Currency.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Currency.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = {"akk_Currency.lookup", "akk_Currency.edit"})
    @MenuPolicy(menuIds = {"akk_Currency.lookup"})
    void currency();
}
