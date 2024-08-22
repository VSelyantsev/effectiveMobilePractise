package ru.itis.kpfu.selyantsev.junit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.controller.UserController;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.service.UserService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(UserController.class)
public class UserControllerJunitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private UserService userService;

    private static final String API_PATH = "/api/v1/users";
    private static final UUID USER_UUID = UUID.fromString("de6b0ea8-002e-42ad-be7b-95abf0efcd3d");
    private static final UserRequest USER_REQUEST = UserRequest.builder()
            .firstName("Vlad1")
            .lastName("LastName2")
            .build();

    private static final User USER_ENTITY = User.builder()
            .userId(USER_UUID)
            .firstName(USER_REQUEST.getFirstName())
            .lastName(USER_REQUEST.getLastName())
            .build();

    private static final UserResponse USER_RESPONSE = UserResponse.builder()
            .userId(USER_ENTITY.getUserId())
            .firstName(USER_ENTITY.getFirstName())
            .lastName(USER_ENTITY.getLastName())
            .build();

    @Test
    void testCreate() {
        when(userService.create(any(UserRequest.class)))
                .thenReturn(Mono.just(USER_UUID));

        webTestClient.post().uri(API_PATH)
                .contentType(APPLICATION_JSON)
                .bodyValue(USER_REQUEST)
                .exchange()
                .expectStatus().isCreated();

        verify(userService, times(1)).create(any(UserRequest.class));
    }

    @Test
    void testFindUserByValidUUID() {
        when(userService.findUserById(USER_UUID)).thenReturn(Mono.just(USER_RESPONSE));

        webTestClient.get().uri(API_PATH + "/{userId}", USER_UUID)
                .exchange()
                .expectStatus().isOk();

        verify(userService, times(1)).findUserById(USER_UUID);
    }

    @Test
    void testFindAll() {
        when(userService.findAll(0, 10)).thenReturn(Flux.just(USER_RESPONSE));

        webTestClient.get().uri(uriBuilder ->
                    uriBuilder.path(API_PATH)
                            .queryParam("page", 0)
                            .queryParam("pageSize", 10)
                            .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(UserResponse.class)
                .hasSize(1)
                .consumeWith(responseList -> {
                    UserResponse response = responseList.getResponseBody().get(0);
                    assertAll(
                            () -> assertEquals(USER_RESPONSE.getUserId(), response.getUserId()),
                            () -> assertEquals(USER_RESPONSE.getFirstName(), response.getFirstName()),
                            () -> assertEquals(USER_RESPONSE.getLastName(), response.getLastName())
                    );
                });

        verify(userService, times(1)).findAll(0, 10);
    }

    @Test
    void testUpdateUserById() {
        when(userService.updateUserById(any(UUID.class), any(UserRequest.class)))
                .thenReturn(Mono.just(USER_RESPONSE));

        webTestClient.put().uri(API_PATH + "/{userId}", USER_UUID)
                .contentType(APPLICATION_JSON)
                .bodyValue(USER_REQUEST)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertAll(
                            () -> assertEquals(USER_RESPONSE.getUserId(), response.getUserId()),
                            () -> assertEquals(USER_RESPONSE.getFirstName(), response.getFirstName()),
                            () -> assertEquals(USER_RESPONSE.getLastName(), response.getLastName())
                    );
                });

        verify(userService, times(1)).updateUserById(any(UUID.class), any(UserRequest.class));
    }

    @Test
    void testDeleteUserById() {
        when(userService.deleteUserById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.delete().uri(API_PATH + "/{userId}", USER_UUID)
                .exchange()
                .expectStatus().isNoContent();

        verify(userService, times(1)).deleteUserById(any(UUID.class));
    }
}
