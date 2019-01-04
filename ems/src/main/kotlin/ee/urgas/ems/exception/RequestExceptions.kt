package ee.urgas.ems.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidRequestException(override val message: String) : RuntimeException(message)
