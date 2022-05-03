package jpabook.jpashop.domain.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    @DisplayName("상품 주문")
    void orderSave()throws Exception{

        //given
        Member member = creatMember();
        Item book = creatBook("백엔드 개발자", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), 2);

        //the
        Order getOrder = orderRepository.findOne(orderId);
        Assertions.assertEquals(getOrder.getStatus(),OrderStatus.ORDER,"상품 주문시 상태는 ORDER");
        Assertions.assertEquals(1, getOrder.getOrderItems().size(),"주문한 상품 종류 수가 정확해야 한다.");
        Assertions.assertEquals(10000*orderCount, getOrder.getTotalPrice(), "주문 가격 * 수량이다");
        Assertions.assertEquals(8, book.getStockQuantity(), "주문수량 만큼 재고가 줄어야된다");
    }

    @Test
    @DisplayName("상품주문_재고수량초과")
    void notEnoughStockException() throws Exception{
        //given
        Member member = creatMember();
        Item item = creatBook("백엔드 개발자", 10000, 10);

        int orderCount = 11;

        //when & then
        Assertions.assertThrows(RuntimeException.class,()->{
            orderService.order(member.getId(), item.getId(), orderCount);
        });

    }

    @Test
    @DisplayName("상품 주문취소")
    void orderCancel(){
        //given
        Member member = creatMember();
        Item item = creatBook("백엔드 개발자", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        Assertions.assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 캔슬");
        Assertions.assertEquals(10, item.getStockQuantity(), "주문 취소시 상태는 캔슬");
    }


    private Item creatBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);

        em.persist(book);
        return book;
    }

    private Member creatMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "서초", "123-123"));
        em.persist(member);
        return member;
    }



}