package esproject.core.configurations.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;

import esproject.core.configurations.SecretConfiguration;

/**
 * Secret OSGI configuration file for project. SecretConfiguration class only for OSGI config values.
 *
 * @author yograna
 *
 */
@Component(
        service = SecretConfigurationImpl.class,
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = SecretConfiguration.class)
public class SecretConfigurationImpl {

    /** The es user. */
    private String esUser;

    /** The es password. */
    private String esPassword;

    /**
     * Activate.
     *
     * @param configuration
     *            the configuration
     */
    @Activate
    @Modified
    public void activate(final SecretConfiguration configuration) {

        esUser = configuration.getEsUser();
        esPassword = configuration.getEsPassword();

    }

    /**
     * Gets the es user.
     *
     * @return the es user
     */
    public String getEsUser() {
        return esUser;
    }

    /**
     * Gets the es password.
     *
     * @return the es password
     */
    public String getEsPassword() {
        return esPassword;
    }

}
