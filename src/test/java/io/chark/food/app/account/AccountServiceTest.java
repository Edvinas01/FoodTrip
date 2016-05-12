package io.chark.food.app.account;

import io.chark.food.FoodTripIntegrationTest;
import io.chark.food.domain.authentication.account.Account;
import io.chark.food.domain.authentication.account.AccountRepository;
import io.chark.food.domain.authentication.permission.PermissionRepository;
import io.chark.food.domain.authentication.permission.Permission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@FoodTripIntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AccountServiceTest {

    private static final String TEST_USERNAME = "Username";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_EMAIL = "Test@test.com";

    @Resource
    private PermissionRepository permissionRepository;

    @Resource
    private AccountRepository accountRepository;

    private AccountService accountService;

    @Before
    public void setUp() {
        accountService = new AccountService(
                permissionRepository,
                accountRepository,
                new BCryptPasswordEncoder());
    }

    @After
    public void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    public void registerAccount() {
        Optional<Account> registered = accountService.register(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_PASSWORD);

        Account account = registered.get();
        assertThat(account.getAuthorities()).containsExactly(permissionRepository
                .findByAuthority(Permission.Authority.ROLE_USER));

        assertThat(account.getRegistrationDate()).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo(TEST_PASSWORD);
        assertThat(account.getPrettyUsername()).isEqualTo(TEST_USERNAME);
        assertThat(account.getEmail()).isEqualTo(TEST_EMAIL.trim().toLowerCase());
        assertThat(account.getUsername()).isEqualTo(TEST_USERNAME.trim().toLowerCase());
    }

    @Test
    public void registerFailed() {
        accountService.register(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        Optional<Account> registered = accountService
                .register(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD);

        assertThat(registered.isPresent()).isFalse();
    }
}