package orders

import cats.Functor
import cats.data.EitherT
import cats.data.StateT
import cats.effect.IO
import cats.mtl.MonadState
import cats.mtl.instances.all._
import cats.syntax.all._
import orders.Orders.OrderCompletionFailure
import orders.Orders.OrderNotFullyPicked
import orders.Orders.OrderNotInProgress
import orders.domain.Order.Status.Complete
import orders.domain.Order.Status.InProgress
import orders.domain.Order.Status.Initial
import orders.domain.Order
import orders.domain.Pick
import org.scalatest.Matchers
import org.scalatest.WordSpec

class OrdersSpec extends WordSpec with Matchers {
  case class TestState(orders: Map[Order.Id, Order], picks: Map[Order.Id, List[Pick]])
  type TestEffect[A] = EitherT[StateT[IO, TestState, ?], OrderCompletionFailure, A]

  implicit def orderRepository[F[_]: Functor](implicit S: MonadState[F, TestState]): OrderRepository[F] = new OrderRepository[F] {
    override def find(id: Order.Id): F[Option[Order]] =
      S.get.map(_.orders.get(id))

    override def save(order: Order): F[Unit] =
      S.modify(s => s.copy(orders = s.orders + (order.id -> order)))
  }

  implicit def pickRepository[F[_]: Functor](implicit S: MonadState[F, TestState]): PickRepository[F] = new PickRepository[F] {
    override def findPicksOfOrder(id: Order.Id): F[List[domain.Pick]] =
      S.get.map(_.picks.getOrElse(id, List.empty))
  }

  val orders = Orders.instance[TestEffect]

  val OrderId = Order.Id("123")
  val OrderInInitialStatus = Order(OrderId, Initial)
  val OrderInProgressStatus = Order(OrderId, InProgress)
  val OrderInCompleteStatus = Order(OrderId, Complete)

  val PickInProgress = Pick(Pick.Id(1), Pick.Status.InProgress)
  val PickFinished = Pick(Pick.Id(2), Pick.Status.Finished)

  "Order completion" when {
    "order is initial status" should {
      "not complete the order" in {
        val initialState = TestState(Map(OrderId -> OrderInInitialStatus), Map.empty)

        val (state, result) = orders.completeOrder(OrderId).value.run(initialState).unsafeRunSync()

        state shouldEqual initialState
        result should be(Left(OrderNotInProgress(OrderId)))
      }
    }

    "order is in progress with in progress picks" should {
      "not complete order" in {
        val initialState = TestState(Map(OrderId -> OrderInProgressStatus), Map(OrderId -> List(PickInProgress, PickFinished)))

        val (state, result) = orders.completeOrder(OrderId).value.run(initialState).unsafeRunSync()

        state shouldEqual initialState
        result should be(Left(OrderNotFullyPicked(OrderId)))
      }
    }

    "order is in progress with finished picks" should {
      "complete order" in {
        val initialState = TestState(Map(OrderId -> OrderInProgressStatus), Map(OrderId -> List(PickFinished)))

        val (state, result) = orders.completeOrder(OrderId).value.run(initialState).unsafeRunSync()

        state shouldEqual initialState.copy(orders = Map(OrderId -> OrderInCompleteStatus))
        result should be(Right(OrderInCompleteStatus))
      }
    }
  }

}
