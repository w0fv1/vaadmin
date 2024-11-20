package dev.w0fv1.vaadmin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

@Component
@Repository
public class GenericRepository {
    @PersistenceContext
    private EntityManager entityManager;

    private final TransactionTemplate transactionTemplate;

    public GenericRepository(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Nullable
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        return transactionTemplate.execute(action);
    }

    @Transactional
    public <T> T save(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @Transactional
    public <T> void delete(T entity) {
        entityManager.remove(entity);
        entityManager.flush();
    }


    @Transactional
    public <T> Boolean exist(Long id, Class<T> clazz) {
        return entityManager.find(clazz, id) != null;
    }

    @Transactional
    public <T, ID> T find(ID id, Class<T> type) {
        return entityManager.find(type, id);
    }

    @Transactional
    public <T,ID> List<T> findAll(List<ID> ids, Class<T> type) {
        if (ids == null || ids.isEmpty()) {
            return List.of(); // 返回空列表
        }

        // 构建 JPQL 查询
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.id IN :ids";
        TypedQuery<T> query = entityManager.createQuery(jpql, type);
        query.setParameter("ids", ids);
        List<T> resultList = query.getResultList();
        if (resultList == null) {
            return List.of();
        }
        return resultList;
    }


    @Transactional
    public <T> T find(String uuid, Class<T> type) {
        String query = "FROM " + type.getSimpleName() + " t WHERE t.uuid = :uuid";
        TypedQuery<T> typedQuery = entityManager.createQuery(query, type);
        typedQuery.setParameter("uuid", uuid);

        if (typedQuery.getResultList().isEmpty()) {
            return null;
        }
        return typedQuery.getSingleResult();
    }

    public Long findMaxIdByLtLastTime(Class<?> type, OffsetDateTime lastTime) {
        return findMaxIdByLtLastTime(type, lastTime, 0L);
    }

//    @Transactional
//    public <T extends BaseEntity<ID>, ID> T updateStatusById(ID id, Class<T> clazz, Status newStatus) {
//        // 根据 ID 和类型查找实体
//        T entity = entityManager.find(clazz, id);
//        if (entity != null) {
//            // 更新状态
//            entity.setStatus(newStatus);
//            // 合并实体，保存更改
//            entityManager.merge(entity);
//        }
//        return entity;
//    }

    public Long findMaxIdByLtLastTime(Class<?> type, OffsetDateTime lastTime, Long coalesceValue) {
        Query query = entityManager.createQuery("SELECT max(a.id) FROM " + type.getSimpleName() + " a WHERE a.createdTime < :lastTime");
        query.setParameter("lastTime", lastTime);
//        query.setParameter("coalesceValue", coalesceValue);
        Long result = (Long) query.getSingleResult();
        if (isNull(result)) {
            result = coalesceValue;
        }
        return result;
    }

    /**
     * 获取表的总记录数，应用条件筛选
     */
    @Transactional
    public <T> Long getTotalSize(Class<T> type, PredicateManager<T> predicateManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(type);
        List<Predicate> predicates = predicateManager.buildPredicates(cb, root);

        // 设置查询返回类型为 Long，选择计数
        cq.select(cb.count(root));

        // 应用传入的 Predicate 条件
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        TypedQuery<Long> typedQuery = entityManager.createQuery(cq);
        return typedQuery.getSingleResult();
    }


    @Transactional
    public <T> List<T> getPage(Class<T> type, int page, int size, PredicateManager<T> predicateManager) {


        // 使用 CriteriaBuilder 构造查询
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> root = cq.from(type);
        List<Predicate> predicates = predicateManager.buildPredicates(cb, root);

        // 应用传入的 Predicate 条件
        cq.where(predicates.toArray(new Predicate[0]));

        // 创建查询并设置分页
        TypedQuery<T> query = entityManager.createQuery(cq);
        query.setFirstResult(page * size); // 设置起始位置
        query.setMaxResults(size); // 设置每页大小

        return query.getResultList();
    }

    public interface PredicateBuilder<T> {
        void build(CriteriaBuilder cb, Root<T> root, List<Predicate> predicates);
    }

    public static class PredicateManager<T> {
        private final Map<String, PredicateBuilder<T>> predicateBuilders = new HashMap<>();

        // Add or replace a predicate builder with a key
        public void putPredicate(String key, PredicateBuilder<T> builder) {
            predicateBuilders.put(key, builder);
        }

        // Remove a predicate builder by key
        public void removePredicate(String key) {
            predicateBuilders.remove(key);
        }

        // Clear all predicates
        public void clearPredicates() {
            predicateBuilders.clear();
        }

        // Build predicates using the CriteriaBuilder and Root
        public List<Predicate> buildPredicates(CriteriaBuilder cb, Root<T> root) {
            List<Predicate> predicates = new ArrayList<>();
            for (PredicateBuilder<T> builder : predicateBuilders.values()) {
                builder.build(cb, root, predicates);
            }
            return predicates;
        }
    }


}
