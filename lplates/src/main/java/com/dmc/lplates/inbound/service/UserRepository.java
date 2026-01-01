//package com.dmc.lplates.inbound.service;
//
//import java.util.List;
//
//import com.dmc.lplates.inbound.models.User;
//import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.data.repository.query.Param;
//
//
//
//
//public interface UserRepository extends PagingAndSortingRepository<User, Long> {
//
//	List<User> findByUsernameAndPassword(String username, String password);
//
//	List<User> findByUsernameContaining(@Param("username") String username);
//
//	User findByUsernameIgnoreCase(String username);
//
//}
