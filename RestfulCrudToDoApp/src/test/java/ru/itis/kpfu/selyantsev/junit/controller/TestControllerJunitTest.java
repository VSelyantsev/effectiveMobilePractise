package ru.itis.kpfu.selyantsev.junit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.controller.TaskController;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.service.TaskService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(TaskController.class)
public class TestControllerJunitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TaskService taskService;

    private static final String API_PATH = "/api/v1/tasks";
    private static final UUID USER_UUID = UUID.fromString("de6b0ea8-002e-42ad-be7b-95abf0efcd3d");
    private static final UUID TASK_UUID = UUID.fromString("9f5e37f7-a0aa-4435-920a-8ff3f1f7b05a");
    private static final TaskRequest TASK_REQUEST = TaskRequest.builder()
            .taskName("testTaskName")
            .build();

    private static final TaskResponse TASK_RESPONSE = TaskResponse.builder()
            .taskId(TASK_UUID)
            .taskName(TASK_REQUEST.getTaskName())
            .userId(USER_UUID)
            .build();

    @Test
    void testCreateTask() {
        when(taskService.create(any(UUID.class), any(TaskRequest.class)))
                .thenReturn(Mono.just(TASK_UUID));

        webTestClient.post().uri(API_PATH + "/{userId}", USER_UUID)
                .contentType(APPLICATION_JSON)
                .bodyValue(TASK_REQUEST)
                .exchange()
                .expectStatus().isCreated();

        verify(taskService, times(1)).create(any(UUID.class), any(TaskRequest.class));
    }

    @Test
    void testFindTaskById() {
        when(taskService.findTaskById(TASK_UUID)).thenReturn(Mono.just(TASK_RESPONSE));

        webTestClient.get().uri(API_PATH + "/{taskId}", TASK_UUID)
                .exchange()
                .expectStatus().isOk();

        verify(taskService, times(1)).findTaskById(TASK_UUID);
    }

    @Test
    void testFindAllTasks() {
        when(taskService.findAll(0, 10)).thenReturn(Flux.just(TASK_RESPONSE));

        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path(API_PATH)
                                .queryParam("page", 0)
                                .queryParam("pageSize", 10)
                                .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(TaskResponse.class)
                .hasSize(1)
                .consumeWith(responseList -> {
                    TaskResponse response = responseList.getResponseBody().get(0);
                    assertAll(
                            () -> assertEquals(TASK_RESPONSE.getTaskId(), response.getTaskId()),
                            () -> assertEquals(TASK_RESPONSE.getTaskName(), response.getTaskName()),
                            () -> assertEquals(TASK_RESPONSE.getUserId(), response.getUserId())
                    );
                });

        verify(taskService, times(1)).findAll(0, 10);
    }

    @Test
    void testFindAllTasksByUserId() {
        when(taskService.findAllTasksByUserId(any(UUID.class))).thenReturn(Flux.just(TASK_RESPONSE));

        webTestClient.get().uri(uriBuilder ->
                uriBuilder.path(API_PATH + "/users")
                        .queryParam("userId", USER_UUID)
                        .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBodyList(TaskResponse.class)
                .hasSize(1)
                .consumeWith(responseList -> {
                    assertEquals(TASK_RESPONSE.getUserId(), responseList.getResponseBody().get(0).getUserId());
                });

        verify(taskService, times(1)).findAllTasksByUserId(any(UUID.class));
    }

    @Test
    void testUpdateTask() {
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        boolean isComplete = true;

        TaskResponse updatedTaskResponse = TaskResponse.builder()
                .taskId(taskId)
                .isComplete(isComplete)
                .taskName("updatedTaskName")
                .userId(userId)
                .build();

        when(taskService.updateTaskCompletionStatus(any(UUID.class), any(UUID.class), anyBoolean()))
                .thenReturn(Mono.just(updatedTaskResponse));

        webTestClient.put().uri(uriBuilder ->
                        uriBuilder.path(API_PATH)
                                .queryParam("userId", userId.toString())
                                .queryParam("taskId", taskId.toString())
                                .queryParam("isComplete", Boolean.toString(isComplete))
                                .build())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertAll(
                            () -> assertEquals(updatedTaskResponse.getTaskId(), taskResponse.getTaskId()),
                            () -> assertEquals(updatedTaskResponse.getTaskName(), taskResponse.getTaskName()),
                            () -> assertEquals(updatedTaskResponse.getUserId(), taskResponse.getUserId()),
                            () -> assertEquals(updatedTaskResponse.isComplete(), taskResponse.isComplete())
                    );
                });

        verify(taskService, times(1))
                .updateTaskCompletionStatus(any(UUID.class), any(UUID.class), anyBoolean());
    }

    @Test
    void deleteTaskByTaskId() {
        when(taskService.deleteTaskById(any(UUID.class))).thenReturn(Mono.empty());

        webTestClient.delete().uri(uriBuilder ->
                uriBuilder.path(API_PATH)
                        .queryParam("taskId", TASK_UUID.toString())
                        .build())
                .exchange()
                .expectStatus().isNoContent();

        verify(taskService, times(1)).deleteTaskById(any(UUID.class));
    }
}
