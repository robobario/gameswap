package org.gameswap.web.resource;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.eclipse.jetty.http.HttpStatus;
import org.gameswap.model.User;
import org.gameswap.persistance.UserDAO;
import org.gameswap.security.PasswordService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.gameswap.web.resource.Api.createUser;
import static org.gameswap.web.resource.Api.helpers;
import static org.mockito.Mockito.*;


public class UserResourceTest {

    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static UserDAO dao = mock(UserDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
                                                                     .addResource(new UserResource(dao))
                                                                     .addProvider(new ContextInjectableProvider<>(HttpServletRequest.class, request))
                                                                     .build();
    private User casper;


    @Before
    public void setUp() throws Exception {
        reset(dao);
        casper = createUser("casper", PasswordService.hashPassword("thefriendlyghost"));
        User mario = createUser("mario", PasswordService.hashPassword("luigi"));
        when(dao.findAll()).thenReturn(newArrayList(casper, mario));
    }

    @Test
    public void testGetAll() throws Exception {
        Response response = whenGetAllUsers();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        JsonArray userArray = (JsonArray) helpers().getJsonResponse(response);
        JsonObject user1 = (JsonObject) userArray.get(0);
        JsonObject user2 = (JsonObject) userArray.get(1);
        assertThat(user1.get("username").getAsString()).isEqualTo("casper");
        assertThat(user2.get("username").getAsString()).isEqualTo("mario");
    }

    @Test
    public void testGet() throws Exception {
        int userId = 10;
        when(dao.find(userId)).thenReturn(Optional.of(casper));
        Response response = whenGetOneUser(userId);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        JsonObject hopefullyCasper = (JsonObject) helpers().getJsonResponse(response);
        assertThat(hopefullyCasper.get("username").getAsString()).isEqualTo("casper");
    }

    @Test
    public void testUpdate() throws Exception {
        int userId = 12;
        when(dao.find(userId)).thenReturn(Optional.of(casper));
        User crash = createUser("crash", "bandicoot");
        when(dao.merge(crash)).thenReturn(crash);
        Response response = whenUpdateUser(userId, crash);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
        verify(dao).merge(crash);
    }

    @Test
    public void testDelete() throws Exception {
        int userId = 12;
        when(dao.find(userId)).thenReturn(Optional.of(casper));
        Response response = whenDeleteUser(userId);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        verify(dao).delete(casper);
    }

    private Response whenDeleteUser(int userId) {
        return resources.client().target("/users/" + userId).request(MediaType.APPLICATION_JSON_TYPE)
                        .delete();
    }

    private Response whenGetAllUsers() {
        return resources.client().target("/users").request(MediaType.APPLICATION_JSON_TYPE)
                        .get();
    }

    private Response whenGetOneUser(int userId) {
        return resources.client().target("/users/" + userId).request(MediaType.APPLICATION_JSON_TYPE)
                        .get();
    }

    private Response whenUpdateUser(int userId, User crash) {
        return resources.client().target("/users/" + userId).request(MediaType.APPLICATION_JSON_TYPE)
                        .put(Entity.entity(crash, MediaType.APPLICATION_JSON_TYPE));
    }

}