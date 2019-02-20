package orders

import cats.Id
import cats.data.{EitherT, StateT}
import cats.effect.IO
import cats.mtl.instances.all._
import orders.Orders.{OrderCompletionFailure, OrderNotInProgress}
import orders.domain.Order
import orders.domain.Order.Status.Complete
import orders.domain.Order.Status.InProgress
import orders.domain.Order.Status.Initial
import org.scalatest.Matchers
import org.scalatest.WordSpec

class OrdersSpec extends WordSpec with Matchers {

  type TestEffect[A] = EitherT[StateT[IO, Map[Order.Id, Order], ?], OrderCompletionFailure, A]

  implicit val repository: OrderRepository[TestEffect] = new OrderRepository[TestEffect] {
    override def find(id: Order.Id): TestEffect[Option[Order]] =
      EitherT.liftF(StateT.inspect(_.get(id)))

    override def save(order: Order): TestEffect[Unit] =
      EitherT.liftF(StateT.modify(_ + (order.id -> order)))
  }

  val orders = Orders.instance[TestEffect]

  val OrderId = Order.Id("123")
  val OrderInInitialStatus = Order(OrderId, Initial)
  val OrderInProgressStatus = Order(OrderId, InProgress)
  val OrderInCompleteStatus = Order(OrderId, Complete)

  "Order completion" when {
    "order is initial status" should {
      "not complete the order" in {
        val initialState = Map(OrderId -> OrderInInitialStatus)

        val (state, result) = orders.completeOrder(OrderId).value.run(initialState).unsafeRunSync()
        state shouldEqual initialState
        result should be(Left(OrderNotInProgress(OrderId)))
      }
    }

    "order is in progress" should {
      "complete order" in {
        val initialState = Map(OrderId -> OrderInProgressStatus)

        val (state, result) = orders.completeOrder(OrderId).value.run(initialState).unsafeRunSync()
        state shouldEqual Map(OrderId -> OrderInCompleteStatus)
        result should be(Right(OrderInCompleteStatus))
      }
    }
  }

}
