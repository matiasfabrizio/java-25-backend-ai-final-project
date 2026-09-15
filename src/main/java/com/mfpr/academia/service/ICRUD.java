package com.mfpr.academia.service;

import java.util.List;

/**
 * Generic interface for services that will later be implemented through 'EntityImplementation'.
 * @param <T>   // Entity Type
 * @param <ID>  // ID of the Entity
 */
public interface ICRUD<T, ID> {
    T save(T t);
    T update(ID id, T t) throws Exception;
    List<T> findAll();
    T findById(ID id);
    void delete(ID id);
}
