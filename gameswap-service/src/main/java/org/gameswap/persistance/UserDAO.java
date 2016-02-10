package org.gameswap.persistance;

import com.google.common.base.Optional;

import org.gameswap.model.User;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import java.util.List;

import io.dropwizard.hibernate.AbstractDAO;

public class UserDAO extends AbstractDAO<User> {

    public UserDAO(SessionFactory provider) {
        super(provider);
    }


    public Optional<User> find(long id) {
        return Optional.fromNullable(get(id));
    }


    public List<User> findAll() {
        return (List<User>) criteria().list();
    }


    public Optional<User> findByName(String username) {
        User foundUser = (User) namedQuery("User.findByName")
                .setParameter("username", username)
                .uniqueResult();
        return Optional.fromNullable(foundUser);
    }


    public User save(User entity) throws HibernateException {
        return persist(entity);
    }


    public User merge(User entity) throws HibernateException {
        return (User) currentSession().merge(entity);
    }


    public void delete(User entity) throws HibernateException {
        currentSession().delete(entity);
    }

    public Optional<User> findById(long id) {
        return Optional.fromNullable(get(id));
    }

    public Optional<User> findByProvider(User.Provider provider, String id) {
        User foundUser = (User) namedQuery(String.format("User.findBy%s", provider.capitalize()))
                .setParameter(provider.getName(), id)
                .uniqueResult();
        return Optional.fromNullable(foundUser);
    }
}
