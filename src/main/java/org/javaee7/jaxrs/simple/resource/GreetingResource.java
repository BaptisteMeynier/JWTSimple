package org.javaee7.jaxrs.simple.resource;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.javaee7.jaxrs.simple.filter.JWTTokenNeeded;

@Path("/secure/greeting")
@Produces(TEXT_PLAIN)
public class GreetingResource {


	// ======================================
	// =          Business methods          =
	// ======================================

	@GET
	public Response echo(@QueryParam("message") String message) {
		return Response.ok().entity(message == null ? "no message" : message).build();
	}

	@GET
	@Path("jwt")
	@JWTTokenNeeded
	public Response echoWithJWTToken(@QueryParam("message") String message) {
		return Response.ok().entity(message == null ? "no message" : message).build();
	}
}
