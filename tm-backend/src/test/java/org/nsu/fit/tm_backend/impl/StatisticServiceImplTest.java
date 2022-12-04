package org.nsu.fit.tm_backend.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.data.StatisticBO;
import org.nsu.fit.tm_backend.service.data.StatisticPerCustomerBO;
import org.nsu.fit.tm_backend.service.impl.StatisticServiceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

// Лабораторная 2: покрыть unit тестами класс StatisticServiceImpl на 100%.
// Чтобы протестировать метод calculate() используйте Mockito.spy(statisticService) и переопределите метод
// calculate(UUID customerId) чтобы использовать стратегию "разделяй и властвуй".
@ExtendWith(MockitoExtension.class)
public class StatisticServiceImplTest {
    @Mock
    private CustomerService customerService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private StatisticServiceImpl statisticService;

    @Test
    void testCalculate_CustomerNotFound() {
        StatisticPerCustomerBO statistics = statisticService.calculate(null);
        assertThat(statistics).isEqualTo(null);
    }

    @Test
    void testCalculate_CustomerFound() {
        CustomerPojo customer = new CustomerPojo();
        customer.id = UUID.randomUUID();
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 100;

        when(customerService.lookupCustomer(customer.id)).thenReturn(customer);

        SubscriptionPojo subscription = new SubscriptionPojo();
        subscription.customerId = customer.id;
        UUID subscriptionUUID = UUID.randomUUID();
        subscription.id = subscriptionUUID;
        subscription.planFee = 500;
        subscription.planDetails = "Test details";
        subscription.planName = "Test plan name";
        ArrayList<SubscriptionPojo> subscriptionPojos = new ArrayList<>();
        subscriptionPojos.add(subscription);

        when(subscriptionService.getSubscriptions(customer.id)).thenReturn(subscriptionPojos);

        StatisticPerCustomerBO statistics = statisticService.calculate(customer.id);

        StatisticPerCustomerBO expectedStatistics = new StatisticPerCustomerBO();
        expectedStatistics.setOverallBalance(customer.balance);
        expectedStatistics.setOverallFee(500);
        HashSet<UUID> expectedIds = new HashSet<>();
        expectedIds.add(subscriptionUUID);
        expectedStatistics.setSubscriptionIds(expectedIds);

        assertThat(statistics).isEqualTo(expectedStatistics);
    }

    @Test
    void testCalculate_AllCustomers() {
        CustomerPojo customer1 = new CustomerPojo();
        customer1.id = UUID.randomUUID();
        customer1.firstName = "John";
        customer1.lastName = "Wick";
        customer1.login = "john_wick@example.com";
        customer1.pass = "Baba_Jaga";
        customer1.balance = 100;

        UUID subscriptionUUID = UUID.randomUUID();

        CustomerPojo customer2 = new CustomerPojo();
        customer2.id = UUID.randomUUID();
        customer2.firstName = "Ryan";
        customer2.lastName = "Gosling";
        customer2.login = "ryan_gosling@example.com";
        customer2.pass = "Kojey_Bessmertnii";
        customer2.balance = 250;

        UUID subscription2UUID = UUID.randomUUID();

        UUID randomUUID = UUID.randomUUID();

        HashSet<UUID> customerIds = new HashSet<>();
        customerIds.add(customer1.id);
        customerIds.add(customer2.id);
        customerIds.add(randomUUID);

        when(customerService.getCustomerIds()).thenReturn(customerIds);

        StatisticPerCustomerBO statistic1 = new StatisticPerCustomerBO();
        statistic1.setOverallBalance(customer1.balance);
        statistic1.setOverallFee(399);
        HashSet<UUID> expectedIds1 = new HashSet<>();
        expectedIds1.add(subscriptionUUID);
        statistic1.setSubscriptionIds(expectedIds1);

        StatisticPerCustomerBO statistic2 = new StatisticPerCustomerBO();
        statistic2.setOverallBalance(customer2.balance);
        statistic2.setOverallFee(499);
        HashSet<UUID> expectedIds2 = new HashSet<>();
        expectedIds2.add(subscription2UUID);
        statistic2.setSubscriptionIds(expectedIds2);

        StatisticServiceImpl statisticServiceSpy = Mockito.spy(statisticService);

        doReturn(statistic1).when(statisticServiceSpy).calculate(customer1.id);
        doReturn(statistic2).when(statisticServiceSpy).calculate(customer2.id);
        doReturn(null).when(statisticServiceSpy).calculate(randomUUID);

        HashSet<StatisticPerCustomerBO> customers = new HashSet<>();
        customers.add(statistic1);
        customers.add(statistic2);

        StatisticBO statistics = statisticServiceSpy.calculate();

        StatisticBO expectedStatistics = new StatisticBO();
        expectedStatistics.setOverallBalance(350);
        expectedStatistics.setOverallFee(898);
        expectedStatistics.setCustomers(customers);

        assertThat(statistics).isEqualTo(expectedStatistics);
    }
}