package org.nsu.fit.tm_backend.service.impl;

import javax.inject.Inject;
import javax.ws.rs.ext.Provider;
import org.jvnet.hk2.annotations.Service;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;

import java.util.List;
import java.util.UUID;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.SubscriptionService;

@Service
public class StatisticServiceImpl {
    @Inject
    private CustomerService customerService;

    @Inject
    private SubscriptionService subscriptionService;

    public StatisticOperationResult calculate(List<UUID> customerIds) {
        StatisticOperationResult result = new StatisticOperationResult();

        result.customerIds = customerIds;
        for (UUID customerId : customerIds) {
            CustomerPojo customer = customerService.getCustomer(customerId);
            result.overallBalance += customer.balance;

            List<SubscriptionPojo> subscriptions = subscriptionService.getSubscriptions(customerId);
            for (SubscriptionPojo subscription : subscriptions) {
                result.overallFee += subscription.planFee;
            }
        }

        return result;
    }

    public static class StatisticOperationResult {
        // Список идентификаторов customer'ов.
        private List<UUID> customerIds;

        // Их общий остаточный баланс.
        public int overallBalance;

        // Их общая сумма денег потраченных покупку различных планов.
        public int overallFee;
    }
}
