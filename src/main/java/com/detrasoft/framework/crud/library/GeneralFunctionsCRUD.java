package com.detrasoft.framework.crud.library;

import com.detrasoft.framework.crud.entities.GenericEntity;
import org.hibernate.collection.internal.PersistentBag;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GeneralFunctionsCRUD {
    public static boolean checkEmptyOrNull(GenericEntity entity) {
        if (entity != null) {
            Class<?> classe = entity.getClass();

            for (Field field : classe.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (field.getName() != "serialVersionUID") {
                        Object valueObj = field.get(entity);
                        if (valueObj != null) {
                            if (!valueObj.toString().equals("")) {
                                return false;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public static void clearPropertiesEmpty(GenericEntity entity, Boolean clearList, Class mainClass) {
        try {
            Class<?> classe = entity.getClass();
            for (Field field : classe.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class)
                        || field.isAnnotationPresent(OneToMany.class)) {
                    if (field.get(entity) != null) {

                        if (clearList && field.get(entity).getClass() == ArrayList.class) {
                            if (((ArrayList) field.get(entity)).size() == 0) {
                                field.set(entity, null);
                            }
                        } else if (clearList && field.get(entity).getClass() == PersistentBag.class) {
                            if (((PersistentBag) field.get(entity)).size() == 0) {
                                field.set(entity, null);
                            }
                        } else if(field.get(entity).getClass().getSuperclass() == GenericEntity.class
                                && field.get(entity).getClass() != mainClass) {
                            if (checkEmptyOrNull((GenericEntity) field.get(entity))) {
                                field.set(entity, null);
                            } else {
                                clearPropertiesEmpty((GenericEntity) field.get(entity), clearList, mainClass);
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
