package com.mbs.webflux.demo.controller;

import javax.validation.Valid;

import com.mbs.webflux.demo.domain.User;
import com.mbs.webflux.demo.repository.UserRepository;
import com.mbs.webflux.demo.util.CheckUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository repository;

	@GetMapping("/")
	public Flux<User> getAll() {
		return repository.findAll();
	}

	@GetMapping(value = "/stream/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<User> streamGetAll() {
		return repository.findAll();
	}

	@PostMapping("/")
	public Mono<User> createUser(@Valid @RequestBody User user) {
		user.setId(null);
		CheckUtil.checkName(user.getName());
		return this.repository.save(user);
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("id") String id) {
		return this.repository.findById(id)
				.flatMap(user -> this.repository.delete(user).then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<User>> updateUser(@PathVariable("id") String id, @Valid @RequestBody User user) {
		CheckUtil.checkName(user.getName());
		return this.repository.findById(id)
				.flatMap(u -> {
					u.setAge(user.getAge());
					u.setName(user.getName());
					return this.repository.save(u);
				})
				.map(u -> new ResponseEntity<>(u, HttpStatus.OK))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<User>> findUserById(@PathVariable("id") String id) {
		return this.repository.findById(id)
				.map(u -> new ResponseEntity<>(u, HttpStatus.OK))
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/age/{start}/{end}")
	public Flux<User> findByAge(@PathVariable("start") int start, @PathVariable("end") int end) {
		return this.repository.findByAgeBetween(start, end);
	}

	@GetMapping(value = "/stream/age/{start}/{end}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<User> streamFindByAge(@PathVariable("start") int start, @PathVariable("end") int end) {
		return this.repository.findByAgeBetween(start, end);
	}

	@GetMapping("/old")
	public Flux<User> oldUser() {
		return this.repository.oldUser();
	}

	@GetMapping(value = "/stream/old", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<User> streamOldUser() {
		return this.repository.oldUser();
	}
}
