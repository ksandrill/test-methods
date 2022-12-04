package org.nsu.fit.tests.api.customer;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.nsu.fit.services.rest.data.TopUpBalanceRequest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class TopUpBalanceTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private CustomerPojo customerPojo;

    @BeforeClass
    void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Top Up balance feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create customer", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Top Up balance feature")
    public void createCustomerTest() {
        CustomerPojo pojo = new CustomerPojo();
        pojo.firstName = faker.name().firstName();
        pojo.lastName = faker.name().lastName();
        pojo.login = faker.internet().emailAddress();
        pojo.balance = 0;
        pojo.pass = faker.internet().password(7, 11);

        customerPojo = restClient.createCustomer(pojo, adminToken);
        assertNotNull(customerPojo);
        assertEquals(pojo.firstName, customerPojo.firstName);
        assertEquals(pojo.login, customerPojo.login);
        assertEquals(pojo.pass, customerPojo.pass);
        assertEquals(pojo.balance, customerPojo.balance);
    }

    @Test(description = "To Up balance", dependsOnMethods = "createCustomerTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Top Up balance feature")
    public void toUpBalanceTest() {
        TopUpBalanceRequest topUpBalanceRequest = new TopUpBalanceRequest();
        topUpBalanceRequest.money = 42;

        AccountTokenPojo customerToken = restClient.authenticate(customerPojo.login, customerPojo.pass);

        restClient.topUpBalance(customerToken, topUpBalanceRequest);

        CustomerPojo check = restClient.meCustomer(customerToken);
        assertEquals(42, check.balance);
    }

    @AfterClass
    public void afterClass() {
        restClient.deleteCustomer(customerPojo, adminToken);
    }

}