package eu.europeana.metis.ui.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.EmbeddedLdapServer;
import org.springframework.ldap.test.EmbeddedLdapServerFactoryBean;
import org.springframework.ldap.test.LdifPopulator;
import org.springframework.security.config.annotation.authentication.configurers.ldap.LdapAuthenticationProviderConfigurer.ContextSourceBuilder;
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper;
import org.springframework.security.ldap.userdetails.InetOrgPersonContextMapper;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

import eu.europeana.metis.ui.ldap.dao.UserDao;
import eu.europeana.metis.ui.ldap.dao.impl.UserDaoImpl;

@Configuration
@PropertySource("classpath:/authentication.properties")
public class LDAPManagerConfig {

	@Value("${ldif.url}")
	private String url;
	
	@Value("${ldif.dn}")
	private String dn;
	
	@Value("${ldif.pwd}")
	private String pwd;
	
	@Value("${ldif.base}")
	private String base;
	
	@Value("${ldif.clean}")
	private String clean;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public LdapContextSource contextSource() {
	    LdapContextSource ldapContextSource = new LdapContextSource();
	    ldapContextSource.setUrl(url);
	    ldapContextSource.setBase(base);
	    ldapContextSource.setUserDn(dn);
	    ldapContextSource.setPassword(pwd);
	    return ldapContextSource;
	}

	@Bean
	public LdapTemplate ldapTemplate() {
	    return new LdapTemplate(contextSource());
	}

	@Bean
	public InetOrgPersonContextMapper inetOrgPersonContextMapper() {
	    return new InetOrgPersonContextMapper();
	}
	
	@Bean
	public DefaultLdapUsernameToDnMapper defaultLdapUsernameToDnMapper() {
	    return new DefaultLdapUsernameToDnMapper("ou=users", "uid");// "uid"
	}

	@Bean
	public LdapUserDetailsManager ldapUserDetailManager() {
	    LdapUserDetailsManager userManager = new LdapUserDetailsManager(contextSource());
	    userManager.setGroupSearchBase("ou=roles,ou=metis_authentication");
	    userManager.setUserDetailsMapper(inetOrgPersonContextMapper());
	    userManager.setUsernameMapper(defaultLdapUsernameToDnMapper());
	    userManager.setGroupRoleAttributeName("cn");
	    userManager.setGroupMemberAttributeName("member");
	    return userManager;
	}
	
	@Bean
	public EmbeddedLdapServerFactoryBean embeddedLdapServer() {
		EmbeddedLdapServerFactoryBean embeddedLdapServerFactoryBean = new EmbeddedLdapServerFactoryBean();
		embeddedLdapServerFactoryBean.setPartitionName("example");
		embeddedLdapServerFactoryBean.setPartitionSuffix(base);
		embeddedLdapServerFactoryBean.setPort(18880);		
		return embeddedLdapServerFactoryBean;
	}
	
	@Bean
	@DependsOn("embeddedLdapServer")
	public LdifPopulator ldifPopulator() {
		LdifPopulator ldifPopulator = new LdifPopulator();
		ldifPopulator.setContextSource(contextSource());
		ldifPopulator.setResource(new ClassPathResource("metis-ldap.ldif"));
		ldifPopulator.setBase(base);
		ldifPopulator.setDefaultBase("dc=europeana,dc=eu");
		ldifPopulator.setClean(Boolean.valueOf(clean));
		return ldifPopulator;
	}
	
	@Bean
	public UserDao userDao() {
		return new UserDaoImpl();
	}
}