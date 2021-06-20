package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

@Service
public class StateService {
    @Autowired
    private StateDao stateDAO;

    @Transactional
    public StateEntity getStateById(final Long stateId) {
        return stateDAO.getStateById(stateId);
    }

    public StateEntity getStateByUuid(@NotNull String stateUuid) {
        return stateDAO.getStateByUuid(stateUuid);
    }
}
