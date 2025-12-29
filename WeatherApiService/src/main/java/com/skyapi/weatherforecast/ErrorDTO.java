package com.skyapi.weatherforecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ErrorDTO {

	private Date timestamp;
	private int status;
	private String path;
	private List<String> errors = new ArrayList<>();

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getError() {
		return errors;
	}

	public void setError(List<String> error) {
		this.errors = error;
	}


	public void addError(String message) {
		this.errors.add(message);
	}

}
