package akkount.service;

import io.jmix.core.JmixEntity;

import javax.annotation.Nullable;
import java.util.List;

public interface UserDataService {
    String NAME = "akk_UserDataService";

    @Nullable
    <T extends JmixEntity> T loadEntity(String key, Class<T> entityClass);

    <T extends JmixEntity> List<T> loadEntityList(String key, Class<T> entityClass);

    void saveEntity(String key, JmixEntity entity);

    void addEntity(String key, JmixEntity entity);

    void removeEntity(String key, JmixEntity entity);
}