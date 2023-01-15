package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import org.springframework.transaction.annotation.Transactional;

public class GenericInsertService<Entity extends GenericEntity>{

    protected GenericCRUDRepository<Entity> repository;

    public GenericInsertService(GenericCRUDRepository<Entity> repository) { this.repository = repository; }

    @Transactional
    public Entity insert(Entity entity) {
        beforeInsert(entity);
        entity = repository.save(entity);
        repository.flush();
        afterInsert(entity);
        return entity;
    }
    // Insert
    protected void beforeInsert(Entity entity) {}
    protected void afterInsert(Entity entity) {}

}
