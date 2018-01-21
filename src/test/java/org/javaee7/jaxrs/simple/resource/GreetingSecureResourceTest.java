package org.javaee7.jaxrs.simple.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


import java.io.File;
import java.net.URI;
import java.security.Key;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.javaee7.jaxrs.simple.application.MyApplication;
import org.javaee7.jaxrs.simple.domain.User;
import org.javaee7.jaxrs.simple.filter.JWTTokenNeeded;
import org.javaee7.jaxrs.simple.filter.JWTTokenNeededFilter;
import org.javaee7.jaxrs.simple.utils.KeyGenerator;
import org.javaee7.jaxrs.simple.utils.LoggerProducer;
import org.javaee7.jaxrs.simple.utils.PasswordUtils;
import org.javaee7.jaxrs.simple.utils.SimpleKeyGenerator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.jsonwebtoken.Jwts;


@RunWith(Arquillian.class)
@RunAsClient
public class GreetingSecureResourceTest {


    // ======================================
    // =             Attributes             =
    // ======================================

    private static final User TEST_USER = new User("id", "last name", "first name", "login", "password");
    private static String token;
    private Client client;
    private WebTarget userTarget;
    private WebTarget secureGreetingTarget;

    // ======================================
    // =          Injection Points          =
    // ======================================

    @ArquillianResource
    private URI baseURL;

    // ======================================
    // =         Deployment methods         =
    // ======================================

    @Deployment(testable = false)
    public static WebArchive createDeployment() {

        // Import Maven runtime dependencies
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();
        
    	return ShrinkWrap.create(WebArchive.class)
                .addClasses(MyApplication.class)
                .addClasses(UserEndpoint.class,GreetingResource.class)
                .addClasses(JWTTokenNeededFilter.class,JWTTokenNeeded.class)
                .addClasses(User.class)
                .addClasses(PasswordUtils.class,LoggerProducer.class)
                .addClasses(KeyGenerator.class,SimpleKeyGenerator.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/persistence-test.xml", "META-INF/persistence.xml")
                .addAsLibraries(files);
    }

    // ======================================
    // =          Lifecycle methods         =
    // ======================================

    @Before
    public void initWebTarget() {
        client = ClientBuilder.newClient();
        userTarget = client
        		.target(baseURL).path("api").path("users");
        secureGreetingTarget  = client
        		.target(baseURL).path("api").path("secure").path("greeting").path("jwt");
    }
    
    // ======================================
    // =            Test methods            =
    // ======================================


    
    @Test
    @InSequence(1)
    public void invokingGreetingShouldFailCauseNoToken() throws Exception {
        Response response = secureGreetingTarget.request(TEXT_PLAIN).get();
        assertEquals(401, response.getStatus());
    }

    @Test
    @InSequence(2)
    public void shouldCreateAUser() throws Exception {
        Response response = userTarget.request(APPLICATION_JSON_TYPE).post(Entity.entity(TEST_USER, APPLICATION_JSON_TYPE));
        assertEquals(201, response.getStatus());
    }

    @Test
    @InSequence(3)
    public void shouldLogUserIn() throws Exception {
        Form form = new Form();
        form.param("login", TEST_USER.getLogin());
        form.param("password", TEST_USER.getPassword());

        Response response = userTarget.path("login").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaderString(HttpHeaders.AUTHORIZATION));
        token = response.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Check the JWT Token
        String justTheToken = token.substring("Bearer".length()).trim();
        Key key = new SimpleKeyGenerator().generateKey();
        assertEquals(1, Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getHeader().size());
        assertEquals("HS512", Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getHeader().getAlgorithm());
        assertEquals(4, Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getBody().size());
        assertEquals("login", Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getBody().getSubject());
        assertEquals(baseURL.toString().concat("api/users/login"), Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getBody().getIssuer());
        assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getBody().getIssuedAt());
        assertNotNull(Jwts.parser().setSigningKey(key).parseClaimsJws(justTheToken).getBody().getExpiration());
    }

    @Test
    @InSequence(4)
    public void invokingGreetingShouldSucceedCauseToken() throws Exception {
        Response response = secureGreetingTarget.request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, token).get();
        assertEquals(200, response.getStatus());
        assertEquals("no message", response.readEntity(String.class));
    }


    @Test
    @InSequence(5)
    public void shouldGreetingHello() throws Exception {
        Response response = secureGreetingTarget.queryParam("message", "hello").request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, token).get();
        assertEquals(200, response.getStatus());
        assertEquals("hello", response.readEntity(String.class));
    }
    
    
}

