package com.mfpr.academia.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IGenericRepo<T, ID> extends JpaRepository<T, ID> {
}
