package org.olivesafety.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olivesafety.common.exception.handler.ItemHandler;
import org.olivesafety.common.exception.handler.MemberHandler;
import org.olivesafety.common.exception.handler.OrdersHandler;
import org.olivesafety.common.exception.status.ErrorStatus;
import org.olivesafety.item.domain.Item;
import org.olivesafety.item.domain.repository.ItemRepository;
import org.olivesafety.member.domain.Coupon;
import org.olivesafety.member.domain.Member;
import org.olivesafety.member.domain.repository.CouponRepository;
import org.olivesafety.order.converter.OrdersConverter;
import org.olivesafety.order.domain.Orders;
import org.olivesafety.order.domain.OrdersItem;
import org.olivesafety.order.domain.repository.OrdersItemRepository;
import org.olivesafety.order.domain.repository.OrdersRepository;
import org.olivesafety.order.dto.OrdersRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersCommandService {

    private final ItemRepository itemRepository;
    private final OrdersRepository ordersRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final CouponRepository couponRepository;

    //@PersistenceContext
    //private EntityManager entityManager;

    @Transactional
    public Orders create(OrdersRequestDTO.ordersAddDTO request, Member member) {

        // orders 엔티티 생성 및 연관관계 매핑
        Orders neworders = OrdersConverter.toorders(request,member);
        ordersRepository.save(neworders);

        // ordersItem, 연관관계 매핑, 판매 재고 업데이트
        Item item = itemRepository.findByIdWithLock(request.getId()).orElseThrow(()-> new ItemHandler(ErrorStatus.ITEM_NOT_FOUND));
        //entityManager.refresh(item);

        // ordersItem 엔티티 생성 및 연관관계 매핑
        OrdersItem newordersItem = OrdersConverter.toordersItem(request, item.getPrice());
        newordersItem.setItem(item);
        newordersItem.setorders(neworders);

        if (newordersItem.getAmount() > item.getStock()) {
            throw new OrdersHandler(ErrorStatus.LACK_OF_STOCK);
        }

        //쿠폰 사용하는지 검사
        if(request.getCode() != null){

            Coupon coupon = couponRepository.findByCode(request.getCode()).orElseThrow(() -> new MemberHandler(ErrorStatus.COUPON_NOT_FOUND));
            //쿠폰 코드 검사
            if(coupon.isUsed()){
                throw new MemberHandler(ErrorStatus.COUPON_NOT_VALID);
            }

            newordersItem.applyCouponPrice();
            coupon.useCoupon();
        }

        // item의 판매량, 재고 업데이트
        item.updateSales(newordersItem.getAmount());
        item.updateStock(newordersItem.getAmount());

        ordersItemRepository.save(newordersItem);


        return neworders;
    }


}
