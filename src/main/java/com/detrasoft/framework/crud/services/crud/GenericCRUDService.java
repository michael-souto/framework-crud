package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.core.library.GeneralFunctionsCore;
import com.detrasoft.framework.core.notification.Message;
import com.detrasoft.framework.core.notification.MessageType;
import com.detrasoft.framework.core.resource.Translator;
import com.detrasoft.framework.core.service.GenericService;
import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.DatabaseException;
import com.detrasoft.framework.crud.services.exceptions.ResourceNotFoundException;
import com.detrasoft.framework.enums.CodeMessages;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Validated
public class GenericCRUDService<Entity extends GenericEntity> extends GenericService {

    protected GenericCRUDRepository<Entity> repository;

    public GenericCRUDService(GenericCRUDRepository<Entity> repository) {
        this.repository = repository;
    }

    protected List<Message> messages = new ArrayList<>();

    @Transactional(readOnly = true)
    public Page<Entity> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Entity> findAllPaged(Specification<Entity> specs, Pageable pageable) {
        return repository.findAll(specs, pageable);
    }

    @Transactional(readOnly = true)
    public List<Entity> findAll(Specification<Entity> specs) {
        return repository.findAll(specs);
    }

    @Transactional(readOnly = true)
    public Entity findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Entity findByOne(Specification<Entity> specs) {
        return repository.findOne(specs).get();
    }

    @Transactional
    public Entity insert(@Valid Entity entity) {
        clearMessages();
        beforeInsert(entity);
        entity = repository.save(entity);
        repository.flush();
        afterInsert(entity);
        generateMessage(entity, CodeMessages.SUCCESS_INSERTING);
        return entity;
    }

    @Transactional
    public Entity update(UUID id,@Valid Entity entity) {
        try {
            clearMessages();
            beforeInitUpdate(entity);
            Entity entityFinded = findById(id);
            copyProperties(entity, entityFinded);
            beforeUpdate(entityFinded);
            entityFinded = repository.save(entityFinded);
            repository.flush();
            afterUpdate(entityFinded);
            generateMessage(entity, CodeMessages.SUCCESS_UPDATING);
            return entityFinded;
        } catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof EntityNotFoundException) {
                throw new ResourceNotFoundException(id);
            }
            throw e;
        }
    }

    @Transactional
    public void delete(UUID id) {
        try {
            clearMessages();
            var entity = findById(id);
            repository.delete(entity);
            repository.flush();
            generateMessage(entity, CodeMessages.SUCCESS_DELETING);
        }
        catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation" + e.getMessage());
        } 
        catch (Exception e) {
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof EntityNotFoundException) {
                throw new ResourceNotFoundException(id);
            }
            throw e;
        }
    }

    public Entity newInstanceEntity() {
        Entity entity = null;
        try {
            entity = (Entity) ((Class) ((ParameterizedType) this.getClass().
                    getGenericSuperclass()).getActualTypeArguments()[0]).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
    
    protected void generateMessage(Entity entity, CodeMessages code){
        String nameEntity = Translator.getTranslatedText(GeneralFunctionsCore.formatCamelCase(entity.getClass().getSimpleName()));
        addMessageTranslated(code, nameEntity, MessageType.success, nameEntity);
    }

    // Insert
    protected void beforeInsert(Entity entity) {}
    protected void afterInsert(Entity entity) {}
    // Update
    protected void beforeUpdate(Entity entity) {}
    protected void beforeInitUpdate(Entity entity) {}
    protected void afterUpdate(Entity entity) {}
    // Delete
    protected void beforeDelete(Entity entity) {}
    protected void afterDelete(Entity entity) {}

    protected void copyProperties(Object source, Object target, String... ignoreProperties){
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
