package com.cspinformatique.kubik.kos.domain.account.service.impl;

import java.security.Principal;
import java.util.Arrays;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.cspinformatique.kubik.kos.domain.account.repository.AccountRepository;
import com.cspinformatique.kubik.kos.domain.account.service.AccountService;
import com.cspinformatique.kubik.kos.domain.account.service.RoleService;
import com.cspinformatique.kubik.kos.domain.kubik.service.KubikNotificationService;
import com.cspinformatique.kubik.kos.exception.ValidationException;
import com.cspinformatique.kubik.kos.model.account.Account;
import com.cspinformatique.kubik.kos.model.account.Address;
import com.cspinformatique.kubik.kos.model.account.Role;
import com.cspinformatique.kubik.kos.model.account.Role.Type;
import com.cspinformatique.kubik.kos.model.kubik.KubikNotification;
import com.cspinformatique.kubik.kos.model.kubik.KubikNotification.Action;

@Service
public class AccountServiceImpl implements AccountService {
	@Resource
	private AccountRepository accountRepository;

	@Resource
	private KubikNotificationService kubikNotificationService;

	@Resource
	private PasswordEncoder passwordEncoder;

	@Resource
	private RoleService roleService;

	@Override
	@Transactional
	public Account createAccount(String username, CharSequence password) {
		if (findByUsername(username) != null) {
			throw new ValidationException("usernameAlreadyExists");
		}

		Account account = save(new Account(0, username, passwordEncoder.encode(password),
				Arrays.asList(new Role[] { roleService.findByType(Type.CUSTOMER) }), false, null, null));

		kubikNotificationService.createNewNotification(account.getId(), KubikNotification.Type.ACCOUNT, Action.UPDATE);

		return account;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails result = accountRepository.findByUsername(username);

		if (result == null) {
			throw new UsernameNotFoundException("User " + username + " could not be found.");
		}

		return result;
	}

	@Override
	public Account findByUsername(String username) {
		return accountRepository.findByUsername(username);
	}

	@Override
	public Account findByPrincipal(Principal principal) {
		return principal != null ? findByUsername(principal.getName()) : null;
	}

	@Override
	public Account findOne(long id) {
		return accountRepository.findOne(id);
	}

	@Override
	public Account save(Account account) {
		return accountRepository.save(account);
	}

	@Override
	public Address saveAddress(Account account, Address address) {
		Assert.notNull(account);

		if (address.getType().equals(Address.Type.BILLING))
			account.setBillingAddress(address);
		else
			account.setShippingAddress(address);

		account = save(account);

		if (address.getType().equals(Address.Type.BILLING))
			return account.getBillingAddress();
		else
			return account.getShippingAddress();
	}
}
