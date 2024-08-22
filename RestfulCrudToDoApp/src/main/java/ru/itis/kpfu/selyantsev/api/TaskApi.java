package ru.itis.kpfu.selyantsev.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@Tag(name = "Task", description = "Api for performing interaction with Task Entity")
@RequestMapping(value = "/api/v1/tasks")
public interface TaskApi {

    @Operation(summary = "Create Task by TaskRequest")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Return tasks UUID representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UUID.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            value = "{userId}"
    )
    @ResponseStatus(HttpStatus.CREATED)
    Mono<UUID> create(@PathVariable UUID userId, @Valid @RequestBody TaskRequest task);

    @Operation(summary = "Find Task By taskId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return TaskResponse representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE,
            value = "{taskId}"
    )
    @ResponseStatus(HttpStatus.OK)
    Mono<TaskResponse> findTaskById(@PathVariable UUID taskId);

    @Operation(summary = "Find all tasks")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return Flux<TaskResponse> representation in page",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Flux<TaskResponse> findAll(
            @RequestParam int page,
            @RequestParam int pageSize
    );

    @Operation(summary = "Find all User tasks By userId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return User Tasks Representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/users",
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Flux<TaskResponse> findAllTasksByUserId(
            @RequestParam UUID userId
    );

    @Operation(summary = "Update task execution status")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return Updated TaskResponse Representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.PUT,
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Mono<TaskResponse> updateTaskCompletionStatus(
            @RequestParam UUID userId,
            @RequestParam UUID taskId,
            @RequestParam boolean isComplete
    );

    @Operation(summary = "Delete task by taskId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task Deleted Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Task Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.DELETE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> deleteTaskByTaskId(@RequestParam UUID taskId);
}
