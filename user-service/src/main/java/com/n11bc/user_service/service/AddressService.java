package com.n11bc.user_service.service;

import com.n11bc.user_service.dto.request.AddressRequest;
import com.n11bc.user_service.dto.response.AddressResponse;
import com.n11bc.user_service.entity.Address;
import com.n11bc.user_service.entity.User;
import com.n11bc.user_service.exception.AddressNotFoundException;
import com.n11bc.user_service.exception.UserNotFoundException;
import com.n11bc.user_service.mapper.AddressMapper;
import com.n11bc.user_service.repository.AddressRepository;
import com.n11bc.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional
    public AddressResponse createAddress(String userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId, "id"));

        Address address = addressMapper.addressRequestToAddress(request);
        address.setUser(user);

        // If this is the first address or explicitly marked as default
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 0 || request.isDefaultAddress()) {
            // Unmark all other addresses as default
            addressRepository.unmarkAllDefaultByUserId(userId);
            address.setDefaultAddress(true);
        }

        Address savedAddress = addressRepository.save(address);
        log.info("Address created for user {}: {}", userId, savedAddress.getTitle());

        return addressMapper.addressToAddressResponse(savedAddress);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId, "id");
        }

        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::addressToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String userId, String addressId) {
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        return addressMapper.addressToAddressResponse(address);
    }

    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress(String userId) {
        Address address = addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .orElseThrow(() -> new AddressNotFoundException("No default address found for user"));

        return addressMapper.addressToAddressResponse(address);
    }

    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        addressMapper.updateAddressFromRequest(request, address);

        // If marked as default, unmark others
        if (request.isDefaultAddress()) {
            addressRepository.unmarkAllDefaultByUserId(userId);
            address.setDefaultAddress(true);
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated for user {}: {}", userId, updatedAddress.getTitle());

        return addressMapper.addressToAddressResponse(updatedAddress);
    }

    @Transactional
    public void deleteAddress(String userId, String addressId) {
        Address address = addressRepository.findByUserIdAndId(userId, addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));

        boolean wasDefault = address.getDefaultAddress();
        addressRepository.delete(address);
        log.info("Address deleted for user {}: {}", userId, addressId);

        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
                log.info("New default address set for user {}: {}", userId, newDefault.getId());
            }
        }
    }

    @Transactional
    public void setDefaultAddress(String userId, String addressId) {
        if (!addressRepository.existsByUserIdAndId(userId, addressId)) {
            throw new AddressNotFoundException(addressId);
        }

        // Unmark all as default
        addressRepository.unmarkAllDefaultByUserId(userId);

        // Mark the specified address as default
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        address.setDefaultAddress(true);
        addressRepository.save(address);

        log.info("Address {} set as default for user {}", addressId, userId);
    }
}
