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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address address;
    private AddressRequest addressRequest;
    private AddressResponse addressResponse;

    private static final String USER_ID = "user-id-1";
    private static final String ADDRESS_ID = "address-id-1";

    @BeforeEach
    void setUp() {
        user = User.builder().id(USER_ID).username("testuser").build();

        address = Address.builder()
                .id(ADDRESS_ID)
                .user(user)
                .title("Home")
                .street("Main St 123")
                .city("Istanbul")
                .country("Turkey")
                .zipCode("34000")
                .defaultAddress(false)
                .createdAt(LocalDateTime.now())
                .build();

        addressRequest = new AddressRequest("Home", "Main St 123", "Istanbul", "Turkey", "34000", false);

        addressResponse = AddressResponse.builder()
                .id(ADDRESS_ID)
                .title("Home")
                .street("Main St 123")
                .city("Istanbul")
                .country("Turkey")
                .zipCode("34000")
                .defaultAddress(false)
                .build();
    }

    // ---- createAddress ----

    @Test
    @DisplayName("createAddress: ilk adres otomatik olarak varsayilan olur")
    void createAddress_firstAddress_becomesDefault() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(addressMapper.addressRequestToAddress(addressRequest)).thenReturn(address);
        when(addressRepository.countByUserId(USER_ID)).thenReturn(0L);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        addressService.createAddress(USER_ID, addressRequest);

        assertThat(address.getDefaultAddress()).isTrue();
        verify(addressRepository).unmarkAllDefaultByUserId(USER_ID);
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("createAddress: defaultAddress=true ise diger adresler varsayilan olmaktan cikarilir")
    void createAddress_explicitDefault_unmarksOthers() {
        AddressRequest defaultRequest = new AddressRequest("Work", "Biz St 1", "Ankara", "Turkey", "06000", true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(addressMapper.addressRequestToAddress(defaultRequest)).thenReturn(address);
        when(addressRepository.countByUserId(USER_ID)).thenReturn(2L);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        addressService.createAddress(USER_ID, defaultRequest);

        assertThat(address.getDefaultAddress()).isTrue();
        verify(addressRepository).unmarkAllDefaultByUserId(USER_ID);
    }

    @Test
    @DisplayName("createAddress: defaultAddress=false ve diger adresler varken varsayilan olmaz")
    void createAddress_notDefault_withExistingAddresses() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(addressMapper.addressRequestToAddress(addressRequest)).thenReturn(address);
        when(addressRepository.countByUserId(USER_ID)).thenReturn(2L);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        addressService.createAddress(USER_ID, addressRequest);

        assertThat(address.getDefaultAddress()).isFalse();
        verify(addressRepository, never()).unmarkAllDefaultByUserId(any());
    }

    @Test
    @DisplayName("createAddress: kullanici bulunamadi")
    void createAddress_userNotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.createAddress("non-existent", addressRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(addressRepository, never()).save(any());
    }

    // ---- getAddressesByUserId ----

    @Test
    @DisplayName("getAddressesByUserId: kullanicinin tum adresleri")
    void getAddressesByUserId_success() {
        Address address2 = Address.builder().id("address-id-2").user(user).title("Work").build();
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of(address, address2));
        when(addressMapper.addressToAddressResponse(any(Address.class))).thenReturn(addressResponse);

        List<AddressResponse> result = addressService.getAddressesByUserId(USER_ID);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getAddressesByUserId: kullanici bulunamadi")
    void getAddressesByUserId_userNotFound() {
        when(userRepository.existsById("non-existent")).thenReturn(false);

        assertThatThrownBy(() -> addressService.getAddressesByUserId("non-existent"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getAddressesByUserId: adresi olmayan kullanici icin bos liste")
    void getAddressesByUserId_noAddresses() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<AddressResponse> result = addressService.getAddressesByUserId(USER_ID);

        assertThat(result).isEmpty();
    }

    // ---- getAddressById ----

    @Test
    @DisplayName("getAddressById: basarili getirme")
    void getAddressById_success() {
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.getAddressById(USER_ID, ADDRESS_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ADDRESS_ID);
    }

    @Test
    @DisplayName("getAddressById: adres bulunamadi")
    void getAddressById_notFound() {
        when(addressRepository.findByUserIdAndId(USER_ID, "non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(USER_ID, "non-existent"))
                .isInstanceOf(AddressNotFoundException.class);
    }

    // ---- getDefaultAddress ----

    @Test
    @DisplayName("getDefaultAddress: varsayilan adres mevcut")
    void getDefaultAddress_success() {
        address.setDefaultAddress(true);
        when(addressRepository.findByUserIdAndDefaultAddressTrue(USER_ID)).thenReturn(Optional.of(address));
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.getDefaultAddress(USER_ID);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getDefaultAddress: varsayilan adres yok")
    void getDefaultAddress_notFound() {
        when(addressRepository.findByUserIdAndDefaultAddressTrue(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getDefaultAddress(USER_ID))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining("No default address found");
    }

    // ---- updateAddress ----

    @Test
    @DisplayName("updateAddress: basarili guncelleme")
    void updateAddress_success() {
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        AddressResponse result = addressService.updateAddress(USER_ID, ADDRESS_ID, addressRequest);

        assertThat(result).isNotNull();
        verify(addressMapper).updateAddressFromRequest(addressRequest, address);
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("updateAddress: defaultAddress=true ise diger adresler varsayilandan cikarilir")
    void updateAddress_setAsDefault_unmarksOthers() {
        AddressRequest defaultRequest = new AddressRequest("Home", "Main St", "Istanbul", "Turkey", "34000", true);
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(addressMapper.addressToAddressResponse(address)).thenReturn(addressResponse);

        addressService.updateAddress(USER_ID, ADDRESS_ID, defaultRequest);

        verify(addressRepository).unmarkAllDefaultByUserId(USER_ID);
        assertThat(address.getDefaultAddress()).isTrue();
    }

    @Test
    @DisplayName("updateAddress: adres bulunamadi")
    void updateAddress_notFound() {
        when(addressRepository.findByUserIdAndId(USER_ID, "non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(USER_ID, "non-existent", addressRequest))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).save(any());
    }

    // ---- deleteAddress ----

    @Test
    @DisplayName("deleteAddress: varsayilan olmayan adres silme")
    void deleteAddress_notDefault_success() {
        address.setDefaultAddress(false);
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));

        addressService.deleteAddress(USER_ID, ADDRESS_ID);

        verify(addressRepository).delete(address);
        verify(addressRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("deleteAddress: varsayilan adres silinince diger adres varsayilan olur")
    void deleteAddress_wasDefault_promotesNextAddress() {
        address.setDefaultAddress(true);
        Address nextAddress = Address.builder().id("address-id-2").user(user).defaultAddress(false).build();
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of(nextAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(nextAddress);

        addressService.deleteAddress(USER_ID, ADDRESS_ID);

        verify(addressRepository).delete(address);
        assertThat(nextAddress.getDefaultAddress()).isTrue();
        verify(addressRepository).save(nextAddress);
    }

    @Test
    @DisplayName("deleteAddress: varsayilan adres silinince baska adres yoksa promosyon yapilmaz")
    void deleteAddress_wasDefault_noRemainingAddresses() {
        address.setDefaultAddress(true);
        when(addressRepository.findByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressRepository.findByUserId(USER_ID)).thenReturn(List.of());

        addressService.deleteAddress(USER_ID, ADDRESS_ID);

        verify(addressRepository).delete(address);
        verify(addressRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteAddress: adres bulunamadi")
    void deleteAddress_notFound() {
        when(addressRepository.findByUserIdAndId(USER_ID, "non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(USER_ID, "non-existent"))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).delete(any(Address.class));
    }

    // ---- setDefaultAddress ----

    @Test
    @DisplayName("setDefaultAddress: basarili guncelleme")
    void setDefaultAddress_success() {
        when(addressRepository.existsByUserIdAndId(USER_ID, ADDRESS_ID)).thenReturn(true);
        when(addressRepository.findById(ADDRESS_ID)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        addressService.setDefaultAddress(USER_ID, ADDRESS_ID);

        verify(addressRepository).unmarkAllDefaultByUserId(USER_ID);
        assertThat(address.getDefaultAddress()).isTrue();
        verify(addressRepository).save(address);
    }

    @Test
    @DisplayName("setDefaultAddress: adres bu kullaniciya ait degil")
    void setDefaultAddress_addressNotBelongToUser() {
        when(addressRepository.existsByUserIdAndId(USER_ID, "other-address-id")).thenReturn(false);

        assertThatThrownBy(() -> addressService.setDefaultAddress(USER_ID, "other-address-id"))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).unmarkAllDefaultByUserId(any());
    }
}
