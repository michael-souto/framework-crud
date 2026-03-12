package com.detrasoft.framework.crud.services.crud;

import java.util.Set;

import com.detrasoft.framework.core.notification.MessageType;
import com.detrasoft.framework.core.resource.Translator;
import com.detrasoft.framework.core.service.GenericService;
import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.EntityValidationException;
import com.detrasoft.framework.enums.CodeMessages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class GenericInsertService<Entity extends GenericEntity> extends GenericService {

    protected GenericCRUDRepository<Entity> repository;

    public GenericInsertService(GenericCRUDRepository<Entity> repository) { this.repository = repository; }
	
    @Autowired
	private Validator validator;

    @Transactional
    public Entity insert(Entity entity) {
        clearMessages();
        beforeInsert(entity);

        Set<ConstraintViolation<Entity>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        if (hasFatalError()) {
            throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
        }
        entity = repository.save(entity);
        repository.flush();
        afterInsert(entity);
        generateMessage(entity, CodeMessages.SUCCESS_INSERTING);
        return entity;
    }

    protected void generateMessage(Entity entity, CodeMessages code){
        String nameEntity = Translator.getTranslatedText(entity.getClass().getSimpleName(), true);
        addMessageTranslated(code, nameEntity, MessageType.success, nameEntity);
    }

    // Insert
    protected void beforeInsert(Entity entity) {}
    protected void afterInsert(Entity entity) {}

}
