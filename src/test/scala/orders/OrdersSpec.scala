package orders

import cats.Functor
import cats.data.EitherT
import cats.data.StateT
import cats.effect.IO
import cats.mtl.MonadState
import cats.mtl.instances.all._
import cats.syntax.all._
import orders.Orders.OrderCompletionFailure
import orders.Orders.OrderNotInProgress
import orders.domain.Order
import orders.domain.Order.Status.Complete
import orders.domain.Order.Status.InProgress
import orders.domain.Order.Status.Initial
import org.scalatest.Matchers
import org.scalatest.WordSpec

class OrdersSpec extends WordSpec with Matchers {

  type TestEffect[A] = EitherT[StateT[IO, Map[Order.Id, Order], ?], OrderCompletionFailure, A]

  implicit def repository[F[_]: Functor](implicit S: MonadState[F, Map[Order.Id, Order]]): OrderRepository[F] = new OrderRepository[F] {
    override def find(id: Order.Id): F[Option[Order]] =
      S.get.map(_.get(id))

    override def save(order: Order): F[Unit] =
      S.modify(_ + (order.id -> order))
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
