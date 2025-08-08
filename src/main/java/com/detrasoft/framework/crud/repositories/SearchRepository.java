package com.detrasoft.framework.crud.repositories;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.detrasoft.framework.crud.entities.GenericEntity;

@SuppressWarnings({"null", "unchecked", "rawtypes"})
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

	public List<Object[]> findNativeSQL(String nativeQuery) {
		return this.em.createNativeQuery(nativeQuery).getResultList();
	}

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

	public List<?> findJPQL(String jpql, Class<?> classe) {
		return this.em.createQuery(jpql, classe).getResultList();
	}

	public List<?> findJPQL(String jpql, Class<?> classe, Pageable pageable) {
		Query query = this.em.createQuery(jpql, classe);
		query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		query.setMaxResults(pageable.getPageSize());
		return query.getResultList();
	}

	public int countJPQL(String jpql) {
		String jpqlWithoutOrderBy = jpql;
		int orderByIndex = jpql.toLowerCase().indexOf("order by");
		if (orderByIndex != -1) {
			jpqlWithoutOrderBy = jpql.substring(0, orderByIndex);
		}
		int fromIndex = jpqlWithoutOrderBy.toLowerCase().indexOf("from");
		if (fromIndex == -1) {
			throw new IllegalArgumentException("A query JPQL deve conter a cláusula FROM.");
		}
		
		String countJpql = "select count(e) " + jpqlWithoutOrderBy.substring(fromIndex);
		
		Query query = em.createQuery(countJpql);
		return ((Number) query.getSingleResult()).intValue();
	}

	public String getSQLNativeCommand(Object entitySearch) {

		String select = "", from = "", join = "", where = "", orderBy = "", columnId = "";
		try {
			Class<?> classe = entitySearch.getClass();
			for (Field fieldSubClass : classe.getDeclaredFields()) {
				fieldSubClass.setAccessible(true);
				if (fieldSubClass.isAnnotationPresent(Column.class)) {
					Column columnSubClass = fieldSubClass.getAnnotation(Column.class);
					if (fieldSubClass.isAnnotationPresent(Id.class)) {
						columnId = columnSubClass.name();
					}
				}
			}

			if (classe != null) {
				// Pegando o nome da tabela da entidade
				if (classe.isAnnotationPresent(Table.class)) {
					Table table = classe.getAnnotation(Table.class);
					from = table.name() + " as " + classe.getSimpleName();
				}

				for (Field field : classe.getDeclaredFields()) {
					Column column = field.getAnnotation(Column.class);
					ElementCollection element = field.getAnnotation(ElementCollection.class);
					JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
					field.setAccessible(true);

					String name = "";
					if (!field.getType().equals(java.util.List.class)) {
						if (column != null && element == null)
							name = column.name();
						else if (joinColumn != null)
							name = joinColumn.name();

						if (name == "")
							name = field.getName();

						if (name != "")
							select = select + classe.getSimpleName() + "." + name + ", ";
					}
					Object valueObj = field.get(entitySearch);
					if (valueObj != null) {
						if (valueObj.getClass().equals(String.class)) {
							if (!"".equals(valueObj.toString())) {
								where = where + "lower(" + classe.getSimpleName() + "." + name + ") like lower('%"
										+ valueObj.toString()
										+ "%') and ";
							}
						} else if (valueObj.getClass().equals(Long.class)) {
							where = where + classe.getSimpleName() + "." + name + " = " + valueObj.toString() + " and ";
						} else if (valueObj.getClass().equals(Boolean.class)) {
							where = where + classe.getSimpleName() + "." + name + " = " + valueObj.toString() + " and ";
						} else if (field.getType().getGenericSuperclass() != null
								&& field.getType().getGenericSuperclass().equals(GenericEntity.class)) {

							JoinColumn joinColumnSubClass = field.getAnnotation(JoinColumn.class);
							String columnJoin = joinColumnSubClass.name();

							Class<?> subClass = valueObj.getClass();

							if (subClass.isAnnotationPresent(Table.class)) {
								Table table = subClass.getAnnotation(Table.class);
								join = join + "inner join " + table.name() + " as " + subClass.getSimpleName() + " on ("
										+ classe.getSimpleName() + "." + columnJoin + " =";
							}

							for (Field fieldSubClass : subClass.getDeclaredFields()) {

								fieldSubClass.setAccessible(true);

								Column columnSubClass = fieldSubClass.getAnnotation(Column.class);
								var alias = subClass.getSimpleName();
								if (fieldSubClass.isAnnotationPresent(Id.class)) {
									if (columnSubClass != null) {
										join = join + " " + subClass.getSimpleName() + "." + columnSubClass.name()
												+ ") ";
									} else {
										join = join + " " + subClass.getSimpleName() + "." + fieldSubClass.getName()
												+ ") ";
									}
								}

								Object valueSubClass = fieldSubClass.get(valueObj);
								if (valueSubClass != null) {
									if (valueSubClass.getClass().equals(String.class)) {
										where = where + "lower(" + alias + "." + columnSubClass.name()
												+ ") like lower('%"
												+ valueSubClass.toString() + "%') and ";
									}
									if (valueSubClass.getClass().equals(UUID.class)) {
										where = where + alias + "." + columnSubClass.name() + " = "
												+ valueSubClass.toString()
												+ " and ";
									}
								}

							}

						}
						if (field.isAnnotationPresent(JoinColumn.class)
								&& field.getType().equals(java.util.List.class)) {

							JoinTable joinColumnSubClass = field.getAnnotation(JoinTable.class);
							String tableList = joinColumnSubClass.name();
							String columnList = joinColumnSubClass.joinColumns()[0].name();
							String inverseColumnList = joinColumnSubClass.inverseJoinColumns()[0].name();

							join = join + "left join " + tableList + " on (" + columnList + " = " + columnId + ") ";

							if (((java.util.List) valueObj).size() == 1) {
								Class<?> valueList = ((java.util.List) valueObj).get(0).getClass();
								String tableRelation = "";
								if (valueList.isAnnotationPresent(Table.class)) {
									tableRelation = valueList.getAnnotation(Table.class).name();
								}

								for (Field fieldSubClass : ((java.util.List) valueObj).get(0).getClass()
										.getDeclaredFields()) {
									fieldSubClass.setAccessible(true);

									if (fieldSubClass.isAnnotationPresent(Id.class)) {
										String entityId = fieldSubClass.getName();

										if (fieldSubClass.getAnnotation(Column.class).name() != null) {
											entityId = fieldSubClass.getAnnotation(Column.class).name();
										}
										join = join + "left join " + tableRelation + " on (" + inverseColumnList
												+ " = " + entityId + ") ";
									}

									if (fieldSubClass.isAnnotationPresent(Column.class)) {
										Column columnSearch = fieldSubClass.getAnnotation(Column.class);
										Object valueSubClass = fieldSubClass
												.get(((java.util.List) valueObj).get(0));
										where = where + "lower(" + columnSearch.name() + ") like lower('%"
												+ valueSubClass.toString() + "%') and ";
									}
								}

							}
						}
					}
				}

				if (!select.equals("")) {
					select = "select *";
				}

				if (!where.equals("")) {
					where = "where " + where.substring(0, where.length() - 5) + " ";
				}

				if (!from.equals("")) {
					from = "from " + from + " ";
				}

			}

		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return select + from + join + where + orderBy;

	}

	public String getJPQLCommand(Object entitySearch) {
		String select = "";
		String from = "";
		String join = "";
		String where = "";
		String orderBy = "";

		try {
			Class<?> clazz = entitySearch.getClass();
			// Define um alias padrão para a entidade principal
			String alias = "e";

			// Monta o SELECT JPQL: sempre seleciona a entidade (ou pode ser customizado se
			// necessário)
			select = "select " + alias + " ";

			// Em JPQL usamos o nome da entidade. Se a classe possuir a anotação @Entity com
			// name definido, podemos usá-lo.
			String entityName = clazz.getSimpleName();
			if (clazz.isAnnotationPresent(Entity.class)) {
				Entity entityAnnot = (Entity) clazz.getAnnotation(Entity.class);
				if (!entityAnnot.name().isEmpty()) {
					entityName = entityAnnot.name();
				}
			}
			from = "from " + entityName + " " + alias + " ";

			// Percorre os campos para construir as cláusulas WHERE e JOIN
			for (Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				Object value = field.get(entitySearch);

				if (value != null) {
					// Se o campo for uma coleção (por exemplo, uma associação @OneToMany ou
					// @ManyToMany)
					if (field.getType().equals(java.util.List.class)) {
						// Realiza um left join na coleção usando o nome do atributo
						String joinAlias = field.getName();
						join += " left join " + alias + "." + field.getName() + " " + joinAlias + " ";

						// Se a lista contiver pelo menos um elemento, verifica os atributos deste
						// elemento
						java.util.List list = (java.util.List) value;
						if (!list.isEmpty()) {
							Object listElement = list.get(0);
							Class<?> listElemClass = listElement.getClass();
							for (Field listField : listElemClass.getDeclaredFields()) {
								listField.setAccessible(true);
								Object listFieldValue = listField.get(listElement);
								if (listFieldValue != null) {
									if (listFieldValue instanceof String) {
										where += " and lower(" + joinAlias + "." + listField.getName() + ") = '" + listFieldValue.toString().toLowerCase() + "' ";
									} 
									else {
										where += " and " + joinAlias + "." + listField.getName() + " = "
												+ listFieldValue.toString() + " ";
									}
								}
							}
						}
					}
					// Se o campo for um relacionamento para outra entidade
					else if (field.getType().isAnnotationPresent(Entity.class)) {
						// Realiza join na associação
						String joinAlias = field.getName();
						join += " left join " + alias + "." + field.getName() + " " + joinAlias + " ";

						// Itera sobre os atributos da entidade associada para incluir condições se
						// houver valores
						Class<?> assocClass = field.getType();
						for (Field assocField : assocClass.getDeclaredFields()) {
							assocField.setAccessible(true);
							Object assocValue = assocField.get(value);
							if (assocValue != null) {
								if (assocValue instanceof String) {
									where += " and lower(" + joinAlias + "." + assocField.getName() + ") = '" + assocValue.toString().toLowerCase() + "' ";
								} 
								// else {
								// 	where += " and " + joinAlias + "." + assocField.getName() + " = "
								// 			+ assocValue.toString() + " ";
								// }
							}
						}
					}
					// Se for um atributo simples (básico)
					else {
						if (value instanceof String) {
							if (!((String) value).isEmpty()) {
								where += " and lower(" + alias + "." + field.getName() + ") = '" + value.toString().toLowerCase() + "' ";
							}
						} else if (value instanceof Number || value instanceof Boolean) {
							where += " and " + alias + "." + field.getName() + " = " + value.toString() + " ";
						}
					}
				}
			}

			// Ajusta a cláusula WHERE, removendo o primeiro "and"
			if (!where.isEmpty()) {
				where = "where " + where.substring(5) + " ";
			}

			// Monta a query final
			return select + from + join + where + orderBy;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
