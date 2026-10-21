package com.example.bookmanagement.mapper;

import com.example.bookmanagement.dto.LoanWithBookDetail;
import com.example.bookmanagement.entity.Loan;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LoanMapper {

    @Select("select * from loans where borrowed_user = #{userId}")
    List<Loan> getLoansByBorrowedUserId(int userId); // ユーザIDをもとに貸出記録を取得

    @Select("select * from loans where id = #{id}")
    Loan getLoanById(int id); // IDをもとに貸出記録を取得

    @Select("select * from loans where id = #{id} for update")
    Loan getLoanByIdForUpdate(int id);

    @Select("select * from loans where borrowed_user = #{id} and returned_at is null order by due_date desc, id asc")
    List<Loan> getUnReturnedLoanByUserId(int id); // ユーザIDをもとに未返却の貸出記録を取得

    @Select("select * from loans where borrowed_book = #{bookId} and returned_at is null order by due_date desc, id asc")
    List<Loan> getUnReturnedLoanByBookId(int bookId); // 書籍IDをもとに未返却の貸出記録を取得

    @Select("SELECT l.id, l.borrowed_user AS user_id, u.name AS user_name, l.borrowed_book AS book_id, b.name AS book_name, b.author AS book_author, (b.is_available AND b.quantity > (SELECT COUNT(*) FROM loans active WHERE active.borrowed_book = b.id AND active.returned_at IS NULL)) AS book_borrowable, NOT b.is_available AS book_deleted, l.borrowed_at, l.due_date, l.returned_at FROM loans l JOIN books b ON b.id = l.borrowed_book JOIN users u ON u.id = l.borrowed_user WHERE l.borrowed_user = #{userId}")
    List<LoanWithBookDetail> getLoanWithBookDetailByUserId(int userId); // ユーザIDをもとに貸出記録とその書籍の詳細を取得

    @Insert("insert into loans (borrowed_user, borrowed_book, due_date) values (#{borrowedUser}, #{borrowedBook}, #{dueDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertLoan(Loan loan); // 貸出記録の挿入

    @Update("update loans set returned_at = now() where id = #{id} and returned_at is null")
    int updateReturnedAtById(int id); // IDをもとに返却日時を更新

    @Update("update loans set due_date = #{dueDate}, extended = true where id = #{id} and returned_at is null and extended = false")
    int updateDueDateById(int id, LocalDate dueDate); // IDをもとに返却期限を更新
}
