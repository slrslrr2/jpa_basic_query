package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain2 {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {

            // 페이징
            /**
            for(int i=0; i<100; i++){
                Member member = new Member("gbitkim", i);
                em.persist(member);
            }

            em.flush();
            em.clear();

            String jpql = "select m from Member m order by m.age desc";
            List<Member> resultList = em.createQuery(jpql, Member.class)
                    .setFirstResult(0)
                    .setMaxResults(10)
                    .getResultList();

            for (Member result : resultList) {
                System.out.println("result = " + result);
            }
            **/

            // STEP1. 내부조인
            Team team = new Team();
            team.setName("teamA");
            em.persist(team);

            Member member = new Member("gbitkim", 20);
            member.setType(MemberType.ADMIN);
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();

//            String jpql = "select m from Member m inner join m.team t where t.name = 'teamA'"; // 내부조인
//            String jpql = "select m from Member m left join m.team t where t.name = 'teamA'"; // 외부조인
//            String jpql = "select m from Member m, Team t where m.username=t.name"; // 세타조인
//            String jpql = "select m from Member m LEFT JOIN m.team t on m.id = t.id"; // 외부조인

            /**
             *
             select
                 member0_.id as id1_0_,
                 member0_.age as age2_0_,
                 member0_.TEAM_ID as team_id4_0_,
                 member0_.username as username3_0_
             from Member member0_
             left outer join Team team1_
             on member0_.TEAM_ID=team1_.id
                and ( member0_.id=team1_.id )
             */

            String jpql = "select m from Member m LEFT JOIN m.team t on m.id = t.id and m.type = :userTyoe"; // 외부조인
            List<Member> resultList = em.createQuery(jpql, Member.class)
                    .setParameter("userTyoe", MemberType.ADMIN).getResultList();
            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }

            // 단순 CASE
            String query = "select " +
                            "case t.name " +
                                "when '팀A' then '인센티브110%'" +
                                "when '팀B' then '인센티브120%'" +
                                "else '인센티브105%' END " +
                            "from Team t";

            // 조건 CASE
            String query2 = "select" +
                    "case when m.age <= 10 then '학생요금" +
                    "when m.age >= 60 then '경로요금" +
                    "else '일반요금' END" +
                    "from Member m";

            // NULL 값 채우기
            String query3 = "select coalesce(m.username, 'NULL값이다') from Member m";

            // 특정값이면 null 반환하고 나머지 본인이름
            // gbitkim 이름을 숨길 때 사용
            String query4 = "select NULLIF(m.username, 'gbitkim') from Member m";

            List<String> resultList1 = em.createQuery(query, String.class).getResultList();

            /**
             * JPQL 기본함수
             * CONCAT
             * SUBSTRING
             * TRIM
             * LOWER
             * UPPER
             * LENGTH
             * LOCATE
             * ABS
             * SQRT
             * MOD
             * SIZE
             */
            String queryJpqlBaseFunction = "select 'a' || 'b' From Member m";
            String queryJpqlBaseFunction2 = "select concat('a', 'b') From Member m";
            String queryJpqlBaseFunction3 = "select subString(2,3) From Member m"; // 2번째부터 3개를 잘라낸다.
            String queryJpqlBaseFunction4 = "select locate('de', 'abcdefg') From Member m"; // 숫자가 나옴 4번째에 있네.

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
