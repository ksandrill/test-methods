package org.nsu.fit.tm_backend.impl;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Лабораторная 2: покрыть unit тестами класс CustomerServiceImpl на 100%.
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void testCreateCustomer() {
        // arrange: готовим входные аргументы и настраиваем mock'и.
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

        // act: вызываем метод, который хотим протестировать.
        CustomerPojo customer = customerService.createCustomer(createCustomerInput);

        // assert: проверяем результат выполнения метода.
        assertEquals(customer.id, createCustomerOutput.id);

        // Проверяем, что метод по созданию Customer был вызван ровно 1 раз с определенными аргументами
        verify(repository, times(1)).createCustomer(createCustomerInput);

        // Проверяем, что другие методы не вызывались...
        verify(repository, times(0)).getCustomers();
    }

    // Как не надо писать тест...
    @Test
    void testCreateCustomerWithNullArgument_Wrong() {
        try {
            customerService.createCustomer(null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Argument 'customer' is null.", ex.getMessage());
        }
    }

    @Test
    void testCreateCustomerWithNullArgument_Right() {
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.createCustomer(null));
        assertEquals("Argument 'customer' is null.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithShortPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwe";
        createCustomerInput.balance = 0;

        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Password is very easy.", exception.getMessage());
    }
}
