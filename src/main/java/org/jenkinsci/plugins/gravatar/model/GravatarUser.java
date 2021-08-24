package org.jenkinsci.plugins.gravatar.model;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import hudson.model.User;
import hudson.tasks.Mailer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.emptyToNull;

public class GravatarUser {

	private final String userId;

	private final Supplier<Optional<String>> mailSupplier = Suppliers.memoize(new Supplier<Optional<String>>() {
		public Optional<String> get() {
			Mailer.UserProperty mailProperty = user().getProperty(Mailer.UserProperty.class);
			if (mailProperty == null) {
				return Optional.absent();
			}
			return Optional.fromNullable(emptyToNull(mailProperty.getAddress()));
		}
	});

	public GravatarUser(User user) {
		checkNotNull(user);
		this.userId = user.getId();
	}

	public static GravatarUser gravatarUser(User user) {
		checkNotNull(user);
		return new GravatarUser(user);
	}

	public User user() {
		return User.get(userId());
	}

	private String userId() {
		return this.userId;
	}


	public Optional<String> emailAddress() {
		return mailSupplier.get();
	}

	@Override
	public String toString() {
		return String.format("GravatarUser{userId='%s'}", userId);
	}

	//--- for using as a cacheKey

	@Override
	public int hashCode() {
		return Objects.hashCode(this.userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final GravatarUser other = (GravatarUser) obj;
		return Objects.equal(this.userId, other.userId);
	}
}
