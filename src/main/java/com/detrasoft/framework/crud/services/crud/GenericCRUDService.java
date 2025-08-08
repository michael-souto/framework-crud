package com.detrasoft.framework.crud.services.crud;

import com.detrasoft.framework.core.library.GeneralFunctionsCore;
import com.detrasoft.framework.core.notification.Message;
import com.detrasoft.framework.core.notification.MessageType;
import com.detrasoft.framework.core.resource.Translator;
import com.detrasoft.framework.core.service.GenericService;
import com.detrasoft.framework.crud.dtos.RequestImportDTO;
import com.detrasoft.framework.crud.entities.GenericEntity;
import com.detrasoft.framework.crud.repositories.GenericCRUDRepository;
import com.detrasoft.framework.crud.repositories.SearchRepository;
import com.detrasoft.framework.crud.services.exceptions.DatabaseException;
import com.detrasoft.framework.crud.services.exceptions.EntityValidationException;
import com.detrasoft.framework.crud.services.exceptions.ResourceNotFoundException;
import com.detrasoft.framework.crud.services.exceptions.IdentifierNotProvidedForUpgrading;
import com.detrasoft.framework.enums.CodeMessages;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"rawtypes", "unchecked"})
@Validated
public class GenericCRUDService<Entity extends GenericEntity> extends GenericService {

    protected GenericCRUDRepository<Entity> repository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
	protected SearchRepository searchRepository;
    
    public GenericCRUDService(GenericCRUDRepository<Entity> repository) {
        this.repository = repository;
    }

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
		if (hasFatalError()) {
			throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
		}
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
			if (hasFatalError()) {
				throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
			}
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
    public Entity update(@Valid Entity entity) {
        try {
            if (entity.getId() == null) {
                throw new IdentifierNotProvidedForUpgrading(entity.getId());
            }
            clearMessages();
            beforeInitUpdate(entity);
            beforeUpdate(entity);
			if (hasFatalError()) {
				throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
			}
            entity = repository.save(entity);
            repository.flush();
            afterUpdate(entity);
            generateMessage(entity, CodeMessages.SUCCESS_UPDATING);
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = getRootCause(e);
            if (rootCause instanceof EntityNotFoundException) {
                throw new ResourceNotFoundException(entity.getId());
            }
            throw e;
        }
    }

