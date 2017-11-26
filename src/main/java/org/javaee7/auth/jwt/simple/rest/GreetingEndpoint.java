package org.javaee7.auth.jwt.simple.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.javaee7.auth.jwt.simple.rest.naming.JWTTokenNeeded;

@Path("/greeting")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingEndpoint {

	@GET
    public Response greeting(@QueryParam("message") String message) {
        return Response.ok().entity(message == null ? "no message" : message).build();
    }

    @GET
    @Path("jwt")
    @JWTTokenNeeded
    public Response greetingWithJWTToken(@QueryParam("message") String message) {
        return Response.ok().entity(message == null ? "no message" : message).build();
    }
	
}
