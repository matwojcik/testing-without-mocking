package orders

import orders.domain.Order
import orders.domain.Order.Status.Complete
import orders.domain.Order.Status.InProgress
import orders.domain.Order.Status.Initial
import org.scalatest.Matchers
import org.scalatest.WordSpec

class OrdersSpec extends WordSpec with Matchers {

  val OrderId = Order.Id("123")
  val OrderInInitialStatus = Order(OrderId, Initial)
  val OrderInProgressStatus = Order(OrderId, InProgress)
  val OrderInCompleteStatus = Order(OrderId, Complete)

  "Order completion" when {
    "order is initial status" should {
      "not complete the order" in {}
    }

    "order is in progress" should {
      "complete order" in {}
    }
  }

}
