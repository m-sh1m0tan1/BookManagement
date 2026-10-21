package com.example.bookmanagement.mapper;

import com.example.bookmanagement.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("select * from users where mail = #{mail}")
    User getUserByMail(String mail); // メールをもとにユーザ取得

    @Select("select * from users where id = #{id}")
    User getUserById(int id); // IDをもとにユーザ取得

    @Select("select * from users where id = #{id} for update")
    User getUserByIdForUpdate(int id); // IDをもとにユーザ取得(ロック)

    @Insert("insert into users (mail, name, password) values (#{mail}, #{name}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertUser(User user); // ユーザ追加

    @Update("update users set mail = #{mail}, name = #{name}, password = #{password} where id = #{id}")
    int updateUser(User user); // ユーザ更新

    @Update("update users set is_admin = #{isAdmin} where id = #{id}")
    int updateIsAdminByUserId(int id, boolean isAdmin); // 管理者権限更新
}
