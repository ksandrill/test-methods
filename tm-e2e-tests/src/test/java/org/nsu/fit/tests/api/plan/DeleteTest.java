package org.nsu.fit.tests.api.plan;

import com.github.javafaker.Faker;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.nsu.fit.services.rest.RestClient;
import org.nsu.fit.services.rest.data.AccountTokenPojo;
import org.nsu.fit.services.rest.data.PlanPojo;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class DeleteTest {
    private RestClient restClient;
    private Faker faker;
    private AccountTokenPojo adminToken;
    private PlanPojo result;

    @BeforeClass
    public void beforeClass() {
        restClient = new RestClient();
        faker = new Faker();
    }

    @Test(description = "Authenticate as admin")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete plan feature")
    public void authAsAdminTest() {
        adminToken = restClient.authenticate("admin", "setup");
        assertNotNull(adminToken);
    }

    @Test(description = "Create plan", dependsOnMethods = "authAsAdminTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete plan feature")
    public void createPlanTest() {
        PlanPojo planPojo = new PlanPojo();
        planPojo.details = faker.commerce().productName();
        planPojo.name = faker.name().title();
        planPojo.fee = faker.number().numberBetween(1, 10);

        result = restClient.createPlan(planPojo, adminToken);
        assertNotNull(restClient);
        assertEquals(planPojo.name, result.name);
    }

    @Test(description = "Delete plan", dependsOnMethods = "createPlanTest")
    @Severity(SeverityLevel.BLOCKER)
    @Feature("Delete plan feature")
    public void deletePlanTest() {
        restClient.deletePlan(result, adminToken);
        java.util.List<PlanPojo> res = restClient.getPlans(adminToken, result.name);
        for(PlanPojo planPojo: res){
            assertNotSame(result.name, planPojo.name);
        }

    }
}
