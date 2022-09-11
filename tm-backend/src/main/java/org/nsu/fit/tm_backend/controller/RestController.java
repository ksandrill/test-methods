package org.nsu.fit.tm_backend.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.nsu.fit.tm_backend.MainFactory;
import org.nsu.fit.tm_backend.repository.impl.MemoryRepository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CredentialsPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.HealthCheckPojo;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.repository.data.TopUpBalancePojo;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.JsonMapper;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("")
public class RestController {
    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response authenticate(String credentialsDataJson) {
        // convert json to object.
        CredentialsPojo credentialsPojo = JsonMapper.fromJson(credentialsDataJson, CredentialsPojo.class);

        return Response.ok().entity(JsonMapper.toJson(
                MainFactory.getInstance().getAuthenticationTokenManager().authenticate(credentialsPojo),
                true)).build();
    }

    @GET
    @Path("/health_check")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response healthCheck() {
        HealthCheckPojo result = new HealthCheckPojo();
        try {
            new MemoryRepository(LoggerFactory.getLogger(MemoryRepository.class));
            result.dbStatus = "OK";
        } catch (Throwable ex) {
            result.dbStatus = ex.getMessage();
        }
        return Response.ok().entity(JsonMapper.toJson(result, true)).build();
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Authority.ADMIN_ROLE, Authority.CUSTOMER_ROLE })
    public Response me(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            ContactPojo contactPojo = MainFactory.getInstance()
                    .getCustomerManager()
                    .me(authenticatedUserDetails);

            return Response.ok().entity(JsonMapper.toJson(contactPojo, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    // Example request: ../customers?login='john_wick@example.com'
    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Authority.ADMIN_ROLE, Authority.CUSTOMER_ROLE })
    public Response getCustomers(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("login") String customerLogin) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            if (authenticatedUserDetails.isCustomer()) {
                customerLogin = authenticatedUserDetails.getName();
            }

            String login = customerLogin;

            List<CustomerPojo> customers = MainFactory.getInstance()
                    .getCustomerManager()
                    .getCustomers().stream()
                    .filter(x -> login.isEmpty() || x.login.equals(login))
                    .collect(Collectors.toList());

            return Response.ok().entity(JsonMapper.toJson(customers, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/customers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response createCustomer(CustomerPojo customerData) {
        try {
            // create new customer
            CustomerPojo customer = MainFactory.getInstance().getCustomerManager().createCustomer(customerData);

            // send the answer
            return Response.ok().entity(JsonMapper.toJson(customer, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/customers/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response deleteCustomer(@PathParam("id") String customerId) {
        try {
            MainFactory.getInstance().getCustomerManager().deleteCustomer(UUID.fromString(customerId));

            // send the answer
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/customers/top_up_balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response topUpBalance(@Context SecurityContext securityContext, String topUpBalancePojoStr) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            // convert json to object.
            TopUpBalancePojo topUpBalancePojo = JsonMapper.fromJson(topUpBalancePojoStr, TopUpBalancePojo.class);

            topUpBalancePojo.customerId = UUID.fromString(authenticatedUserDetails.getUserId());
            MainFactory.getInstance().getCustomerManager().topUpBalance(topUpBalancePojo);

            // send the answer.
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/plans")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response getPlans(@DefaultValue("") @QueryParam("customer_id") String customerIdStr) {
        try {
            UUID customerId = null;
            if (!StringUtils.isBlank(customerIdStr)) {
                customerId = UUID.fromString(customerIdStr);
            }

            List<PlanPojo> plans = MainFactory.getInstance()
                    .getPlanManager()
                    .getPlans(customerId);

            return Response.ok().entity(JsonMapper.toJson(plans, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/available_plans")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response getAvailablePlans(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            List<PlanPojo> plans = MainFactory.getInstance()
                    .getPlanManager()
                    .getPlans(UUID.fromString(authenticatedUserDetails.getUserId()));

            return Response.ok().entity(JsonMapper.toJson(plans, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response createPlan(String planDataJson) {
        try {
            // convert json to object
            PlanPojo planData = JsonMapper.fromJson(planDataJson, PlanPojo.class);

            // create new customer
            PlanPojo plan = MainFactory.getInstance().getPlanManager().createPlan(planData);

            // send the answer
            return Response.ok().entity(JsonMapper.toJson(plan, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/plans/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response deletePlan(@PathParam("id") String planId) {
        try {
            MainFactory.getInstance().getPlanManager().deletePlan(UUID.fromString(planId));

            // send the answer
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response createSubscription(@Context SecurityContext securityContext, String subscriptionDataJson) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            // convert json to object.
            SubscriptionPojo subscriptionPojo = JsonMapper.fromJson(subscriptionDataJson, SubscriptionPojo.class);

            // create new subscription.
            subscriptionPojo.customerId = UUID.fromString(authenticatedUserDetails.getUserId());
            subscriptionPojo = MainFactory.getInstance().getSubscriptionManager().createSubscription(subscriptionPojo);

            // send the answer.
            return Response.ok().entity(JsonMapper.toJson(subscriptionPojo, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/subscriptions/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response deleteSubscription(@PathParam("id") String subscriptionId) {
        try {
            MainFactory.getInstance().getSubscriptionManager().deleteSubscription(UUID.fromString(subscriptionId));
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response getSubscriptions(@DefaultValue("") @QueryParam("customer_id") String customerIdStr) {
        try {
            UUID customerId = null;
            if (!StringUtils.isBlank(customerIdStr)) {
                customerId = UUID.fromString(customerIdStr);
            }

            List<SubscriptionPojo> subscriptions = MainFactory.getInstance()
                    .getSubscriptionManager()
                    .getSubscriptions(customerId);

            return Response.ok().entity(JsonMapper.toJson(subscriptions, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/available_subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response getAvailableSubscriptions(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            List<SubscriptionPojo> subscriptions = MainFactory.getInstance()
                    .getSubscriptionManager()
                    .getSubscriptions(UUID.fromString(authenticatedUserDetails.getUserId()));

            return Response.ok().entity(JsonMapper.toJson(subscriptions, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }
}
