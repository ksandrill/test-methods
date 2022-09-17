package org.nsu.fit.tm_backend.manager;

import lombok.var;
import org.slf4j.Logger;
import org.nsu.fit.tm_backend.repository.IRepository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.controller.data.TopUpBalanceRequest;
import org.nsu.fit.tm_backend.manager.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Globals;

import java.util.List;
import java.util.UUID;

public class CustomerManager extends ParentManager {
    public CustomerManager(IRepository dbService, Logger flowLog) {
        super(dbService, flowLog);
    }

    /**
     * Метод создает новый объект класса Customer. Ограничения:
     * Аргумент 'customer' - не null;
     * firstName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * lastName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * login - указывается в виде email, проверить email на корректность, проверить что нет customer с таким же email;
     * pass - длина от 6 до 12 символов включительно, не должен быть простым (123qwe или 1q2w3e), не должен содержать части login, firstName, lastName
     * balance - должно быть равно 0 перед отправкой базу данных.
     */
    public CustomerPojo createCustomer(CustomerPojo customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Argument 'customer' is null.");
        }

        if (customer.pass == null) {
            throw new IllegalArgumentException("Field 'customer.pass' is null.");
        }

        if (customer.pass.length() < 6 || customer.pass.length() > 12) {
            throw new IllegalArgumentException("Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
        }

        if (customer.pass.equalsIgnoreCase("123qwe")) {
            throw new IllegalArgumentException("Password is very easy.");
        }

        // Лабораторная 2: добавить код который бы проверял, что нет customer'а c таким же login (email'ом).
        // Попробовать добавить другие ограничения, посмотреть как быстро растет кодовая база тестов.

        return dbService.createCustomer(customer);
    }

    /**
     * Метод возвращает список customer'ов.
     */
    public List<CustomerPojo> getCustomers() {
        return dbService.getCustomers();
    }

    public CustomerPojo getCustomer(UUID customerId) {
        return dbService.getCustomer(customerId);
    }

    public CustomerPojo lookupCustomer(String login) {
        return dbService.getCustomers().stream()
                .filter(x -> x.login.equals(login))
                .findFirst()
                .orElse(null);
    }

    public ContactPojo me(AuthenticatedUserDetails authenticatedUserDetails) {
        ContactPojo contactPojo = new ContactPojo();

        if (authenticatedUserDetails.isAdmin()) {
            contactPojo.login = Globals.ADMIN_LOGIN;

            return contactPojo;
        }

        // Лабораторная 2: обратите внимание что вернули данных больше чем надо...
        // т.е. getCustomerByLogin честно возвратит все что есть в базе данных по этому customer'у.
        // необходимо написать такой unit тест, который бы отлавливал данное поведение.
        return dbService.getCustomerByLogin(authenticatedUserDetails.getName());
    }

    public void deleteCustomer(UUID id) {
        dbService.deleteCustomer(id);
    }

    /**
     * Метод добавляет к текущему балансу переданное значение, которое должно быть строго больше нуля.
     */
    public CustomerPojo topUpBalance(UUID customerId, Integer money) {
        var customerPojo = dbService.getCustomer(customerId);

        customerPojo.balance += money;

        dbService.editCustomer(customerPojo);

        return customerPojo;
    }
}
