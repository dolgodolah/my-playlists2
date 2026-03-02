package com.garfield.myplaylists.usecase

class ForbiddenException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String) : RuntimeException(message)
