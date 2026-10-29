package com.example.bookmanagement.mapper;

import com.example.bookmanagement.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {
    @Select("select * from books where id = #{id} and is_available = true")
    Book getBookById(int id); // IDをもとに本取得

    @Select("select * from books where (name like CONCAT('%', #{keyword}, '%') or author like CONCAT('%', #{keyword}, '%')) and is_available = true")
    List<Book> getBooksByKeyword(String keyword); // キーワードをもとに本を取得

    @Select("select b.* from books b where (b.name like CONCAT('%', #{keyword}, '%') or b.author like CONCAT('%', #{keyword}, '%')) and b.quantity > (select count(*) from loans l where l.borrowed_book = b.id and l.returned_at is null) and b.is_available = true")
    List<Book> getAvailableBooksByKeyword(String keyword); // 貸出可能な本をキーワードで検索

    @Select("select * from books where author = #{author} and is_available = true")
    List<Book> getBooksByAuthor(String author); // 指定した著者名の本を取得

    @Select("select * from books where id = #{id} and is_available = true for update")
    Book getBookByIdForUpdate(int id); // IDをもとに本を取得(ロック)

    @Insert("insert into books(name, author, isbn, quantity) values(#{name}, #{author}, #{isbn}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertBook(Book book); // 新規本の追加

    @Update("update books set name = #{name}, author = #{author}, isbn = #{isbn}, quantity = #{quantity} where id = #{id}")
    int updateBook(Book book); // 本の更新

    @Update("update books set is_available = false where id = #{id} and quantity = 0")
    int softDeleteBookById(int id); // IDをもとに本を削除
}
