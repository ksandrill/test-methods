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

import java.util.List;

import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class GetTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private CustomerPojo customerPojo;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get customers feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create customer", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get customers feature")
    public void createCustomersTest() {
        CustomerPojo customer1 = new CustomerPojo();
        customer1.firstName = faker.name().firstName();
        customer1.lastName = faker.name().lastName();
        customer1.pass = faker.internet().password(7, 11);
        customer1.login = faker.internet().emailAddress();
        customer1.balance = faker.number().numberBetween(0, 100);

        customerPojo = restClient.createCustomer(customer1, adminToken);

        assertEquals(customer1.login, customerPojo.login);
        assertNotNull(customer1.firstName, customerPojo.firstName);
    }

    @Test(description = "Get customers", dependsOnMethods = "createCustomersTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Get customers feature")
    public void getCustomersTest() {
        List<CustomerPojo> result = restClient.getCustomers(adminToken, customerPojo.login);
        assertNotNull(result);
        assertNotEquals(result.size(), 0);
    }

    @AfterClass
    public void afterClass() {
        restClient.deleteCustomer(customerPojo, adminToken);
    }
}
