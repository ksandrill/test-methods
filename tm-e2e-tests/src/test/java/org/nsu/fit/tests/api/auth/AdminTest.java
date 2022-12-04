
package org.nsu.fit.tests.api.auth;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.ContactPojo;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class AdminTest {
    private AccountTokenPojo adminToken;
    private RestClient restClient;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Authentication as admin feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Me as Admin", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Authentication as admin feature")
    public void meAsAdminTest() {
        ContactPojo contactPojo = restClient.meAdmin(adminToken);
        assertNotNull(contactPojo);
        assertEquals("admin", contactPojo.login);
    }

}