package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {

            // 1. 기본문법과 쿼리 API
            /**
            String jpql = "select m From Member m where m.username like '%g%'";

            // 1-1. retuen Type이 지정 O
            TypedQuery<Member> query = em.createQuery(jpql, Member.class);

            // 1-2. retuen Type이 지정 X
            Query query1 = em.createQuery("select m.username, m.age From Member m where m.username like '%g%'");

            // 2-1. return 여러개인 경우 null오류 X
            List<Member> resultList = query.getResultList();

            // 2-2. reuturn 단 한개인경우 null오류!
            // javax.persistence.NoResultException => 한개도 없는경우
            // javax.persistence.NonUniqueResultException => 2개이상인 경우
            Member singleResult = query.getSingleResult();

            // 3. 파라미터 바인딩
            TypedQuery query3 = em.createQuery("select m.username, m.age From Member m where m.username:username", Member.class)
                    .setParameter("username", "gbitkim");
            List<Member> resultList1 = query.getResultList();
             **/

            // 2, 프로젝션(SELECT)
            ////////////////////////////////////// STEP 2-1. 프로젝션 영속성관리 표시 //////////////////////////////////////////////////////
            Member member = new Member("lbitkim", 20, MemberType.ADMIN);
            em.persist(member);
            em.flush();
            em.clear();

            String jpql = "select m From Member m";
            List<Member> resultList = em.createQuery(jpql, Member.class).getResultList();

            // 영속성컨텍스트에서 위 jpql이 관리가 될까 안될까?
            // 1. 관리자 되고있다면 Age값이 변경됨
            // 2. 관리가 안되고있다면 Agr값 변경X
            Member findMember = resultList.get(0);
            findMember.setAge(30);
            /*
             // update jpql.Member
                update Member set
                age=?, TEAM_ID=?, username=?
                where id=?
             */
            // ==> update 쿼리가 나감. 즉, 관리가 되고있다.

            ////////////////////////////////////// STEP 2-2. 엔티티 프로젝션 //////////////////////////////////////////////////////
            String jpql2 = "select m.team From Member m";
            List<Team> teamList = em.createQuery(jpql2, Team.class).getResultList();
            /**
            select team1_.id as id1_3_
                , team1_.name as name2_3_
            from Member member0_
                inner join Team team1_
                on member0_.TEAM_ID=team1_.id
             */
            // join 쿼리가 나간다, 간편해 보이지만 저렇게 쓰지말고
            // 명확히 join 이 되는지 분명히 예시해주는것이 낫기때문이다.
            // m.team으로 하면 한눈에 join하는지 안보이기때문에

            String jpql3 = "select t From Member m join m.team t";
            List<Team> teamList2 = em.createQuery(jpql3, Team.class).getResultList();
            /**
            select team1_.id as id1_3_,
                   team1_.name as name2_3_
            from Member member0_
                inner join Team team1_
                on member0_.TEAM_ID=team1_.id
             */

            ////////////////////////////////////// STEP 2-3. 임베디드 프로젝션 //////////////////////////////////////////////////////
            // Address는 Order안에 있는 컬럼이기때문에 join쿼리 자체를 쓰는것이 불가능하다.
            // 즉, 직접 컬럼명을 o.address라고 명시하는것이 좋다.ㅍ
            // 또한 값 타입이기때문에 select a From Address라고 직접 명시할 수 없다. 시작하는 엔티티로 조회가능하다.

            String jpql4 = "select o.address From Order o";
            List<Address> resultList1 = em.createQuery(jpql4, Address.class).getResultList();

            ////////////////////////////////////// STEP 2-4. 스칼라 타입 프로젝션 //////////////////////////////////////////////////////
            // 아래와 같은 스칼라타입은 m.username, m.age 2가지를 조회해오는것이기에 변수를 다음과 같이 지정할 수 있다.
            em.createQuery("select m.username, m.age From Member m where m.username like '%b%'").getResultList();

            // 2-4-1. Object[]을 제네릭으로 명시합으로써 타입으로 조회
            List<Object[]> reesult = em.createQuery("select m.username, m.age From Member m where m.username like '%b%'").getResultList();
            Object[] oResult = reesult.get(0); //List의 2가지 변수들이기때문에 다음과 같이 가져온다.
            System.out.println("username = " + oResult[0]);
            System.out.println("age = " + oResult[1]);

            // 2-4-2 DTO를 새로 만들어서 조회
            // @Entity가 아닌 DTO를 생성자 함수를 사용해서 조회해오는 방법이다.
            // new 패키지명.클래스명()
            // 생성자 함수를 이용하는 방법이다.
            String jpqlGetObjectStr = "select new jpql.MemberDTO(m.username, m.age) " +
                    "From Member m " +
                    "where m.username like '%b%'";
            List<MemberDTO> resultList2 = em.createQuery(jpqlGetObjectStr, MemberDTO.class).getResultList();
            MemberDTO memberDTO = resultList2.get(0);
            System.out.println("memberDTO.getUsername() = " + memberDTO.getUsername());

            tx.commit();
        } catch (Exception e){
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}