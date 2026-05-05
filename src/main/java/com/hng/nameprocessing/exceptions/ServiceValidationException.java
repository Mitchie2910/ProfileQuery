package com.hng.nameprocessing.exceptions;

import lombok.Getter;

@Getter
public class ServiceValidationException extends RuntimeException {
  private final int code;
  private final String message;

  public ServiceValidationException(String message, int code) {
    super(message);
    this.code = code;
    this.message = message;
  }
}
