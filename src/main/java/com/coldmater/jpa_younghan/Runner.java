package com.coldmater.jpa_younghan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;

@Component
public class Runner implements ApplicationRunner {
    @Autowired
    EntityManagerFactory emf;
    //보통 DB 당 하나만 생성된다. 애플리케이션이 실행되는 시점에.

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        //고객의 요청이 올 때마다 생성됐다가 사라지는 것이다.
        //따라서 쓰레드간에 공유를 해서는 안된다.

        tx.begin();
        //JPA의 모든 데이터 변경은 트랜젝션 안에서 실행해야만 한다.

        //INSERT
        try{
            Member member = new Member();
            member.setId(1L);
            member.setName("yh");

            em.persist(member);

            Member member2 = new Member();
            member2.setId(2L);
            member2.setName("pcw");

            em.persist(member2);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        // FIND & UPDATE
        EntityManager em2 = emf.createEntityManager();

        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();

        try {
            Member loadedMember = em2.find(Member.class, 1L);

            //FIND
            System.out.println("member.id = " + loadedMember.getId());
            System.out.println("member.name = " + loadedMember.getName());

            //em.persist(loadedMember) 를 부를 필요가 없다.
            //java collection 을 다루는 것처럼 다룰 수 있기 때문이다.
            //JPA에 등록된 entity 는 JPA 가 관리를 하게 되는데, transaction commit 시점에 해당 entity 의 변경사항을 체크하여
            //알아서 쿼리를 날려준 뒤 commit 을 실행하게 된다.

            //UPDATE
            loadedMember.setName("ck");

            tx2.commit();
        } catch (Exception e) {
            tx2.rollback();
        } finally {
            em2.close();
        }

        //JPQL
        // JPA 를 사용하면서도 복잡한 쿼링을 지원하기 위한 기능, 결국 JPA 를 사용하다보면 기승전쿼리
        // JPA 를 사용하면 엔티티 객체를 중심으로 개발을 하게 되는데, 데이터베이스에서 필터링을 하여 가져와야 하는 경우,
        // 테이블을 기준으로 하는것이 아닌 객체를 대상으로 검색할 수 있게 해줌.
        // 결국 JPA 를 사용하여 객체 중심으로 개발을 하면서도 검색 조건이 포함된 SQL 쿼링을 객체를 대상으로 하기 위한 기술이다.
        // 따라서 객체지향 SQL 이라고 할 수 있겠다. (dialect 에 맞게 sql 이 다시 날아간다.)
        // sql 쓰신 분들은 1~2시간 공부하면 다 쓸 수 있다.
        EntityManager em3 = emf.createEntityManager();

        //이 때, m 은 테이블이라기보다는 객체의 개념이라고 보면 된다.
        //setFirstResult, setMaxResults 같은 개념은 페이지네이션 개념과 대응되는데,
        //dialect 를 바꾸면 그 DB 언어에 따라서 알아서 쿼링한다.(ex - Oracle 에서는 rownum, 일반(ansi)적으로는 limit, offset)
        List<Member> loadedMembers = em3.createQuery("select m from Member as m", Member.class)
                .setFirstResult(5)
                .setMaxResults(8)
                .getResultList();

        for (Member member: loadedMembers) {
            System.out.println("member.name = " + member.getName());
        }

        em3.close();

        emf.close();
    }
}
