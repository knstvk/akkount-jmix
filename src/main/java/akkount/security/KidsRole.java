package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.UiFilterRole;
import io.jmix.securityui.role.UiMinimalRole;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

@ResourceRole(name = "Kids", code = "kids")
public interface KidsRole extends
        UiMinimalRole, UiFilterRole,
        MasterDataReadRole, OperationsFullAccessRole {

    @ScreenPolicy(screenIds = "akk_PreferencesScreen")
    @MenuPolicy(menuIds = {"akk_PreferencesScreen"})
    void screens();
}
