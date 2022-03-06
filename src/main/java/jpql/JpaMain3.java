package jpql;

import javafx.print.Collation;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

public class JpaMain3 {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
             // 경로 표현식 (묵시적조인, 명시적조인)

             /**
             1. 상태필드
             단순히 값을 저장하기 위한 필드
             경로 탐색의 끝
             **/
             String jpql = "select m.username from Member m";
             List<String> resultList = em.createQuery(jpql, String.class).getResultList();

             /**
             2. 연관필드 - 단일 값 연관 필드
             @ManyToOne, @OneToOne, 대상이 엔티티
             탐색 가능
             (m.team)
             묵시적 내부 조인 발생(inner join) =>사용 자제, 직관적이지 않다.
             team이라는 컬럼명이 Team객체인지 team컬럼인지 명확하게 구분이 안됨!
             탐색 가능
             m.team.name

             select team1_.id as id1_3_,
                    team1_.name as name2_3_
             from Member member0_
             inner join Team team1_
                on member0_.TEAM_ID=team1_.id
             */
            String jpql2 = "select m.team from Member m";
            List<Team> resultList2 = em.createQuery(jpql2, Team.class).getResultList();

             /**
             3. 컬렉션 값 연관필드
             묵시적인 매부 조인 발생
             @OneToMany, @ManyToMany
             컬렉션은 경로 탐색의 끝이기때문에, "select t.members from Team t" X!!!
             From 절에서 명시적 조인을 통해 별칭을 얻어서 탐색한다. O!!
             String jpql3 = "select m.username from Team t join t.members m";
             List<String> resultList1 = em.createQuery(jpql3, String.class).getResultList();

             * select members1_.username as col_0_0_
             * from Team team0_
             * inner join Member members1_
             *      on team0_.id=members1_.TEAM_ID
             */

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
