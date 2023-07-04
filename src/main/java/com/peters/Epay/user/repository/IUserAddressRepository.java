package com.peters.Epay.user.repository;

import com.peters.Epay.user.entity.UserAddress;
import com.peters.Epay.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserAddressRepository extends JpaRepository<UserAddress, Long> {
    Optional<UserAddress> findByUser(UserEntity user);
}
