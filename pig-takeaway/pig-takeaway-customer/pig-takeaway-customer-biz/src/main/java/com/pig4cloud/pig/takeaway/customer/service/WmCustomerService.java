package com.pig4cloud.pig.takeaway.customer.service;

import com.pig4cloud.pig.takeaway.customer.dto.WmCustomerDTO;

public interface WmCustomerService {

	WmCustomerDTO createCustomer(WmCustomerDTO customerDTO);

	boolean updateCustomer(WmCustomerDTO customerDTO);

	WmCustomerDTO current();

}
