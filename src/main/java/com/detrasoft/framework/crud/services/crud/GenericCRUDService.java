package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.DatabaseException;
import com.detrasoft.framework.crud.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;

@Validated
public class GenericCRUDService<Entity extends GenericEntity> {

    protected GenericCRUDRepository<Entity> repository;

    public GenericCRUDService(GenericCRUDRepository<Entity> repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Entity> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Entity findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public Entity insert(@Valid Entity entity) {
        beforeInsert(entity);
        entity = repository.save(entity);
        repository.flush();
        afterInsert(entity);
        return entity;
    }

    @Transactional
    public Entity update(UUID id,@Valid Entity entity) {
        try {
            Entity entityFinded = repository.getReferenceById(id);
            BeanUtils.copyProperties(entity, entityFinded);
            beforeUpdate(entityFinded);
            entityFinded = repository.save(entityFinded);
            repository.flush();
            afterUpdate(entityFinded);
            return entityFinded;
        }
        catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @Transactional
    public void delete(UUID id) {
        try {
            var entity = findById(id);
            repository.delete(entity);
            repository.flush();
        }
        catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        }
        catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integrity violation" + e.getMessage());
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


    // Insert
    protected void beforeInsert(Entity entity) {}
    protected void afterInsert(Entity entity) {}
    // Update
    protected void beforeUpdate(Entity entity) {}
    protected void afterUpdate(Entity entity) {}
    // Delete
    protected void beforeDelete(Entity entity) {}
    protected void afterDelete(Entity entity) {}
}
