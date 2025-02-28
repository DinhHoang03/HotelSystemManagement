package com.humg.HotelSystemManagement.service.HumanService;

import java.util.List;

public interface IGeneralHumanCRUDService<T, U, Y> {
    public T create(U request);

    public List<T> getAll();

    public T getById(Long id);

    public T updateById(Long id, Y request);

    public void deleteById(Long id);
}
