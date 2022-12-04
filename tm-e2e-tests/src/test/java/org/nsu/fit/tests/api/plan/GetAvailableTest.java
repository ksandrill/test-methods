package org.nsu.fit.tests.api.plan;
import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNotSame;
public class GetAvailableTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private CustomerPojo customerPojo;
    private PlanPojo planPojo;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get available plans feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create customer", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get available plans feature")
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
    }

    @Test(description = "Create plan", dependsOnMethods = "createCustomerTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get available plans feature")
    public void createPlanTest() {
        PlanPojo newPlan = new PlanPojo();
        newPlan.details = faker.commerce().productName();
        newPlan.name = faker.name().title();
        newPlan.fee = faker.number().numberBetween(1, 10);

        planPojo = restClient.createPlan(newPlan, adminToken);
        assertNotNull(restClient);
        assertEquals(newPlan.name, planPojo.name);
    }

    @Test(description = "Get available plans", dependsOnMethods = "createPlanTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get available plans feature")
    public void getAvailablePlansTest() {
        List<PlanPojo> result = restClient.getAvailablePlans(customerPojo.login);
        assertNotNull(result);
        assertNotSame(0, result.size());
    }

    @AfterClass
    public void afterClass() {
        restClient.deletePlan(planPojo, adminToken);
        restClient.deleteCustomer(customerPojo, adminToken);
    }

}
