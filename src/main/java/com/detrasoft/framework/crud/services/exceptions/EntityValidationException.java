package com.detrasoft.framework.crud.services.exceptions;

import java.util.List;

import com.detrasoft.framework.core.notification.Message;

public class EntityValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private List<Message> messages;

	public EntityValidationException(String msg, List<Message> messages) {
		super(msg);
		this.messages = messages;
	}

	public List<Message> getMessages() {
		return messages;
	}

}
