package org.nsu.fit.tm_backend;

import org.slf4j.LoggerFactory;
import org.nsu.fit.tm_backend.repository.impl.MemoryRepository;
import org.nsu.fit.tm_backend.manager.auth.AuthenticationTokenManager;
import org.nsu.fit.tm_backend.manager.CustomerManager;
import org.nsu.fit.tm_backend.manager.PlanManager;
import org.nsu.fit.tm_backend.manager.SubscriptionManager;

public class MainFactory {
    private static volatile MainFactory instance;

    private final AuthenticationTokenManager authenticationTokenManager;
    private final CustomerManager customerManager;
    private final PlanManager planManager;
    private final SubscriptionManager subscriptionManager;

    private MainFactory() {
        MemoryRepository dbService = new MemoryRepository(LoggerFactory.getLogger(MemoryRepository.class));

        authenticationTokenManager = new AuthenticationTokenManager(dbService, LoggerFactory.getLogger(AuthenticationTokenManager.class));
        customerManager = new CustomerManager(dbService, LoggerFactory.getLogger(CustomerManager.class));
        planManager = new PlanManager(dbService, LoggerFactory.getLogger(PlanManager.class));
        subscriptionManager = new SubscriptionManager(dbService, LoggerFactory.getLogger(SubscriptionManager.class));
    }

    public static MainFactory getInstance() {
        if (instance != null) {
            return instance;
        }

        synchronized (MainFactory.class) {
            if (instance == null) {
                instance = new MainFactory();
            }
            return instance;
        }
    }

    public AuthenticationTokenManager getAuthenticationTokenManager() {
        return authenticationTokenManager;
    }

    public CustomerManager getCustomerManager() {
        return customerManager;
    }

    public PlanManager getPlanManager() {
        return planManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}
