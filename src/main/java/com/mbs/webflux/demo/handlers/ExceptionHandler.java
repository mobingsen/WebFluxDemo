package com.mbs.webflux.demo.handlers;

import com.mbs.webflux.demo.exceptions.CheckException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class ExceptionHandler implements WebExceptionHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.BAD_REQUEST);
		response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		String errorMsg = toStr(ex);
		DataBuffer db = response.bufferFactory().wrap(errorMsg.getBytes());
		return response.writeWith(Mono.just(db));
	}

	private String toStr(Throwable ex) {
		if (ex instanceof CheckException) {
			CheckException e = (CheckException) ex;
			return e.getFieldName() + ": invalid value " + e.getFieldValue();
		} else {
			ex.printStackTrace();
			return ex.toString();
		}
	}
}
