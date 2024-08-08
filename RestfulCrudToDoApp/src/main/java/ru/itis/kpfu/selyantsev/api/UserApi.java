package ru.itis.kpfu.selyantsev.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "User", description = "Api for performing interaction with User entity")
@RequestMapping(value = "/api/v1/users")
public interface UserApi {

    @Operation(summary = "Create User by UserRequest")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Return users UUID representation",
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
            produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Mono<UUID> createUser(@RequestBody UserRequest userRequest);

    @Operation(summary = "Find User by userId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return UserResponse representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "User Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            produces = APPLICATION_JSON_VALUE,
            value = "/{userId}"
    )
    @ResponseStatus(HttpStatus.OK)
    Mono<UserResponse> findUserById(@PathVariable("userId") UUID userId);

    @Operation(summary = "Find all users")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return Flux<UserResponse> representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
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
    Flux<UserResponse> findAll();

    @Operation(summary = "Update user by userId")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Return Updated UserResponse Representation",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "User Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.PUT,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            value = "/{userId}"
    )
    @ResponseStatus(HttpStatus.OK)
    Mono<UserResponse> updateUserById(@PathVariable UUID userId, @RequestBody UserRequest userRequest);

    @Operation(summary = "Delete user by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User Deleted Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "User Not Found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/{userId}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> deleteUserById(@PathVariable UUID userId);
}
