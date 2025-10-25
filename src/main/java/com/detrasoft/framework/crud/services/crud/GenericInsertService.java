package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.core.notification.MessageType;
import com.detrasoft.framework.core.resource.Translator;
import com.detrasoft.framework.core.service.GenericService;
import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.EntityValidationException;
import com.detrasoft.framework.enums.CodeMessages;

import org.springframework.transaction.annotation.Transactional;

public class GenericInsertService<Entity extends GenericEntity> extends GenericService {

    protected GenericCRUDRepository<Entity> repository;

    public GenericInsertService(GenericCRUDRepository<Entity> repository) { this.repository = repository; }

    @Transactional
    public Entity insert(Entity entity) {
        clearMessages();
        beforeInsert(entity);
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
