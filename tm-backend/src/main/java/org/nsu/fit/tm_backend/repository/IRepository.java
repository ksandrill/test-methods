package org.nsu.fit.tm_backend.repository;

import org.nsu.fit.tm_backend.repository.data.AccountTokenPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;

import java.util.List;
import java.util.UUID;

public interface IRepository {
    CustomerPojo createCustomer(CustomerPojo customerPojo);

    void editCustomer(CustomerPojo customerPojo);

    void deleteCustomer(UUID id);

    List<CustomerPojo> getCustomers();

    CustomerPojo getCustomer(UUID id);

    CustomerPojo getCustomerByLogin(String customerLogin);

    AccountTokenPojo createAccountToken(AccountTokenPojo accountTokenPojo);

    void checkAccountToken(String authenticationToken);

    PlanPojo createPlan(PlanPojo plan);

    void deletePlan(UUID id);

    List<PlanPojo> getPlans();

    SubscriptionPojo createSubscription(SubscriptionPojo subscriptionPojo);

    void deleteSubscription(UUID id);

    List<SubscriptionPojo> getSubscriptions();

    List<SubscriptionPojo> getSubscriptions(UUID customerId);
}
