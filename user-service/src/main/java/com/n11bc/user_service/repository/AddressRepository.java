package com.n11bc.user_service.repository;

import com.n11bc.user_service.entity.Address;
import com.n11bc.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    List<Address> findByUserId(String userId);

    Optional<Address> findByUserIdAndId(String userId, String addressId);

    Optional<Address> findByUserIdAndDefaultAddressTrue(String userId);

    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId")
    void unmarkAllDefaultByUserId(@Param("userId") String userId);

    long countByUserId(String userId);

    boolean existsByUserIdAndId(String userId, String addressId);
}
