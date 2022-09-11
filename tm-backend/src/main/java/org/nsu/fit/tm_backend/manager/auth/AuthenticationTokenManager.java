package org.nsu.fit.tm_backend.manager.auth;

import org.slf4j.Logger;
import org.nsu.fit.tm_backend.MainFactory;
import org.nsu.fit.tm_backend.repository.IRepository;
import org.nsu.fit.tm_backend.repository.data.AccountTokenPojo;
import org.nsu.fit.tm_backend.repository.data.CredentialsPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.manager.ParentManager;
import org.nsu.fit.tm_backend.manager.auth.exception.AuthenticationException;
import org.nsu.fit.tm_backend.manager.auth.exception.AuthenticationTokenRefreshmentException;
import org.nsu.fit.tm_backend.manager.auth.exception.InvalidAuthenticationTokenException;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticationTokenDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Лабораторная *: Исследуйте данный класс, подумайте какие потенциальные проблемы он содержит.
 */
public class AuthenticationTokenManager extends ParentManager {
    public AuthenticationTokenManager(IRepository dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    public AccountTokenPojo authenticate(CredentialsPojo credentialsPojo) {
        log.info("Credentials: " + credentialsPojo.toString());

        AccountTokenPojo accountTokenPojo = new AccountTokenPojo();
        if (credentialsPojo.login.equalsIgnoreCase("admin")
                && credentialsPojo.pass.equalsIgnoreCase("setup")) {
            accountTokenPojo.id = UUID.randomUUID();
            accountTokenPojo.authorities = Collections.singleton(Authority.ADMIN_ROLE);
            accountTokenPojo.token = issueToken(credentialsPojo.login, accountTokenPojo.authorities);
        } else {
            CustomerPojo customerPojo = MainFactory.getInstance().getCustomerManager().lookupCustomer(credentialsPojo.login);

            if (customerPojo == null) {
                throw new AuthenticationException(String.format("Customer with login '%s' is not exists.", credentialsPojo.login));
            }

            accountTokenPojo.id = UUID.randomUUID();
            accountTokenPojo.authorities = Collections.singleton(Authority.CUSTOMER_ROLE);
            accountTokenPojo.token = issueToken(customerPojo.login, accountTokenPojo.authorities);
        }

        return dbService.createAccountToken(accountTokenPojo);
    }

    public AuthenticationTokenDetails lookupAuthenticationTokenDetails(String authenticationToken) {
        dbService.checkAccountToken(authenticationToken);

        return parseToken(authenticationToken);
    }

    public AuthenticatedUserDetails lookupAuthenticatedUserDetails(AuthenticationTokenDetails authenticationTokenDetails) {
        if (authenticationTokenDetails.getUserName().equalsIgnoreCase("admin")) {
            if (!authenticationTokenDetails.isAdmin()) {
                throw new InvalidAuthenticationTokenException("Invalid token...");
            }
            return new AuthenticatedUserDetails(null, authenticationTokenDetails.getUserName(), authenticationTokenDetails.getAuthorities());
        }

        if (!authenticationTokenDetails.isCustomer()) {
            throw new InvalidAuthenticationTokenException("Invalid token...");
        }

        CustomerPojo customerPojo = dbService.getCustomerByLogin(authenticationTokenDetails.getUserName());

        return new AuthenticatedUserDetails(customerPojo.id.toString(), authenticationTokenDetails.getUserName(), authenticationTokenDetails.getAuthorities());
    }

    /**
     * Issue a token for a user with the given authorities.
     */
    public String issueToken(String username, Set<String> authorities) {
        String id = generateTokenIdentifier();
        ZonedDateTime issuedDate = ZonedDateTime.now();
        ZonedDateTime expirationDate = calculateExpirationDate(issuedDate);

        AuthenticationTokenDetails authenticationTokenDetails = new AuthenticationTokenDetails(
                id,
                username,
                authorities,
                issuedDate,
                expirationDate,
                0,
                Globals.AUTHENTICATION_JWT_REFRESH_LIMIT);

        return new AuthenticationTokenIssuer().issueToken(authenticationTokenDetails);
    }

    /**
     * Parse and validate the token.
     */
    public AuthenticationTokenDetails parseToken(String token) {
        return new AuthenticationTokenParser().parseToken(token);
    }

    /**
     * Refresh a token.
     */
    public String refreshToken(AuthenticationTokenDetails currentTokenDetails) {
        if (!currentTokenDetails.isEligibleForRefreshment()) {
            throw new AuthenticationTokenRefreshmentException("This token cannot be refreshed");
        }

        ZonedDateTime issuedDate = ZonedDateTime.now();
        ZonedDateTime expirationDate = calculateExpirationDate(issuedDate);

        // Reuse the same id.
        AuthenticationTokenDetails newTokenDetails = new AuthenticationTokenDetails(
                currentTokenDetails.getId(),
                currentTokenDetails.getUserName(),
                currentTokenDetails.getAuthorities(),
                issuedDate,
                expirationDate,
                currentTokenDetails.getRefreshCount() + 1,
                Globals.AUTHENTICATION_JWT_REFRESH_LIMIT);

        return new AuthenticationTokenIssuer().issueToken(newTokenDetails);
    }

    /**
     * Calculate the expiration date for a token.
     */
    private ZonedDateTime calculateExpirationDate(ZonedDateTime issuedDate) {
        return issuedDate.plusSeconds(Globals.AUTHENTICATION_JWT_VALID_FOR);
    }

    /**
     * Generate a token identifier.
     */
    private String generateTokenIdentifier() {
        return UUID.randomUUID().toString();
    }
}
