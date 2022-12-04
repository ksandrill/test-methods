package org.nsu.fit.tests.api.subscription;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.nsu.fit.services.rest.data.SubscriptionPojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.*;

public class DeleteTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private CustomerPojo customerPojo;
    private AccountTokenPojo customerToken;
    private PlanPojo planPojo;
    private SubscriptionPojo result;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete subscription feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create customer", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete subscription feature")
    public void createCustomerTest() {
        CustomerPojo pojo = new CustomerPojo();
        pojo.firstName = faker.name().firstName();
        pojo.lastName = faker.name().lastName();
        pojo.pass = faker.internet().password(7, 11);
        pojo.login = faker.internet().emailAddress();
        pojo.balance = faker.number().numberBetween(0, 100);

        customerPojo = restClient.createCustomer(pojo, adminToken);
        assertNotNull(customerPojo);
        assertEquals(customerPojo.firstName, customerPojo.firstName);
        assertEquals(customerPojo.lastName, customerPojo.lastName);
        assertEquals(customerPojo.login, customerPojo.login);
        assertEquals(customerPojo.balance, customerPojo.balance);

        customerToken = restClient.authenticate(customerPojo.login, customerPojo.pass);
        assertNotNull(customerToken);
    }

    @Test(description = "Create plan", dependsOnMethods = "createCustomerTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete subscription feature")
    public void createPlanTest() {
        PlanPojo pojo = new PlanPojo();
        pojo.details = faker.commerce().productName();
        pojo.name = faker.name().title();
        pojo.fee = faker.number().numberBetween(1, 10);

        planPojo = restClient.createPlan(pojo, adminToken);
        assertNotNull(restClient);
        assertEquals(pojo.name, planPojo.name);
    }

    @Test(description = "Create subscription", dependsOnMethods = "createCustomerTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete subscription feature")
    public void makeSubscriptionTest() {
        SubscriptionPojo pojo = new SubscriptionPojo();
        List<PlanPojo> availablePlans = restClient.getAvailablePlans(customerPojo.login);
        assertNotNull(availablePlans);
        assertNotEquals(availablePlans.size(), 0);

        PlanPojo plan = availablePlans.get(0);
        pojo.planId = plan.id;
        pojo.planName = plan.name;
        pojo.planDetails = plan.details;
        pojo.planFee = plan.fee;

        result = restClient.createSubscription(pojo, customerToken);
        assertNotNull(result);
        assertEquals(result.planId, pojo.planId);
        assertEquals(result.planName, pojo.planName);
        assertEquals(result.planDetails, pojo.planDetails);
        assertEquals(result.planFee, pojo.planFee);
        assertEquals(result.customerId, customerPojo.id);
    }

    @Test(description = "Delete subscription", dependsOnMethods = "makeSubscriptionTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete subscription feature")
    public void deleteSubscription() {
        restClient.deleteSubscription(result, customerToken);
        java.util.List<SubscriptionPojo> res = restClient.getSubscriptions(customerPojo.login, adminToken);
        System.out.println("fsfsfs");
        for (SubscriptionPojo subPojo : res) {
            assertNotSame(subPojo.id, result.id);
        }
    }

    @AfterClass
    public void afterClass() {
        restClient.deletePlan(planPojo, adminToken);
        restClient.deleteCustomer(customerPojo, adminToken);
    }
}
