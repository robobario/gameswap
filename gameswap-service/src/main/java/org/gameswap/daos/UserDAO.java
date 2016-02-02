package org.gameswap.daos;

import com.google.common.base.Optional;
import io.dropwizard.hibernate.AbstractDAO;
import org.gameswap.models.User;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * A DAO for managing {@link User} objects.
 */
public class UserDAO extends AbstractDAO<User> {

    /**
     * Creates a new DAO with the given session provider.
     *
     * @param provider a session provider
     */
    public UserDAO(SessionFactory provider) {
        super(provider);
    }


    /**
     * Returns the {@link User} with the given ID.
     *
     * @param id the entity ID
     * @return the entity with the given ID
     */
    public Optional<User> find(long id) {
        return Optional.fromNullable(get(id));
    }


    /**
     * Returns all {@link User} entities.
     *
     * @return the list of entities
     */
    public List<User> findAll() {
        return (List<User>) criteria().list();
    }


    public Optional<User> findByName(String username) {
        List userList = criteria().createCriteria("username", username).list();
        User user = userList.size() == 1 ? (User) userList.get(0) : null;
        return Optional.fromNullable(user);
    }


    /**
     * Saves the given {@link User}.
     *
     * @param entity the entity to save
     * @return the persistent entity
     */
    public User save(User entity) throws HibernateException {
        return persist(entity);
    }


    /**
     * Merges the given {@link User}.
     *
     * @param entity the entity to merge
     * @return the persistent entity
     */
    public User merge(User entity) throws HibernateException {
        return (User) currentSession().merge(entity);
    }


    /**
     * Deletes the given {@link User}.
     *
     * @param entity the entity to delete
     */
    public void delete(User entity) throws HibernateException {
        currentSession().delete(entity);
    }
}
