package com.detrasoft.framework.crud.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface GenericCRUDRepository<T> extends JpaRepository<T, Object>, JpaSpecificationExecutor<T> {
}
