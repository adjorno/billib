package com.adjorno.billib.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Chart list not found")
public class ChartListNotFoundException extends RuntimeException {
}
