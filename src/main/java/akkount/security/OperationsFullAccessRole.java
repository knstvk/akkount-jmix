package akkount.security;

import akkount.entity.Balance;
import akkount.entity.Operation;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Operations Full Access", code = "operations-full-access")
public interface OperationsFullAccessRole {

    @MenuPolicy(menuIds = "application")
    void commonMenu();

    @EntityPolicy(entityClass = Operation.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Operation.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ScreenPolicy(screenIds = {"akk_Operation.lookup", "akk_Operation.edit"}, screenClasses = {})
    @MenuPolicy(menuIds = {"akk_Operation.lookup"})
    void operation();

    @EntityAttributePolicy(entityClass = Balance.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Balance.class, actions = EntityPolicyAction.ALL)
    void balance();
}
