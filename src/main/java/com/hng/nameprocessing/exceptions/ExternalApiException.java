package com.hng.nameprocessing.exceptions;

public class ExternalApiException extends RuntimeException {
  private final int code;
  private final String message;

  public ExternalApiException(String message, int code) {
    super(message);
    this.code = code;
    this.message = message;
  }
}
