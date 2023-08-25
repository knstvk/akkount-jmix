package akkount.service;

import akkount.entity.User;
import akkount.entity.UserData;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component(UserDataWorker.NAME)
public class UserDataWorker {

    public static final String NAME = "akk_UserDataWorker";

    private Log log = LogFactory.getLog(UserDataWorker.class);

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    private Metadata metadata;

    @Nullable
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T loadEntity(String key, Class<T> entityClass) {
        List<String> values = getValues(key);
        if (values.isEmpty())
            return null;

        String value = values.get(0);

        return getEntity(value, entityClass);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> List<T> loadEntityList(String key, Class<T> entityClass) {
        ArrayList<T> result = new ArrayList<>();

        List<String> values = getValues(key);
        if (values.isEmpty())
            return result;

        for (String value : values) {
            T entity = getEntity(value, entityClass);
            if (entity != null) {
                result.add(entity);
            }
        }

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEntity(String key, Object entity, boolean multipleValues) {
        String value = EntityValues.getId(entity).toString();
        String queryString = "select d from akk_UserData d where d.user = ?1 and d.key = ?2";
        if (multipleValues)
            queryString += " and d.value = ?3";

        TypedQuery<UserData> query = entityManager.createQuery(
                queryString, UserData.class);

        query.setParameter(1, currentUserSubstitution.getEffectiveUser());
        query.setParameter(2, key);
        if (multipleValues)
            query.setParameter(3, value);

        List<UserData> resultList = query.getResultList();
        UserData userData;
        if (resultList.isEmpty()) {
            userData = metadata.create(UserData.class);
            userData.setUser(entityManager.getReference(User.class, ((User) currentUserSubstitution.getEffectiveUser()).getId()));
            userData.setKey(key);
            userData.setValue(value);
            entityManager.persist(userData);
        } else {
            userData = resultList.get(0);
            userData.setValue(value);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeEntity(String key, Object entity) {
        String value = EntityValues.getId(entity).toString();
        TypedQuery<UserData> query = entityManager.createQuery(
                "select d from akk_UserData d where d.user = ?1 and d.key = ?2 and d.value = ?3", UserData.class);
        query.setParameter(1, currentUserSubstitution.getEffectiveUser());
        query.setParameter(2, key);
        query.setParameter(3, value);
        List<UserData> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            entityManager.remove(resultList.get(0));
        }
    }

    private <T> T getEntity(String value, Class<T> entityClass) {
        UUID entityId;
        try {
            entityId = UUID.fromString(value);
        } catch (Exception e) {
            log.warn("Invalid entity ID: " + value);
            return null;
        }

        //noinspection unchecked
        return (T) entityManager.find((Class<Entity>) entityClass, entityId);
    }

    private List<String> getValues(String key) {
        TypedQuery<String> query = entityManager.createQuery(
                "select d.value from akk_UserData d where d.user = ?1 and d.key = ?2", String.class);
        query.setParameter(1, currentUserSubstitution.getEffectiveUser());
        query.setParameter(2, key);
        return query.getResultList();
    }
}
