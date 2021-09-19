package com.serrodcal;

import static io.quarkus.vertx.web.Route.HandlerType.FAILURE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.jboss.logging.Logger;

import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.smallrye.mutiny.Uni;

import java.util.NoSuchElementException;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import io.vertx.ext.web.RoutingContext;

@Singleton
@RouteBase(path = "/v3/assets", produces = "application/json")
public class AssestsResource {

    private static final Logger logger = Logger.getLogger(AssestsResource.class.getName());

    @Route(path = "/feature_releases/:version/:release_type", methods = HttpMethod.GET)
    public Uni<List<String>> get(
            @Param @NotNull Integer version, // Path Param, not null allowed
            @Param("release_type") @NotNull String releaseType, // Path Param by name, not null allowed
            @Param @NotNull String os, // Query Param, not null allowed
            @Param("arch") String architecture // Query Param, but nullable
    ) throws IllegalArgumentException {
        if (version < 3)
            throw new IllegalArgumentException("Version cannot be less than 3");
        List<String> response = new ArrayList(Arrays.asList(version.toString(), releaseType, os));
        if (Objects.nonNull(architecture))
            response.add(architecture);
        return Uni.createFrom().item(response);
    }

    @Route(path = "/*", type = FAILURE)
    public void error(RoutingContext context) {
        Throwable t = context.failure();
        if (t != null) {
            logger.error("Failed to handle request", t);
            int status = context.statusCode();
            String chunk = "";
            if (t instanceof NoSuchElementException) {
                status = 404;
            } else if (t instanceof IllegalArgumentException) {
                status = 422;
                chunk = new JsonObject().put("code", status)
                        .put("exceptionType", t.getClass().getName()).put("error", t.getMessage()).encode();
            }
            context.response().setStatusCode(status).end(chunk);
        } else {
            // Continue with the default error handler
            context.next();
        }
    }

}