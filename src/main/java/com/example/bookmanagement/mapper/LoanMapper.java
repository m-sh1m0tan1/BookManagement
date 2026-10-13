package com.example.bookmanagement.mapper;

import com.example.bookmanagement.entity.Loan;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LoanMapper {

    @Select("select * from loans where borrowed_user = #{userId}")
    List<Loan> getLoansByBorrowedUserId(int userId); // ユーザIDをもとに貸出記録を取得

    @Select("select * from loans where id = #{id}")
    Loan getLoanById(int id); // IDをもとに貸出記録を取得

    @Select("select * from loans where borrowed_user = #{id} and returned_at is null order by due_date desc, id asc")
    List<Loan> getUnReturnedLoanByUserId(int id); // ユーザIDをもとに未返却の貸出記録を取得

    @Select("select * from loans where borrowed_book = #{bookId} and returned_at is null order by due_date desc, id asc")
    List<Loan> getUnReturnedLoanByBookId(int bookId); // 書籍IDをもとに未返却の貸出記録を取得

    @Insert("insert into loans (borrowed_user, borrowed_book, due_date) values (#{borrowedUser}, #{borrowedBook}, #{dueDate})")
    int insertLoan(Loan loan); // 貸出記録の挿入

    @Update("update loans set returned_at = now() where id = #{id} and returned_at is null")
    int updateReturnedAtById(int id); // IDをもとに返却日時を更新

    @Update("update loans set due_date = #{dueDate} where id = #{id} and returned_at is null")
    int updateDueDateById(int id, LocalDate dueDate); // IDをもとに返却期限を更新
}
