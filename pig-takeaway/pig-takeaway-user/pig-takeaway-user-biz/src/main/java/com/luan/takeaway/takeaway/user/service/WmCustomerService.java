package com.luan.takeaway.takeaway.user.service;

import com.luan.takeaway.takeaway.user.dto.WmCustomerDTO;

public interface WmCustomerService {

	WmCustomerDTO createCustomer(WmCustomerDTO customerDTO);

	boolean updateCustomer(WmCustomerDTO customerDTO);

	WmCustomerDTO current();

}
