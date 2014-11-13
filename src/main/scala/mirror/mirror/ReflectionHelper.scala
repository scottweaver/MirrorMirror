package mirror.mirror

import scala.reflect.ClassTag
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import ru._

/**
 *
 * @param clazz
 * @tparam T
 */
class ReflectionHelper[T](clazz: Class[T]) {
  val classSymbol = cm.classSymbol(clazz)
  implicit val cTag = ClassTag[T](clazz)
  lazy val m = ru.runtimeMirror(getClass.getClassLoader)


  val companionObject = classSymbol.companion.asModule
  val companionMirror = cm.reflect(cm.reflectModule(companionObject).instance)
  val tpe = classSymbol.toType

  val ts = companionMirror.symbol.typeSignature
  val mApply = ts.member(ru.TermName("apply")).asMethod
  val syms = mApply.paramLists.flatten

  lazy val allMembers = tpe.members

  def parameterListWithDefaults: List[(TermSymbol, Option[Any])] = {
    syms.zipWithIndex.map { case (p: TermSymbol, i) =>
      println(s"Param name is ${p.name} : ${p.typeSignature}")
      if (p.isParamWithDefault) {
        val mDef = ts.member(ru.TermName(s"apply$$default$$${i + 1}")).asMethod
        (p, Some(companionMirror.reflectMethod(mDef)()))
      }
      else {
        (p, None)
      }
    }
  }

  def newInstanceOf(args: List[Any]): T = {
    companionMirror.reflectMethod(mApply)(args: _*).asInstanceOf[T]
  }

  def valueOf(onInstance: T, symbol: TermSymbol): Option[Any] = {
    try
      symbol match {
        case sym: MethodSymbol => Some(m.reflect(onInstance).reflectMethod(sym).apply())
        case sym => Some(m.reflect(onInstance).reflectField(symbol).get)
      }
    catch {
      case e: ScalaReflectionException => {
        System.err.println(s"Failed dynamic invocation ror $symbol on $clazz: ${e.msg}.")
        None
      }
    }
  }

  def valueOf(onInstance: T, fieldName: String): Option[Any] = {
    declaredTermSymbolFor(fieldName) match {
      case Some(sym) => valueOf(onInstance, sym)
      case None => None
    }
  }

  def memberValueOf(onInstance: T, memberName: String): Option[Any] = {
    memberTermSymbolFor(memberName) match {
      case Some(sym) => valueOf(onInstance, sym)
      case None => None
    }
  }

  def declaredTermSymbolFor(name: String): Option[TermSymbol] = {
    try
      Some(tpe.decl(ru.TermName(name)).asTerm)
    catch {
      case e: ScalaReflectionException => None
    }
  }

  def memberTermSymbolFor(name: String): Option[TermSymbol] = {
    try
      Some(tpe.member(ru.TermName(name)).asTerm)
    catch {
      case e: ScalaReflectionException => None
    }
  }

  def companion_?(clazz: Class[T]): Boolean = {
    cm.classSymbol(clazz).isModule
  }

}

object ReflectionHelper {

  implicit class ReflectiveInstance[T <: Any](instance: T) {
    def reflectionHelper: ReflectionHelper[T] = ReflectionHelper(instance)
  }

  implicit class ReflectiveType[T <: Type](t: T) {
    def reflectionHelper: ReflectionHelper[T] = ReflectionHelper(t)
  }

  def apply[T](classTag: ClassTag[T]): ReflectionHelper[T] = {
    classTag.runtimeClass match {
      case c: Class[T] => apply(c)
    }
  }

  def apply[T](i: T): ReflectionHelper[T] = {
    i.getClass match {
      case c: Class[T] => apply(c)
    }
  }

  def apply[T](clazz: Class[T]): ReflectionHelper[T] = {
    new ReflectionHelper[T](clazz)
  }

}

