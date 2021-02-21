package akkount.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.security.role.annotation.SpecificPolicy;

@ResourceRole(name = "Parents", code = "parents")
public interface ParentsRole extends MasterDataFullAccessRole, OperationsFullAccessRole, ReportsReadRole {

    @SpecificPolicy(resources = {"get-balance"})
    void balance();
}
