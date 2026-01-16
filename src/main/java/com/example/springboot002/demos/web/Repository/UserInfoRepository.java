package com.example.springboot002.demos.web.Repository;


import com.example.springboot002.demos.web.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo,Integer>
{
    // 根据用户名查找
    Optional<UserInfo> findByUsername(String username);

    // 根据邮箱查找
    Optional<UserInfo> findByEmail(String email);

    // 根据激活状态查找
    List<UserInfo> findByIsActive(Boolean isActive);

    // 根据姓名模糊查询
    List<UserInfo> findByNameContaining(String keyword);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查邮箱是否存在
    boolean existsByEmail(String email);

    // 自定义查询：查找管理员用户
    @Query("SELECT u FROM UserInfo u WHERE u.isAdmin = true")
    List<UserInfo> findAllAdmins();

    // 自定义查询：统计活跃用户数量
    @Query("SELECT COUNT(u) FROM UserInfo u WHERE u.isActive = true")
    Long countActiveUsers();

    // 自定义查询：根据创建时间范围查找1
    @Query("SELECT u FROM UserInfo u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<UserInfo> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                          @Param("endDate") java.time.LocalDateTime endDate);
}
