package com.mfpr.academia.service.impl;

import com.mfpr.academia.exception.ModelNotFoundException;
import com.mfpr.academia.repository.IGenericRepo;
import com.mfpr.academia.service.ICRUD;

import java.lang.reflect.Method;
import java.util.List;

public abstract class CRUDImpl<T, ID> implements ICRUD<T, ID> {

    protected abstract IGenericRepo<T, ID> getRepo();

    @Override
    public T save(T t) {
        return getRepo().save(t);
    }

    @Override
    public T update(ID id, T t) throws Exception {
        // Validate that it exists
        T _ = findById(id);

        // Create the Method Java Object (entities use a primitive int id, per convention)
        Method setIdMethod = t.getClass().getMethod("setId", int.class);

        // Invoke it to perform the update
        setIdMethod.invoke(t, id);

        return getRepo().save(t);
    }

    @Override
    public List<T> findAll() {
        return getRepo().findAll();
    }

    @Override
    public T findById(ID id) {
        return getRepo().findById(id)
                .orElseThrow(() -> new ModelNotFoundException("No entity found with id: " + id));
    }

    @Override
    public void delete(ID id) {
        getRepo().deleteById(id);
    }
}
