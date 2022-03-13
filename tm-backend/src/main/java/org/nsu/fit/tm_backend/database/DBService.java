package org.nsu.fit.tm_backend.database;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.slf4j.Logger;
import org.nsu.fit.tm_backend.database.data.AccountTokenPojo;
import org.nsu.fit.tm_backend.database.data.CustomerPojo;
import org.nsu.fit.tm_backend.database.data.PlanPojo;
import org.nsu.fit.tm_backend.database.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.manager.auth.exception.AccessDeniedException;
import org.nsu.fit.tm_backend.shared.JsonMapper;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DBService implements IDBService{
    private final Logger logger;
    private static final Object generalMutex = new Object();

    private final List<AccountTokenPojo> accountTokens;

    public DBService(Logger logger) {
        this.logger = logger;
        this.accountTokens = new ArrayList<>();
    }

    @Override
    public AccountTokenPojo createAccountToken(AccountTokenPojo accountTokenPojo) {
        synchronized (generalMutex) {
            logger.debug(String.format("Method 'createAccountToken' was called with data: \n%s", JsonMapper.toJson(accountTokenPojo, true)));

            accountTokens.add(accountTokenPojo);

            return accountTokenPojo;
        }
    }

    @Override
    public void checkAccountToken(String authenticationToken) {
        synchronized (generalMutex) {
            logger.debug(String.format("Method 'checkAccountToken' was called with data: \n%s", authenticationToken));

            accountTokens.stream()
                    .filter(x -> x.token.equals(authenticationToken))
                    .findFirst()
                    .orElseThrow(() -> new AccessDeniedException(""));
        }
    }

    public CustomerPojo createCustomer(CustomerPojo customer) {
        try (CloseableSession session = new CloseableSession(HibernateUtil.getSessionFactory().openSession())) {
            session.delegate().beginTransaction();

            customer.id = UUID.randomUUID();

            session.delegate().save(customer);
            session.delegate().getTransaction().commit();
        }

        return customer;
    }

    public void editCustomer(CustomerPojo value) {
        try (CloseableSession session = new CloseableSession(HibernateUtil.getSessionFactory().openSession())) {
            session.delegate().beginTransaction();

            CustomerPojo customer = session.delegate().get(CustomerPojo.class, value.id);
            session.delegate().lock(customer, LockMode.OPTIMISTIC_FORCE_INCREMENT);

            customer.balance = value.balance;

            session.delegate().merge(customer);
            session.delegate().getTransaction().commit();
        }
    }

    @Override
    public void deleteCustomer(UUID id) {

    }

    public List<CustomerPojo> getCustomers() {
        try (CloseableSession session = new CloseableSession(HibernateUtil.getSessionFactory().openSession())) {
            session.delegate().beginTransaction();

            Criteria criteria;
            criteria = session.delegate().createCriteria(CustomerPojo.class);
            return criteria.list();
        }
    }

    public CustomerPojo getCustomer(UUID id) {
        try (CloseableSession session = new CloseableSession(HibernateUtil.getSessionFactory().openSession())) {
            session.delegate().beginTransaction();
            return  session.delegate().get(CustomerPojo.class, id);
        }
    }

    public CustomerPojo getCustomerByLogin(String login) {
        try (CloseableSession session = new CloseableSession(HibernateUtil.getSessionFactory().openSession())) {
            session.delegate().beginTransaction();

            Query query = session.delegate().createQuery("from CustomerPojo  where login = :login");
            query.setParameter("login", login);
            return (CustomerPojo) query.getSingleResult();
        }
    }

    public PlanPojo createPlan(PlanPojo plan) {
        return plan;
    }

    @Override
    public void deletePlan(UUID id) {

    }

    @Override
    public List<PlanPojo> getPlans() {
        return Collections.emptyList();
    }

    @Override
    public SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo) {
        return subscriptionPojo;
    }

    @Override
    public void deleteSubscription(UUID id) {

    }

    @Override
    public List<SubscriptionPojo> getSubscriptions() {
        return Collections.emptyList();
    }

    @Override
    public List<SubscriptionPojo> getSubscriptions(UUID customerId) {
        return Collections.emptyList();
    }
}