    @Transactional
    public void delete(UUID id) {
        try {
			clearMessages();
            var entity = findById(id);
			beforeDelete(entity);
			if (hasFatalError()) {
				throw new EntityValidationException(Translator.getTranslatedText("error.validation_exception"), getMessages());
			}
            repository.delete(entity);
            repository.flush();
            afterDelete(entity);
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

    @Transactional
    public void delete(List<Entity> entities) {
        entities.forEach(entity -> {
            try {
                delete(entity.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

	public List<Entity> importList(List<Entity> entities, RequestImportDTO<Entity> requestImport) {
		List<Entity> listResult = new ArrayList<>();
		var messagesProcess = new ArrayList<Message>();
		
		for (int index = 0; index < entities.size(); index++) {
			Entity entity = entities.get(index);
			try {
				Entity processedEntity = processEntity(entity, requestImport, index);
				if (processedEntity != null) {
					listResult.add(processedEntity);
				}
			} catch (IllegalArgumentException | SecurityException e) {
				handleProcessingError(e, index, messagesProcess);
			}
		}
		
		this.messages.addAll(messagesProcess);
		return listResult;
	}
	
	private Entity processEntity(Entity entity, RequestImportDTO<Entity> requestImport, int index) {
		RequestImportDTO.Operation operation = requestImport.getOperation();
		
		switch (operation) {
			case CREATE:
				return insert(entity);
				
			case UPDATE:
			case CREATE_OR_UPDATE:
				return processUpdateOrCreateUpdate(entity, requestImport, index);
				
			default:
				return null;
		}
	}
	
	private Entity processUpdateOrCreateUpdate(Entity entity, RequestImportDTO<Entity> requestImport, int index) {
		try {
			if (!hasValidKeys(requestImport)) {
				return handleNoKeys(entity, requestImport);
			}
			
			Entity existingEntity = findExistingEntity(entity, requestImport);
			
			if (existingEntity != null) {
				return updateExistingEntity(entity, existingEntity, requestImport);
			} else {
				return handleEntityNotFound(entity, requestImport);
			}
			
		} catch (Exception e) {
			handleProcessingError(e, index, new ArrayList<>());
			return null;
		}
	}
	
	private boolean hasValidKeys(RequestImportDTO<Entity> requestImport) {
		return requestImport.getKeys() != null && !requestImport.getKeys().isEmpty();
	}
	
	private Entity handleNoKeys(Entity entity, RequestImportDTO<Entity> requestImport) {
		if (requestImport.getOperation().equals(RequestImportDTO.Operation.CREATE_OR_UPDATE)) {
			setComplementField(entity, requestImport);
			return insert(entity);
		}
		return null;
	}
	
	private Entity findExistingEntity(Entity entity, RequestImportDTO<Entity> requestImport) {
		Entity entitySearched = createSearchEntity();
		copyObject(entity, entitySearched, requestImport.getKeys());
		
		var jpqlKey = searchRepository.getJPQLCommand(entitySearched);
		var jpqlEmpty = searchRepository.getJPQLCommand(createSearchEntity());
		
		if (jpqlKey.equals(jpqlEmpty)) {
			return null; // Não há diferença na consulta, entidade não existe
		}
		
		var foundEntities = this.searchRepository.findJPQL(jpqlKey, getGenericClass());
		return (foundEntities != null && !foundEntities.isEmpty()) ? 
			   (Entity) foundEntities.get(0) : null;
	}
	
	private Entity createSearchEntity() {
		Entity entitySearched = (Entity) instantiateRootClass(null);
		instantiateProperties(entitySearched);
		return entitySearched;
	}
	
	private Entity updateExistingEntity(Entity entity, Entity existingEntity, RequestImportDTO<Entity> requestImport) {
		copyObject(entity, existingEntity);
		setComplementField(existingEntity, requestImport);
		return update(existingEntity.getId(), existingEntity);
	}
	
	private Entity handleEntityNotFound(Entity entity, RequestImportDTO<Entity> requestImport) {
		if (requestImport.getOperation().equals(RequestImportDTO.Operation.CREATE_OR_UPDATE)) {
			setComplementField(entity, requestImport);
			return insert(entity);
		}
		return null;
	}
	
	private void handleProcessingError(Exception e, int index, List<Message> messagesProcess) {
		e.printStackTrace();
		messagesProcess.add(Message.builder()
			.type(MessageType.error)
			.code(String.valueOf(index + 1))
			.description(e.getMessage())
			.build());
	}
	

	protected void setComplementField(Entity entitySaved, RequestImportDTO<Entity> requestImport) {
		if (requestImport.getComplementFieldName() != null && !requestImport.getComplementFieldName().isEmpty()) {
			try {
				String fieldName = requestImport.getComplementFieldName();
				
				// O valor que vem como um LinkedHashMap
				Object mapValue = requestImport.getComplementFieldValue();

				// Verifica se o valor é de fato um Map
				if (mapValue instanceof LinkedHashMap) {
					Field field = entitySaved.getClass().getDeclaredField(fieldName);
					
					// Pega o TIPO do campo da entidade (ex: ComplementoDTO.class)
					Class<?> fieldType = field.getType();
					
					// Converte o Map para um objeto do tipo do campo
					Object targetObject = objectMapper.convertValue(mapValue, fieldType);
					
					field.setAccessible(true);
					field.set(entitySaved, targetObject);
				} else {
				   // Lógica antiga para valores simples (se necessário)
				   Field field = entitySaved.getClass().getDeclaredField(fieldName);
				   field.setAccessible(true);
				   field.set(entitySaved, mapValue);
				}

			} catch (Exception e) { // Captura Exception mais genérica aqui
				throw new IllegalArgumentException("Falha ao processar campo complementar '" + requestImport.getComplementFieldName() + "': " + e.getMessage(), e);
			}
		}
	}
	


	protected Class<?> getGenericClass() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		return ((Class<Entity>) (type).getActualTypeArguments()[0]);
	}

	protected Object instantiateRootClass(Class<?> newClass) {
		try {
			if (newClass == null) {
				return (Entity) getGenericClass().getDeclaredConstructor().newInstance();
			}
			return newClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void instantiateProperties(Object entity) {
		try {
			Class<?> clazz = entity.getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(JsonIgnore.class)) {
					continue;
				}
				
				field.setAccessible(true);
				Object fieldValue = field.get(entity);
				if (field.getType().getGenericSuperclass() != null
						&& field.getType().getGenericSuperclass().equals(GenericEntity.class)) {
					Object nestedEntity = instantiateRootClass(field.getType());
					field.set(entity, nestedEntity);
					instantiateProperties(nestedEntity);
				}

				if (field.getType().equals(java.util.List.class)) {
					List<Object> list = (List<Object>) fieldValue;
					list = new ArrayList<>();
					field.set(entity, list);
				}
				field.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void copyObject(Object source, Object destination) {
		if (source == null || destination == null) {
			throw new IllegalArgumentException("The parameters cannot be null");
		}

		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);

				Object sourceValue = field.get(source);
				Object destinationValue = field.get(destination);

				if (sourceValue != null) {
					if (sourceValue instanceof List) {
						List<Object> sourceList = (List<Object>) sourceValue;
						List<Object> destinationList = (List<Object>) destinationValue;

						if (sourceList != null && destinationList != null) {
							if (sourceList.size() == 1 && destinationList.size() == 1) {
								copyObject(sourceList.get(0), destinationList.get(0));
							} else {
								destinationList.clear();
								for (Object item : sourceList) {
									destinationList.add(item);
								}
							}
						}
					} else if (sourceValue instanceof GenericEntity) {
						Object newDestination = field.get(destination);
						if (newDestination != null && (((GenericEntity) newDestination).getId() != null
								&& ((GenericEntity) sourceValue).getId() == null)) {
							copyObject(sourceValue, newDestination);
						} else {
							field.set(destination, sourceValue);
						}
					} else {
						field.set(destination, sourceValue);
					}
				} else if (destinationValue != null && (sourceValue != destinationValue)) {
					field.set(destination, destinationValue);
				}

				field.setAccessible(false);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void copyObject(Object source, Object destination, List<String> propertyNames) {
		if (source == null || destination == null) {
			throw new IllegalArgumentException("The parameters cannot be null");
		}

		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				String fieldName = source.getClass().getSimpleName().toLowerCase() + "." + field.getName();

				Object sourceValue = field.get(source);
				Object destinationValue = field.get(destination);

				if (sourceValue != null) {
					if (sourceValue instanceof List) {
						List<Object> sourceList = (List<Object>) sourceValue;
						List<Object> destinationList = (List<Object>) destinationValue;

						if (sourceList != null && destinationList != null) {
							if (sourceList.size() == 1 && destinationList.size() == 1) {
								copyObject(sourceList.get(0), destinationList.get(0), propertyNames);
							} else {
								if (!propertyNames.isEmpty() && !propertyNames.contains(fieldName)) {
									continue;
								}
								destinationList.clear();
								for (Object item : sourceList) {
									destinationList.add(item);
								}
							}
						}
					} else if (sourceValue instanceof GenericEntity) {
						Object newDestination = field.get(destination);
						if (newDestination != null && (((GenericEntity) newDestination).getId() != null
								&& ((GenericEntity) sourceValue).getId() == null)) {
							copyObject(sourceValue, newDestination);
						} else {
							field.set(destination, sourceValue);
						}
					} else {
						if (!propertyNames.isEmpty() && !propertyNames.contains(fieldName)) {
							continue; 
						}
						field.set(destination, sourceValue);
					}
				} else if (destinationValue != null && (sourceValue != destinationValue)) {
					if (!propertyNames.isEmpty() && !propertyNames.contains(fieldName)) {
						continue; 
					}
					field.set(destination, destinationValue);
				}

				field.setAccessible(false);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
