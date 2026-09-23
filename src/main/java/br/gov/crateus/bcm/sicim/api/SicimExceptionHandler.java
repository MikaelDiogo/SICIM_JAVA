package br.gov.crateus.bcm.sicim.api;

import br.gov.crateus.bcm.sicim.domain.exception.SicimDomainException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Erros do módulo em Problem Details (RFC 7807). Inclui a propriedade {@code message} para
 * manter compatível o extractErrorMessage da SPA herdada do NestJS.
 */
@RestControllerAdvice(basePackages = "br.gov.crateus.bcm.sicim")
public class SicimExceptionHandler {

	private static final String TYPE_BASE = "https://api.pontodatec.com.br/problems/sicim/";

	@ExceptionHandler(SicimDomainException.class)
	ProblemDetail domain(SicimDomainException ex) {
		HttpStatus status = switch (ex.getType()) {
			case NOT_FOUND -> HttpStatus.NOT_FOUND;
			case CONFLICT -> HttpStatus.CONFLICT;
			case VALIDATION -> HttpStatus.BAD_REQUEST;
		};
		return problem(status, ex.getType().name().toLowerCase().replace('_', '-'), ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail invalidBody(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + " " + e.getDefaultMessage())
				.toList();
		ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation", String.join("; ", errors));
		pd.setProperty("errors", errors);
		return pd;
	}

	@ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
			MethodArgumentTypeMismatchException.class})
	ProblemDetail badRequest(Exception ex) {
		return problem(HttpStatus.BAD_REQUEST, "validation", "Invalid request parameters.");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail unreadable(HttpMessageNotReadableException ex) {
		return problem(HttpStatus.BAD_REQUEST, "validation", "Malformed JSON body or invalid field value.");
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	ProblemDetail optimisticLock(OptimisticLockingFailureException ex) {
		return problem(HttpStatus.CONFLICT, "concurrent-update",
				"The property was changed by another user. Reload and try again.");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail integrity(DataIntegrityViolationException ex) {
		return problem(HttpStatus.CONFLICT, "conflict", "The operation violates a data integrity rule.");
	}

	private static ProblemDetail problem(HttpStatus status, String type, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setType(URI.create(TYPE_BASE + type));
		pd.setTitle(status.getReasonPhrase());
		pd.setProperty("message", detail);
		return pd;
	}
}
