package jpql;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();

//            String jpql = "select m from Member m inner join m.team t where t.name = 'teamA'"; // 내부조인
//            String jpql = "select m from Member m left join m.team t where t.name = 'teamA'"; // 외부조인
//            String jpql = "select m from Member m, Team t where m.username=t.name"; // 세타조인

            String jpql = "select m from Member m LEFT JOIN m.team t on m.id = t.id"; // 외부조인
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

            List<Member> resultList = em.createQuery(jpql, Member.class).getResultList();
            for (Member member1 : resultList) {
                System.out.println("member1 = " + member1);
            }
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
