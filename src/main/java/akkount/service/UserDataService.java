package akkount.service;

import javax.annotation.Nullable;
import java.util.List;

public interface UserDataService {
    String NAME = "akk_UserDataService";

    @Nullable
    <T> T loadEntity(String key, Class<T> entityClass);

    <T> List<T> loadEntityList(String key, Class<T> entityClass);

    void saveEntity(String key, Object entity);

    void addEntity(String key, Object entity);

    void removeEntity(String key, Object entity);
}