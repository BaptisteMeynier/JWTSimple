package org.javaee7.jaxrs.simple.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.javaee7.jaxrs.simple.resource.GreetingResource;
import org.javaee7.jaxrs.simple.resource.UserEndpoint;
import org.javaee7.jaxrs.simple.filter.JWTTokenNeededFilter;

@ApplicationPath("api")
public class MyApplication extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
	    Set<Class<?>> s = new HashSet<Class<?>>();
	    s.add(UserEndpoint.class);
	    s.add(GreetingResource.class);
	    s.add(JWTTokenNeededFilter.class);
	    return s;
	}
}
