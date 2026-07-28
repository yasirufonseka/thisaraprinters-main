package com.example.thisaraprinters.service;

import com.example.thisaraprinters.dto.CustomerDto;
import com.example.thisaraprinters.model.CustomerModel;
import com.example.thisaraprinters.repository.CustomerRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepo customerRepo;

    public CustomerService(CustomerRepo customerRepo) {
        this.customerRepo = customerRepo;
    }

    public List<CustomerModel> getAllCustomers() {
        return customerRepo.findAll();
    }

    public CustomerModel getCustomerById(Integer id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id " + id));
    }

    public CustomerModel addCustomer(CustomerDto dto) {
        if (customerRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Save failed: Company Name '" + dto.getName() + "' is already registered by another customer.");
        }
        if (customerRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Save failed: Email address '" + dto.getEmail() + "' is already in use.");
        }
        if (customerRepo.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Save failed: Company contact number '" + dto.getPhone() + "' is already registered.");
        }

        CustomerModel customer = new CustomerModel();
        customer.setName(dto.getName());
        customer.setAddress(dto.getAddress());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setContactperson(dto.getContactperson());
        customer.setContactpersonphone(dto.getContactpersonphone());
        customer.setCreateddate(LocalDate.now());
        customer.setUpdateddate(LocalDate.now());
        return customerRepo.save(customer);
    }

    public CustomerModel updateCustomer(Integer id, CustomerDto dto) {
        CustomerModel existing = getCustomerById(id);

        if (customerRepo.existsByNameAndIdNot(dto.getName(), id)) {
            throw new RuntimeException("Update failed: Company Name '" + dto.getName() + "' is already registered by another customer.");
        }
        if (customerRepo.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new RuntimeException("Update failed: Email address '" + dto.getEmail() + "' is already in use.");
        }
        if (customerRepo.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new RuntimeException("Update failed: Company contact number '" + dto.getPhone() + "' is already registered.");
        }

        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setContactperson(dto.getContactperson());
        existing.setContactpersonphone(dto.getContactpersonphone());
        existing.setUpdateddate(LocalDate.now());
        return customerRepo.save(existing);
    }

    public void deleteCustomer(Integer id) {
        customerRepo.deleteById(id);
    }
}

