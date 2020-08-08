package com.coldmater.jpa_younghan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

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

        emf.close();
    }
}
