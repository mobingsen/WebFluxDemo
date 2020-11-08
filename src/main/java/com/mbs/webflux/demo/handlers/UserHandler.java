package com.mbs.webflux.demo.handlers;

import com.mbs.webflux.demo.domain.User;
import com.mbs.webflux.demo.repository.UserRepository;
import com.mbs.webflux.demo.util.CheckUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
@RequiredArgsConstructor
public class UserHandler {

	private final UserRepository repository;

	public Mono<ServerResponse> getAllUser(ServerRequest request) {
		return ok().contentType(APPLICATION_JSON).body(this.repository.findAll(), User.class);
	}

	public Mono<ServerResponse> createUser(ServerRequest request) {
		Mono<User> user = request.bodyToMono(User.class);
		return user.flatMap(u -> {
			CheckUtil.checkName(u.getName());
			return ok().contentType(APPLICATION_JSON).body(this.repository.save(u), User.class);
		});
	}

	public Mono<ServerResponse> deleteUserById(ServerRequest request) {
		String id = request.pathVariable("id");
		return this.repository.findById(id)
				.flatMap(user -> this.repository.delete(user).then(ok().build()))
				.switchIfEmpty(notFound().build());
	}
}
