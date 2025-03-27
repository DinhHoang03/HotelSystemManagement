package com.humg.HotelSystemManagement.service;

import org.springframework.data.domain.Page;

import java.util.List;

public interface IGeneralCRUDService<T, U, Y, O> {
    public T create(U request);

    public Page<T> getAll(int page, int size);

    public T getById(O id);

    public T update(O id, Y request);

    public void delete(O id);
}
