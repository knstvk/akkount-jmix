package akkount.service;

import akkount.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nullable;

@Service(PortalService.NAME)
public class PortalServiceBean implements PortalService {

    @Autowired
    private UserDataService userDataService;

    @Override
    @Nullable
    public Account getLastAccount(String opType) {
        return userDataService.loadEntity(opType, Account.class);
    }
}