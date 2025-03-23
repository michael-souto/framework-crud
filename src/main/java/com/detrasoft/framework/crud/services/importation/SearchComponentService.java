package com.detrasoft.framework.crud.services.importation;

import java.lang.reflect.Field;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;

import org.springframework.stereotype.Service;

import com.detrasoft.framework.crud.entities.GenericEntity;


@Service
public class SearchComponentService {

	@SuppressWarnings("rawtypes")
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
				for (Field field : classe.getDeclaredFields()) {
					if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class)
							|| field.isAnnotationPresent(JoinTable.class)) {

						Column column = field.getAnnotation(Column.class);
						ElementCollection element = field.getAnnotation(ElementCollection.class);
						JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

						String name = "";

						if (column != null && element == null)
							name = column.name();
						else if (joinColumn != null)
							name = joinColumn.name();

						if (name != "")
							select = select + name + ", ";

						field.setAccessible(true);
						Object valueObj = field.get(entitySearch);
						if (valueObj != null) {
							if (valueObj.getClass().equals(String.class)) {
								if (!"".equals(valueObj.toString())) {
									where = where + "lower(" + name + ") like lower('%" + valueObj.toString()
											+ "%') and ";
								}
							} else if (valueObj.getClass().equals(Long.class)) {
								where = where + name + " = " + valueObj.toString() + " and ";
							} else if (valueObj.getClass().equals(Boolean.class)) {
								where = where + name + " = " + valueObj.toString() + " and ";
							} else if (field.getType().getGenericSuperclass() != null
									&& field.getType().getGenericSuperclass().equals(GenericEntity.class)) {

								JoinColumn joinColumnSubClass = field.getAnnotation(JoinColumn.class);
								String columnJoin = joinColumnSubClass.name();

								Class<?> subClass = field.getType();

								if (subClass.isAnnotationPresent(Table.class)) {
									Table table = subClass.getAnnotation(Table.class);
									join = join + "inner join " + table.name() + " on (" + columnJoin + " =";
								}

								for (Field fieldSubClass : subClass.getDeclaredFields()) {

									fieldSubClass.setAccessible(true);

									if (fieldSubClass.isAnnotationPresent(Column.class)) {
										Column columnSubClass = fieldSubClass.getAnnotation(Column.class);

										if (fieldSubClass.isAnnotationPresent(Id.class)) {
											join = join + " " + columnSubClass.name() + ") ";
										}

										Object valueSubClass = fieldSubClass.get(valueObj);
										if (valueSubClass != null) {
											if (valueSubClass.getClass().equals(String.class)) {
												where = where + "lower(" + columnSubClass.name() + ") like lower('%"
														+ valueSubClass.toString() + "%') and ";
											}
											if (valueSubClass.getClass().equals(Long.class)) {
												where = where + columnSubClass.name() + " = " + valueSubClass.toString()
														+ " and ";
											}
										}
									}
								}

							}
							if (field.getType().equals(java.util.List.class)) {

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
											join = join + "left join " + tableRelation + " on (" + inverseColumnList
													+ " = " + fieldSubClass.getAnnotation(Column.class).name() + ") ";
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
				}

				if (!select.equals("")) {
					select = "select " + select.substring(0, select.length() - 2) + " ";
				}

				if (!where.equals("")) {
					where = "where " + where.substring(0, where.length() - 5) + " ";
				}

				// Pegando o nome da tabela da entidade
				if (classe.isAnnotationPresent(Table.class)) {
					Table table = classe.getAnnotation(Table.class);
					from = from + table.name();
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


}
