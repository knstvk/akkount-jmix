package akkount.security;

import akkount.entity.CategoryAmount;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityui.role.UiFilterRole;
import io.jmix.securityui.role.UiMinimalRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Parents", code = "parents")
public interface ParentsRole extends
        UiMinimalRole, UiFilterRole,
        MasterDataFullAccessRole, OperationsFullAccessRole, ReportsReadRole {

    @SpecificPolicy(resources = "get-balance")
    void balance();

    @ScreenPolicy(screenIds = {"akk_PreferencesScreen", "ShowOperations"}, screenClasses = {})
    @MenuPolicy(menuIds = "akk_PreferencesScreen")
    void screens();


    @EntityPolicy(entityClass = CategoryAmount.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = CategoryAmount.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void categoryReport();
}
