package com.example.demo.staff;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Staff findByStaffId(Integer staffId);
    List<Staff> findByUserIn(List<User> users);
    Optional<Staff> findByUser(User user);
    Optional<Staff> findByUser_UserId(Integer userId);

    @Query("""
        select distinct s
        from Staff s
        join s.user u
        join s.department d
        join UserRole ur on ur.user = u
        join ur.role r
        where u.userId != :userId
            and (
                    :keyword is null or
                        lower(s.name) like lower(concat('%', :keyword, '%')) or
                        lower(d.departmentName) like lower(concat('%', :keyword, '%')) or
                        lower(r.roleName) like lower(concat('%', :keyword, '%'))
                )
    """)
    List<Staff> searchStaff(@Param("userId") Integer userId, @Param("keyword") String keyword);
}
