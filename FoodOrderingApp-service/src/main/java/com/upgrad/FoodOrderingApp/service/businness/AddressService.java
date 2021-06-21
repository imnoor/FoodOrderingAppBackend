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


    /**
     *
     * @param addressEntity
     * @param customerEntity
     * @return
     * @throws SaveAddressException
     *
     * Save address of the customer.
     *
     */
    @Transactional
    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity) throws SaveAddressException {

        //validate for mandatory fields
        if (addressEntity.getCity().isEmpty() ||
                addressEntity.getLocality().isEmpty() ||
                addressEntity.getFlatBuilNo().isEmpty()
        ) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }

        //Pin code validation
        String regex = "^\\d{1,6}$";
        if (!addressEntity.getPincode().matches(regex)) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
        //link address to the customer
        addressEntity.setCustomer(customerEntity);
        //save and return
        return addressDAO.saveAddress(addressEntity);
    }

    /**
     *
     * @param stateUuid
     * @return
     * @throws AddressNotFoundException
     *
     * State lookup by UUID
     *
     */

    public StateEntity getStateByUUID(String stateUuid) throws AddressNotFoundException {
        StateEntity stateEntity = stateDAO.getStateByUuid(stateUuid);
        if (stateEntity == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this state id");
        }
        return stateEntity;
    }

    /**
     *
     * @param uuid
     * @param customerEntity
     * @return
     * @throws AddressNotFoundException
     * @throws AuthorizationFailedException
     *
     * Get Address of customer by UUID
     *
     */
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

    /**
     *
     * @param addressEntity
     * @return
     *
     * Delete address
     *
     */

    @Transactional
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        addressDAO.deleteAddress(addressEntity);
        return addressEntity;
    }

    /**
     *
     * @param customerEntity
     * @return
     *
     * Get all address of a Customer
     *
     */
    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {
        Hibernate.initialize(customerEntity);
        return customerEntity.getSortedAddresses();
    }

    /**
     *
     * @return
     *
     * Get all states.
     *
     */

    public List<StateEntity> getAllStates() {

        List<StateEntity> stateEntities = stateDAO.getAllStates();
        return stateEntities;
    }

}
