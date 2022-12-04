package org.nsu.fit.tm_backend.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void testCreateCustomer_Success() {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;

        CustomerPojo createCustomerOutput = new CustomerPojo();
        createCustomerOutput.id = UUID.randomUUID();
        createCustomerOutput.firstName = "John";
        createCustomerOutput.lastName = "Wick";
        createCustomerOutput.login = "john_wick@example.com";
        createCustomerOutput.pass = "Baba_Jaga";
        createCustomerOutput.balance = 0;

        when(repository.createCustomer(createCustomerInput)).thenReturn(createCustomerOutput);

        // Act
        CustomerPojo customer = customerService.createCustomer(createCustomerInput);

        // Assert
        assertThat(createCustomerOutput.id).isEqualTo(customer.id);
        verify(repository, times(1)).createCustomer(createCustomerInput);
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testCreateCustomer_CustomerIsNull() {
        assertThatThrownBy(() -> customerService.createCustomer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Argument 'customer' is null.");
    }

    @Test
    void testCreateCustomer_CustomerPasswordIsNull() {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = null;
        createCustomerInput.balance = 0;

        // Assert
        assertThatThrownBy(() -> customerService.createCustomer(createCustomerInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Field 'customer.pass' is null.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "1234123412341"})
    void testCreateCustomer_CustomerPasswordLengthOutOfBounds(String password) {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = password;
        createCustomerInput.balance = 0;

        // Assert
        assertThatThrownBy(() -> customerService.createCustomer(createCustomerInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
    }

    @Test
    void testCreateCustomer_KnownPassword() {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwe";
        createCustomerInput.balance = 0;

        // Assert
        assertThatThrownBy(() -> customerService.createCustomer(createCustomerInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is very easy.");
    }

    @Test
    void testCreateCustomer_LoginAlreadyUsed() {
        // Arrange
        CustomerPojo existingCustomer = new CustomerPojo();
        existingCustomer.firstName = "John";
        existingCustomer.lastName = "Wick";
        existingCustomer.login = "john_wick@example.com";
        existingCustomer.pass = "Baba_Jaga";
        existingCustomer.balance = 0;

        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "Ulrich";
        createCustomerInput.lastName = "Tomsen";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(existingCustomer).collect(Collectors.toSet()));

        // Assert
        assertThatThrownBy(() -> customerService.createCustomer(createCustomerInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with provided login already exists.");
        verify(repository, times(1)).getCustomers();
    }

    @ParameterizedTest
    @ValueSource(strings = {"john", "JOHN", "J1hn", "Jo hn", "J$hn", " John "})
    void testCreateCustomer_FirstNameBadFormat(String firstName) {
        // Arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = firstName;
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwerty";
        createCustomerInput.balance = 0;

        // Assert
        assertThatThrownBy(() -> customerService.createCustomer(createCustomerInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name format is invalid.");
    }

    @Test
    void testGetCustomers() {
        // Arrange
        Set<CustomerPojo> customers = Stream
                .of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .map(uuid -> {
                    CustomerPojo customer = new CustomerPojo();
                    customer.id = uuid;
                    return customer;
                })
                .collect(Collectors.toSet());

        when(repository.getCustomers()).thenReturn(customers);

        // Act
        Set<CustomerPojo> retrievedCustomers = customerService.getCustomers();

        // Assert
        assertThat(retrievedCustomers).isEqualTo(customers);
    }

    @Test
    void testGetCustomerIds() {
        // Arrange
        Set<UUID> ids = Stream
                .of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .collect(Collectors.toSet());

        when(repository.getCustomerIds()).thenReturn(ids);

        // Act
        Set<UUID> retrievedIds = customerService.getCustomerIds();

        // Assert
        assertThat(retrievedIds).isEqualTo(ids);
    }

    @Test
    void testGetCustomer_Success() {
        // Arrange
        UUID getCustomerInput = UUID.randomUUID();
        CustomerPojo customer = new CustomerPojo();
        customer.id = getCustomerInput;
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomer(getCustomerInput)).thenReturn(customer);

        // Act
        CustomerPojo retrievedCustomer = customerService.getCustomer(getCustomerInput);

        // Assert
        assertThat(retrievedCustomer).isEqualTo(customer);
    }

    @Test
    void testGetCustomer_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.getCustomer(UUID.randomUUID());

        // Assert
        assertThat(retrievedCustomer).isNull();
    }

    @Test
    void testLookupCustomerById_Success() {
        // Arrange
        UUID lookupCustomerInput = UUID.randomUUID();
        CustomerPojo customer = new CustomerPojo();
        customer.id = lookupCustomerInput;
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(customer).collect(Collectors.toSet()));

        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(lookupCustomerInput);

        // Assert
        assertThat(retrievedCustomer).isEqualTo(customer);
    }

    @Test
    void testLookupCustomerById_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(UUID.randomUUID());

        // Assert
        assertThat(retrievedCustomer).isNull();
    }

    @Test
    void testLookupCustomerByLogin_Success() {
        // Arrange
        String lookupCustomerInput = "john_wick@example.com";
        CustomerPojo customer = new CustomerPojo();
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = lookupCustomerInput;
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomers()).thenReturn(Stream.of(customer).collect(Collectors.toSet()));

        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer(lookupCustomerInput);

        // Assert
        assertThat(retrievedCustomer).isEqualTo(customer);
    }

    @Test
    void testLookupCustomerByLogin_CustomerDoesNotExist() {
        // Act
        CustomerPojo retrievedCustomer = customerService.lookupCustomer("john_wick@example.com");

        // Assert
        assertThat(retrievedCustomer).isNull();
    }

    @Test
    void testMe_Admin() {
        // Arrange
        AuthenticatedUserDetails meInput = new AuthenticatedUserDetails(
                "",
                "",
                Stream.of(Authority.ADMIN_ROLE).collect(Collectors.toSet())
        );
        ContactPojo adminContact = new ContactPojo();
        adminContact.login = Globals.ADMIN_LOGIN;

        // Act
        ContactPojo retrievedContact = customerService.me(meInput);

        // Assert
        assertThat(retrievedContact)
                .usingRecursiveComparison()
                .isEqualTo(adminContact);
    }

    @Test
    void testMe_RegularUser() {
        // Arrange
        String login = "john_wick@example.com";
        AuthenticatedUserDetails meInput = new AuthenticatedUserDetails(
                "",
                login,
                Collections.emptySet()
        );
        ContactPojo userContact = new ContactPojo();
        userContact.login = login;

        CustomerPojo customer = new CustomerPojo();
        customer.firstName = "John";
        customer.lastName = "Wick";
        customer.login = "john_wick@example.com";
        customer.pass = "Baba_Jaga";
        customer.balance = 0;

        when(repository.getCustomerByLogin(login)).thenReturn(customer);

        // Act
        ContactPojo retrievedContact = customerService.me(meInput);

        // Assert
        assertThat(retrievedContact)
                .usingRecursiveComparison()
                .isEqualTo(userContact);
    }

    @Test
    void testMe_NonExistentUser() {
        // Act
        ContactPojo retrievedContact = customerService.me(
                new AuthenticatedUserDetails("", "", Collections.emptySet())
        );

        // Assert
        assertThat(retrievedContact).isNull();
    }

    @Test
    void testDeleteCustomer() {
        // Arrange
        UUID deleteCustomerInput = UUID.randomUUID();

        // Act
        customerService.deleteCustomer(deleteCustomerInput);

        // Assert
        verify(repository, times(1)).deleteCustomer(deleteCustomerInput);
    }

    @Test
    void testTopUpBalance_Success() {
        // Arrange
        UUID topUpBalanceInput = UUID.randomUUID();
        int initialBalance = new Random().nextInt(1000);
        int addedBalance = 50;
        CustomerPojo customer = new CustomerPojo();
        customer.id = topUpBalanceInput;
        customer.balance = initialBalance;

        when(repository.getCustomer(topUpBalanceInput)).thenReturn(customer);

        // Act
        CustomerPojo editedCustomer = customerService.topUpBalance(topUpBalanceInput, addedBalance);

        // Assert
        customer.balance = initialBalance + addedBalance;
        assertThat(editedCustomer).isEqualTo(customer);
        verify(repository, times(1)).editCustomer(customer);
    }

    @Test
    void testTopUpBalance_NegativeMoney() {
        // Arrange
        UUID topUpBalanceInput = UUID.randomUUID();

        // Act/assert
        assertThatThrownBy(() -> customerService.topUpBalance(topUpBalanceInput, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Money amount must be strictly positive.");

        verify(repository, times(0)).editCustomer(any());
    }

    @Test
    void testTopUpBalance_CustomerDoesNotExist() {
        // Act
        CustomerPojo customer = customerService.topUpBalance(UUID.randomUUID(), 1);

        // Assert
        assertThat(customer).isNull();
        verify(repository, times(0)).editCustomer(any());
    }
}