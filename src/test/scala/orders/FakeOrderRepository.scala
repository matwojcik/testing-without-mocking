package orders
import cats.mtl.MonadState
import cats.syntax.all._
import com.olegpy.meow.prelude._
import orders.domain.Order

object FakeOrderRepository {

  implicit def instance[F[_]](implicit S: MonadState[F, Map[Order.Id, Order]]): OrderRepository[F] = new OrderRepository[F] {
    override def find(id: Order.Id): F[Option[Order]] =
      S.get.map(_.get(id))

    override def save(order: Order): F[Unit] =
      S.modify(_ + (order.id -> order))
  }
}
