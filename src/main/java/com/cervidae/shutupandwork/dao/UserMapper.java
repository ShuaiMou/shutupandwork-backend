package com.cervidae.shutupandwork.dao;

import com.cervidae.shutupandwork.pojo.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author AaronDu
 */
@Mapper
@Repository("UserMapper")
public interface UserMapper extends IMapper {

    @Select("SELECT * FROM USER " +
            "WHERE username=#{username}")
    User getByUsername(@Param("username") String username);

    @Select("SELECT * FROM USER " +
            "WHERE id=#{id}")
    User getById(@Param("id") int id);

    @Deprecated
    @Insert("INSERT INTO USER " +
            "VALUES (NULL, #{username}, #{score}, #{currentTime})")
    void add(@Param("username") String username, @Param("score") int score, @Param("currentTime") long currentTime);

    @Insert("INSERT INTO USER " +
            "VALUES (NULL, #{username}, 0, #{currentTime}, #{password}, #{role})")
    void register(@Param("username") String username, @Param("password") String password,
                  @Param("currentTime") long currentTime, String role);

    @Update("UPDATE USER " +
            "SET username=#{username}, score=#{score}, updated=#{currentTime} " +
            "WHERE id=#{id} and updated=#{updated}")
    void updateOptimistic(@Param("id") int id, @Param("username") String username, @Param("score") int score,
                          @Param("updated") long updated, @Param("currentTime") long currentTime);
}