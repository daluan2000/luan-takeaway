package com.luan.takeaway.takeaway.user.service;

import com.luan.takeaway.takeaway.common.entity.WmAddress;

import java.util.List;

public interface WmAddressService {

	WmAddress createAddress(WmAddress address);

	boolean updateAddress(WmAddress address);

	boolean deleteAddress(Long id);

	WmAddress getAddress(Long id);

	List<WmAddress> listCurrentUserAddresses();

}
