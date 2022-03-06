package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain4 {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
             /**
             엔티티 fetch 조인
             **/
             String jpql = "select m from Member m join m.team";
             List<Member> resultList = em.createQuery(jpql, Member.class).getResultList();
             /**
             select m.*
             from Member m
             inner join Team t
                on m.TEAM_ID = t.id
             */

            String jpql2 = "select m from Member m join fetch m.team";
            List<Member> resultList2 = em.createQuery(jpql2, Member.class).getResultList();

             /**
              *  즉시로딩 느낌으로
              *  내가 원할때 조회 가능
             select m.*,
                    t.*     -- fetch조인을 할 경우 Team객체의 모든 컬럼도 조회된다.
             from Member m
             inner join Team t
                on m.TEAM_ID=t.id
             */

             Team teamA = new Team("팀A");
             em.persist(teamA);

             Team teamB = new Team("팀B");
             em.persist(teamB);

             Member member1 = new Member("회원1", 1, MemberType.USER);
             member1.setTeam(teamA);
             em.persist(member1);

            Member member2 = new Member("회원2", 2, MemberType.USER);
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member("회원3", 3, MemberType.USER);
            member3.setTeam(teamB);
            em.persist(member3);

            Member member4 = new Member("회원4", 4, MemberType.USER);
            em.persist(member4);

            em.flush();
            em.clear();

            String jpql3 = "select m from Member m where m.username IN ('회원1', '회원2', '회원3')";
            List<Member> resultList3 = em.createQuery(jpql3, Member.class).getResultList();

            for (Member member : resultList3) {
                System.out.println("member = " + member + ", " + member.getTeam().getName());
                /**
                 * Team을 @ManyToOne(fetch = FetchType.LAZY) 해서 가져오기때문에
                 * member.getTeam().getName()을 만났을 때 Team select 쿼리가 시작된다.
                 *
                 * 회원1은 팀A SQL쿼리가 나간다.
                 * 회원2는 팀A이기 때문에 영속성컨텍스트에 저장되어있어서 [1차캐시]에서 조회한다.
                 * 회원3은 영속성컨텍스트에 없기때문에 SQL쿼리가 나간다.
                 *
                 * ==> 이렇게하면 만약 회원100명을 조회한다고 하면
                 * 쿼리가 최대 100번 나가기때문에, 이 경우 fetch 조인을 사용해야한다.
                 */
            }

            em.flush();
            em.clear();

            /**
            fetch 조인을 하면 아래 쿼리가 실행되어 1차캐시 안에 등록하기에
            member.getTeam().getName()할때마다 select를 안한다.
            select m.*, t.* from Member m inner join Team t
            지연로딩으로 설정해도 fetch 조인이 우선이다
             **/
            String jpql4 = "select m from Member m join fetch m.team t";
            List<Member> resultList4 = em.createQuery(jpql4, Member.class).getResultList();
            for (Member member : resultList4) {
                System.out.println("member = " + member + ", " + member.getTeam().getName());
            }

            em.flush();
            em.clear();

            String jpql5 = "select t from Team t join fetch t.members";
            List<Team> resultList5 = em.createQuery(jpql5, Team.class).getResultList();
            for (Team team : resultList5) {
                System.out.println("team.getName()  = " + team.getName() +", size:"+ team.getMembers().size());
                /**
                 team.getName()  = 팀A, size 2
                 team.getName()  = 팀A, size 2
                 team.getName()  = 팀B, size 1

                 // 일대다는 Query가 뻥튀기 된다.
                 */
            }

            String jpql6 = "select t from Team t";// size 2
            String jpql7 = "select t from Team t join fetch t.members"; // size 3

            // distinct 써보면 어떻게될까?
            String jpql8 = "select distinct t from Team t join fetch t.members"; // size 2
            List<Team> resultList8 = em.createQuery(jpql8, Team.class).getResultList();
            for (Team team : resultList8) {
                System.out.println("team.getMembers() = " + team.getMembers());
                // team.getMembers() = [Member{id=3, username='회원1', age=1}, Member{id=4, username='회원2', age=2}]
                // team.getMembers() = [Member{id=5, username='회원3', age=3}]
            }
            /**
              위 jqpl8로 인한 쿼리는 아래와 같다 ==> size 3
              select distinct team0_.id as id1_3_0_,
                       members1_.id as id1_0_1_, team0_.name as name2_3_0_, members1_.age as age2_0_1_, members1_.TEAM_ID as team_id5_0_1_,
                       members1_.type as type3_0_1_, members1_.username as username4_0_1_, members1_.TEAM_ID as team_id5_0_0__, members1_.id as id1_0_0__ 
              from Team team0_ 
              inner join Member members1_ 
              on team0_.id=members1_.TEAM_ID
              
              But resultList8의 size는 2이다
              왜 이런 결과가 나온것일까?
             
              JPA의 DISTINCT는 SQL에 DISTINCT를 줄여줄뿐 아니라, **엔티티의 중복도 제거**
              또 위 쿼리결과 역시 신기하다
             team.getMembers() = [Member{id=3, username='회원1', age=1}, Member{id=4, username='회원2', age=2}]
             team.getMembers() = [Member{id=5, username='회원3', age=3}]*/

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
