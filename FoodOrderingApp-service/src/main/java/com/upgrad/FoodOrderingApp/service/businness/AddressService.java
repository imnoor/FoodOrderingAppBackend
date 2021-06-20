package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {
    @Autowired
    private AddressDao addressDAO;

    @Autowired
    private StateDao stateDAO;

    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity) throws SaveAddressException {
        if (addressEntity.getCity().isEmpty() ||
                addressEntity.getLocality().isEmpty() ||
                addressEntity.getFlatBuilNo().isEmpty()
        ) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        String regex = "^\\d{1,6}$";
        if (!addressEntity.getPincode().matches(regex)) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
        addressEntity.setCustomer(customerEntity);
        return addressDAO.saveAddress(addressEntity);
    }


    public AddressEntity getAddressById(final Long addressId) {
        return addressDAO.getAddressById(addressId);
    }

    public StateEntity getStateByUUID(String stateUuid) throws AddressNotFoundException {
        StateEntity stateEntity = stateDAO.getStateByUuid(stateUuid);
        if (stateEntity == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this state id");
        }
        return stateEntity;
    }

    public AddressEntity getAddressByUUID(String uuid, CustomerEntity customerEntity) throws AddressNotFoundException, AuthorizationFailedException {
        AddressEntity addressEntity = addressDAO.getAddressByUUID(uuid);
        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        if (!customerEntity.hasAddress(uuid)) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        return addressEntity;
    }

    @Transactional
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        addressDAO.deleteAddress(addressEntity.getId());
        return addressEntity;
    }

    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {
        Hibernate.initialize(customerEntity);
        return customerEntity.getSortedAddresses();
    }

    public List<StateEntity> getAllStates() {

        List<StateEntity> stateEntities = stateDAO.getAllStates();
        return stateEntities;
    }

}
