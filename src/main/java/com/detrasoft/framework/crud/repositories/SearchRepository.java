package com.detrasoft.framework.crud.repositories;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class SearchRepository {

	@Autowired
	EntityManager em;

	public List<?> findNativeSQL(String nativeQuery, Class<?> classe) {
		return this.em.createNativeQuery(nativeQuery, classe).getResultList();
	}

	public List<?> findNativeSQL(String nativeQuery, Class<?> classe, Pageable pageable) {
		nativeQuery = nativeQuery + " LIMIT " + pageable.getPageSize() + " OFFSET "
				+ pageable.getPageNumber() * pageable.getPageSize();
		return this.em.createNativeQuery(nativeQuery, classe).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findNativeSQL(String nativeQuery) {
		return this.em.createNativeQuery(nativeQuery).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findNativeSQL(String nativeQuery, Pageable pageable) {
		nativeQuery = nativeQuery + " LIMIT " + pageable.getPageSize() + " OFFSET "
				+ pageable.getPageNumber() * pageable.getPageSize();
		return this.em.createNativeQuery(nativeQuery).getResultList();
	}

	public int countNativeSQL(String nativeQuery) {
		String queryCount = "SELECT COUNT(*) FROM ( " + nativeQuery + ") B";
		Query query = em.createNativeQuery(queryCount);
		return ((Number) query.getSingleResult()).intValue();
	}
}
