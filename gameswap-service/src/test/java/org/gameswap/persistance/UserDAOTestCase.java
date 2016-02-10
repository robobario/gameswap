package org.gameswap.persistance;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.gameswap.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

public class UserDAOTestCase {

    private Session session;
    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void setupSessionFactory() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.connection.driver_class", "org.h2.Driver");
        properties.put("hibernate.connection.url", "jdbc:h2:mem:test");
        properties.put("hibernate.current_session_context_class", "managed");
        Configuration hibConfiguration = new Configuration()
                .addAnnotatedClass(User.class).addProperties(properties);
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(hibConfiguration.getProperties())
                .build();
        sessionFactory = hibConfiguration.buildSessionFactory(serviceRegistry);
    }

    @Before
    public void setup() {
        session = sessionFactory.withOptions().openSession();
        ManagedSessionContext.bind(session);
        session.beginTransaction();
    }

    @After
    public void teardown() {
        session.flush();
        session.getTransaction().rollback();
        session.close();
        ManagedSessionContext.unbind(sessionFactory);
    }

    @Test
    public void save() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setUsername("username");
        userDAO.save(entity);
        assertNotNull("entity id should be set by hibernate", entity.getId());
        Optional<User> userOptional = userDAO.find(entity.getId());
        assertTrue("saved user was not found", userOptional.isPresent());
    }

    @Test
    public void findNonExistent() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        assertFalse(userDAO.find(1L).isPresent());
    }

    @Test
    public void findAll() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setUsername("username");
        User entity2 = new User();
        entity2.setUsername("username2");
        userDAO.save(entity);
        userDAO.save(entity2);
        List<User> all = userDAO.findAll();
        assertEquals(2, all.size());
        Set<Long> ids = all.stream().map(User::getId).collect(toSet());
        assertEquals(ImmutableSet.of(entity.getId(), entity2.getId()), ids);
    }

    @Test
    public void findAllNoData() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        List<User> all = userDAO.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    public void delete() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setUsername("username");
        userDAO.save(entity);
        long id = entity.getId();
        assertEquals(1, userDAO.findAll().size());
        userDAO.delete(entity);
        assertTrue(userDAO.findAll().isEmpty());
        assertFalse(userDAO.find(id).isPresent());
    }

    @Test
    public void findByName() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setUsername("username");
        User entity2 = new User();
        entity2.setUsername("username2");
        userDAO.save(entity);
        userDAO.save(entity2);
        Optional<User> username = userDAO.findByName("username");
        assertTrue(username.isPresent());
        assertEquals(username.get(), entity);
    }

    @Test
    public void findByNameNoMatch() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setUsername("username");
        User entity2 = new User();
        entity2.setUsername("username2");
        userDAO.save(entity);
        userDAO.save(entity2);
        Optional<User> username = userDAO.findByName("badname");
        assertFalse(username.isPresent());
    }

    @Test
    public void findByProvider() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setProviderId(User.Provider.GOOGLE, "goog");
        User entity2 = new User();
        entity2.setProviderId(User.Provider.GOOGLE, "goog2");
        userDAO.save(entity);
        userDAO.save(entity2);
        Optional<User> username = userDAO.findByProvider(User.Provider.GOOGLE, "goog");
        assertTrue(username.isPresent());
        assertEquals(username.get(), entity);
    }

    @Test
    public void findByProviderNoMatch() {
        UserDAO userDAO = new UserDAO(sessionFactory);
        User entity = new User();
        entity.setProviderId(User.Provider.GOOGLE, "goog");
        User entity2 = new User();
        entity2.setProviderId(User.Provider.GOOGLE, "goog2");
        userDAO.save(entity);
        userDAO.save(entity2);
        Optional<User> username = userDAO.findByProvider(User.Provider.GOOGLE, "badid");
        assertFalse(username.isPresent());
    }

}