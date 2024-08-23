package org.olivesafety.common.presentation;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olivesafety.common.exception.handler.OrdersHandler;
import org.olivesafety.common.exception.status.ErrorStatus;
import org.olivesafety.item.domain.Item;
import org.olivesafety.item.domain.repository.ItemRepository;
import org.olivesafety.order.domain.Orders;
import org.olivesafety.order.domain.OrdersItem;
import org.olivesafety.order.domain.repository.OrdersItemRepository;
import org.olivesafety.order.domain.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j@Service
@RequiredArgsConstructor
public class SqsOrderProcessor {

    private final AmazonSQS amazonSQS;
    private final OrdersRepository ordersRepository;
    private final ItemRepository itemRepository;
    private final OrdersItemRepository ordersItemRepository;

    @Value("${cloud.aws.sqs.queueUrl}")
    private String queueUrl;

    @Scheduled(fixedRate = 5000)
    public void pollQueue() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withMaxNumberOfMessages(10);

        List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
        for (Message message : messages) {
            processMessage(message);
            amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle());
        }
    }

    private void processMessage(Message message) {
        // 메시지에서 orderId를 추출
        String orderId = extractOrderIdFromMessage(message.getBody());

        // 주문 및 연관된 OrdersItem 가져오기
        Orders order = ordersRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new OrdersHandler(ErrorStatus.ORDER_NOT_FOUND));

        for (OrdersItem orderItem : order.getOrdersItemList()) {
            Item item = orderItem.getItem();

            if (orderItem.getAmount() > item.getStock()) {
                throw new OrdersHandler(ErrorStatus.LACK_OF_STOCK);
            }

            // item의 판매량, 재고 업데이트
            item.updateSales(orderItem.getAmount());
            item.updateStock(orderItem.getAmount());

            ordersItemRepository.save(orderItem);
        }
    }

    private String extractOrderIdFromMessage(String messageBody) {
        return messageBody.replace("Order Created: ID=", "").trim();
    }
}
