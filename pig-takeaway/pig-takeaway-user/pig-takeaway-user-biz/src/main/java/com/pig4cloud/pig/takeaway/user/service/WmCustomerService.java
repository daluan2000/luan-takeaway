package com.pig4cloud.pig.takeaway.user.service;

import com.pig4cloud.pig.takeaway.user.dto.WmCustomerDTO;

public interface WmCustomerService {

	WmCustomerDTO createCustomer(WmCustomerDTO customerDTO);

	boolean updateCustomer(WmCustomerDTO customerDTO);

	WmCustomerDTO current();

}
