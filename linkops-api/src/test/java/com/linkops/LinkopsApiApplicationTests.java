package com.linkops;

import com.linkops.user.domain.User;
import com.linkops.user.domain.UserRole;
import com.linkops.user.domain.UserStatus;
import com.linkops.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LinkopsApiApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void shouldPersistUserWithAuditingAndBcryptPassword() {
		String passwordHash = passwordEncoder.encode("Senha-segura-123");
		User user = new User(
				"Maria",
				"Mucavele",
				"maria.auditing.test@linkops.local",
				"+258840000000",
				passwordHash,
				UserRole.CLIENT
		);

		User savedUser = userRepository.saveAndFlush(user);

		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getCreatedAt()).isNotNull();
		assertThat(savedUser.getUpdatedAt()).isNotNull();
		assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(passwordEncoder.matches("Senha-segura-123", savedUser.getPasswordHash())).isTrue();
		assertThat(userRepository.findByEmailIgnoreCase("MARIA.AUDITING.TEST@LINKOPS.LOCAL"))
				.contains(savedUser);
	}

	@Test
	void shouldRejectPlaintextPasswordAtTheDomainBoundary() {
		assertThatThrownBy(() -> new User(
				"Utilizador",
				"Inseguro",
				"plaintext@linkops.local",
				null,
				"Senha-segura-123",
				UserRole.CLIENT
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("BCrypt");
	}

}
