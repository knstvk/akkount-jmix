package akkount.service;

import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

@Service(UserDataService.NAME)
public class UserDataServiceBean implements UserDataService {

    @Inject
    protected UserDataWorker worker;

    @Override
    @Nullable
    public <T> T loadEntity(String key, Class<T> entityClass) {
        return worker.loadEntity(key, entityClass);
    }

    @Override
    public <T> List<T> loadEntityList(String key, Class<T> entityClass) {
        return worker.loadEntityList(key, entityClass);
    }

    @Override
    public void saveEntity(String key, Object entity) {
        worker.saveEntity(key, entity, false);
    }

    @Override
    public void addEntity(String key, Object entity) {
        worker.saveEntity(key, entity, true);
    }

    @Override
    public void removeEntity(String key, Object entity) {
        worker.removeEntity(key, entity);
    }
}