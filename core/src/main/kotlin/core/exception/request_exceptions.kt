package core.exception


class InvalidRequestException(
        override val message: String,
        val code: ReqError? = null,
        vararg val attributes: Pair<String, String>
) : RuntimeException(message)


class ForbiddenException(
        override val message: String,
        val code: ReqError? = null,
        vararg val attributes: Pair<String, String>
) : RuntimeException(message)
