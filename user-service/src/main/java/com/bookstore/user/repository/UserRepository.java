package com.bookstore.user.repository;
import com.bookstore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findByFirstNameContainingIgnoreCase(String keyword);

}
