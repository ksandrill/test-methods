package org.nsu.fit.tests.api.customer;
import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.CustomerPojo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
public class CreateTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private CustomerPojo result;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create customer", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Create customer feature")
    public void createCustomerTest() {
        CustomerPojo customerPojo = new CustomerPojo();
        customerPojo.firstName = faker.name().firstName();
        customerPojo.lastName = faker.name().lastName();
        customerPojo.pass = faker.internet().password(7, 11);
        customerPojo.login = faker.internet().emailAddress();
        customerPojo.balance = faker.number().numberBetween(0, 100);

        result = restClient.createCustomer(customerPojo, adminToken);
        assertNotNull(result);
        assertEquals(customerPojo.firstName, result.firstName);
        assertEquals(customerPojo.lastName, result.lastName);
        assertEquals(customerPojo.login, result.login);
        assertEquals(customerPojo.balance, result.balance);
    }

    @AfterClass
    public void afterClass() {
        restClient.deleteCustomer(result, adminToken);
    }
}
