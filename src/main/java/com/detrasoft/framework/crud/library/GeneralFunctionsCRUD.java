package com.detrasoft.framework.crud.library;

import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.services.crud.GenericCRUDService;
import org.hibernate.collection.spi.PersistentBag;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;

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

    public static <T extends GenericEntity> void mergeLists(
            List<T> source,
            List<T> target,
            GenericCRUDService<T> service,
            BiConsumer<T, T> copyProperties,
            Consumer<T> newEntityInitializer) {

        Map<UUID, T> targetMap = target.stream()
                .filter(t -> t.getId() != null)
                .collect(Collectors.toMap(GenericEntity::getId, t -> t));

        List<T> mergedList = new ArrayList<>();

        for (T src : source) {
            UUID id = src.getId();
            if (id == null) {
                newEntityInitializer.accept(src);
                mergedList.add(src);
            } else {
                T tgt = targetMap.get(id);
                if (tgt == null) {
                    T managed = service.findById(id);
                    copyProperties.accept(src, managed);
                    mergedList.add(managed);
                } else {
                    copyProperties.accept(src, tgt);
                    mergedList.add(tgt);
                }
            }
        }
        target.clear();
        target.addAll(mergedList);
    }

    public static <T extends GenericEntity> void mergeListsSimple(
            List<T> source,
            List<T> target,
            GenericCRUDService<T> service,
            String... ignoreProperties) {

        mergeLists(
                source,
                target,
                service,
                (src, tgt) -> BeanUtils.copyProperties(src, tgt, ignoreProperties),
                t -> {
                }
        );
    }
}
