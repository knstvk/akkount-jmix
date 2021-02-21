package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "Kids", code = "kids")
public interface KidsRole extends MasterDataReadRole, OperationsFullAccessRole {
}
