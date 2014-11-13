package mirror.mirror

import org.scalatest.{Matchers, WordSpecLike}

/**
 * Created by Scott T Weaver on 11/13/14.
 *
 */
class TestReflectionHelper extends WordSpecLike with Matchers {


  val harry = Wizard("Harry Potter", 5)
  val rh = ReflectionHelper(harry)

  "A ReflectionHelper" should {
    "get be able to access declared field values and methods by name." in {
      rh.valueOf(harry, "name") should be (Some("Harry Potter"))
      rh.valueOf(harry, "level") should be (Some(5))
      rh.valueOf(harry, "toString") should be (Some("Wizard(Harry Potter,5)"))
      rh.valueOf(harry, "apparate") should be (None)

    }

    "be able to access inherited members" in {
      rh.memberValueOf(harry, "apparate") should be (Some("Wooooosh!!! <gone>"))
    }


    "be good about handling non-existent fields" in {
      val rh = ReflectionHelper(harry)
      rh.valueOf(harry, "notReal") should be (None)

    }

    "supply easy to use implicits" in {
      import ReflectionHelper._
      val hermione = Wizard("Hermione Granger", 5)
      hermione.reflectionHelper.valueOf(hermione, "name") should be (Some("Hermione Granger"))

//      val rht = Wizard.reflectionHelper //.valueOf(hermione, "name") should be (Some("Hermione Granger"))
    }
  }

}

class MagicUser {
  def apparate: String = "Wooooosh!!! <gone>"
}

case class Wizard(name: String, level : Int = 1) extends MagicUser
