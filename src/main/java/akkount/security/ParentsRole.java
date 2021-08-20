package akkount.security;

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

    @ScreenPolicy(screenIds = "akk_PreferencesScreen")
    @MenuPolicy(menuIds = {"akk_PreferencesScreen"})
    void screens();
}
