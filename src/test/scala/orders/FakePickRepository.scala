package orders
import cats.mtl.MonadState
import cats.syntax.all._
import com.olegpy.meow.prelude._
import orders.domain.Order
import orders.domain.Pick

object FakePickRepository {

  implicit def instance[F[_]](implicit S: MonadState[F, Map[Order.Id, List[Pick]]]): PickRepository[F] = new PickRepository[F] {
    override def findPicksOfOrder(id: Order.Id): F[List[domain.Pick]] =
      S.get.map(_.getOrElse(id, List.empty))
  }

}
