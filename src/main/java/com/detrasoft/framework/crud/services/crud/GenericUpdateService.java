package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.core.notification.MessageType;
import com.detrasoft.framework.core.resource.Translator;
import com.detrasoft.framework.core.service.GenericService;
import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.EntityValidationException;
import com.detrasoft.framework.crud.services.exceptions.ResourceNotFoundException;
import com.detrasoft.framework.enums.CodeMessages;

import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

public class GenericUpdateService<Entity extends GenericEntity> extends GenericService {

    protected GenericCRUDRepository<Entity> repository;
    @Transactional
    public Entity update(UUID id, Entity entity) {
        try {
            clearMessages();
            Entity entityFinded = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
            copyProperties(entity, entityFinded);
            beforeUpdate(entity, entityFinded);
            if (hasFatalError()) {
                throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
            }
            entityFinded = repository.save(entityFinded);
            repository.flush();
            afterUpdate(entityFinded);
            String nameEntity = Translator.getTranslatedText(entity.getClass().getSimpleName(), true);
            addMessageTranslated(CodeMessages.SUCCESS_UPDATING, nameEntity, MessageType.success, nameEntity);
            return entityFinded;
        
        } catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof EntityNotFoundException) {
                throw new ResourceNotFoundException(id);
            }
            throw e;
        }
    }
    public GenericUpdateService(GenericCRUDRepository<Entity> repository) {
        this.repository = repository;
    }
    protected void beforeUpdate(Entity entity, Entity entityFinded) {}
    protected void afterUpdate(Entity entity) {}
    protected void copyProperties(Object source, Object target, String... ignoreProperties){
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
