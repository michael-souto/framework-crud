package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

public class GenericUpdateService<Entity extends GenericEntity> {

    protected GenericCRUDRepository<Entity> repository;
    @Transactional
    public Entity update(UUID id, Entity entity) {
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
    public GenericUpdateService(GenericCRUDRepository<Entity> repository) {
        this.repository = repository;
    }
    protected void beforeUpdate(Entity entity) {}
    protected void afterUpdate(Entity entity) {}

}
