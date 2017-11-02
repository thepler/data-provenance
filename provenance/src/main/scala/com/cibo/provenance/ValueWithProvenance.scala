package com.cibo.provenance

/**
  * Created by ssmith on 9/12/17.
  *
  * ValueWithProvenance[O] is a sealed trait with the following primary implementations:
  * - FunctionCallWithProvenance[O]: a function, its version, and its inputs (also with provenance)
  * - FunctionCallResultWithProvenance[O]: adds a return value to the above signature
  * - UnknownProvenance[O]: a special case of FunctionCallWithProvenance[O] for data w/o history.
  *
  * The type parameter O refers to the return/output type of the function in question.
  *
  * There is an implicit conversion from T -> UnknownProvenance[T] allowing for an
  * entrypoints into the history.
  *
  * Inputs to a FunctionWithProvenance are themselves each some ValueWithProvenance, so a call
  * can be arbitrarily composed of other calls, results of other calls, or raw values.
  *
  * Each of the Function* classes has multiple implementations numbered for the input count.
  * Currently only 0-4 have been written, though traditionally scala expects 0-22.
  *
  * Other members of the sealed trait:
  *
  * The *Deflated versions of FunctionCall{,Result}WithProvenance hold only text strings,
  * and can represent parts of the provenance tree in external applications.  The ResultTracker
  * API returns these as it saves.
  *
  * The IdentityCall and IdentityResult classes are behind UnknownProvenance and NoVersion,
  * and are required to prevent circular deps and boostrap the system.
  *
  */

import com.cibo.provenance.tracker.{ResultTracker, ResultTrackerNone}

import scala.collection.immutable
import scala.language.implicitConversions
import scala.reflect.ClassTag


sealed trait ValueWithProvenance[O] extends Serializable {
  def getOutputClassTag: ClassTag[O]
  def isResolved: Boolean
  def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O]
  def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O]
  def deflate(implicit rt: ResultTracker): ValueWithProvenance[O]
  def inflate(implicit rt: ResultTracker): ValueWithProvenance[O]

  protected def nocopy[T](newObj: T, prevObj: T): T =
    if (newObj == prevObj)
      prevObj
    else
      newObj
}


object ValueWithProvenance {
  // Convert any value T to an UnknownProvenance[T] wherever a ValueWithProvenance is expected.
  // This is how "normal" data is passed into FunctionWithProvenance transparently.
  implicit def convertValueWithNoProvenance[T : ClassTag](v: T): ValueWithProvenance[T] =
    UnknownProvenance(v)
}


abstract class FunctionCallWithProvenance[O : ClassTag](var version: ValueWithProvenance[Version]) extends ValueWithProvenance[O] with Serializable {
  self =>

  val isResolved: Boolean = false

  lazy val getOutputClassTag: ClassTag[O] = implicitly[ClassTag[O]]

  /*
   * Abstract interface.  These are implemented by Function{n}CallSignatureWithProvenance.
   */

  val functionName: String

  // The subclasses are specific Function{N}.
  val impl: AnyRef

  def getInputs: Seq[ValueWithProvenance[_]]

  def resolveInputs(implicit rt: ResultTracker): FunctionCallWithProvenance[O]

  def unresolveInputs(implicit rt: ResultTracker): FunctionCallWithProvenance[O]

  def deflateInputs(implicit rt: ResultTracker): FunctionCallWithProvenance[O]

  def inflateInputs(implicit rt: ResultTracker): FunctionCallWithProvenance[O]

  def run(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O]

  protected[provenance] def newResult(value: Deflatable[O])(implicit bi: BuildInfo): FunctionCallResultWithProvenance[O]

  def getVersion: ValueWithProvenance[Version] = Option(version) match {
    case Some(value) => value
    case None => NoVersion // NoVersion breaks some serialization, so we, sadly, allow a null version for internal things.
  }

  def getVersionValue(implicit rt: ResultTracker): Version = getVersion.resolve.getOutputValue

  def getVersionValueAlreadyResolved: Option[Version] = {
    // This returns a version but only if it is already resolved.
    getVersion match {
      case u: UnknownProvenance[Version] => Some(u.value)
      case r: FunctionCallResultWithProvenance[Version] => r.getOutputValueOption
      case _ => None
    }
  }

  def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] =
    rt.resolve(this)

  def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    unresolveInputs(rt)

  protected[provenance]def getNormalizedDigest(implicit rt: ResultTracker): Digest =
    Util.digest(unresolve(rt))

  protected[provenance]def getInputGroupDigest(implicit rt: ResultTracker): Digest =
    Util.digest(getInputDigests(rt))

  protected[provenance]def getInputDigests(implicit rt: ResultTracker): List[String] = {
    getInputs.toList.map {
      input =>
        val resolvedInput = input.resolve
        val inputValue = resolvedInput.getOutputValue
        val id = Util.digest(inputValue)
        val inputValueDigest = id.id
        inputValueDigest
    }
  }

  def getInputsDigestWithSourceFunctionAndVersion(implicit rt: ResultTracker): Vector[FunctionCallResultWithProvenanceDeflated[_]] = {
    val inputs = getInputs.toVector
    inputs.indices.map {
      i => inputs(i).resolve.deflate
    }.toVector
  }

  def getInputGroupValuesDigest(implicit rt: ResultTracker): Digest = {
    val inputsDeflated: immutable.Seq[FunctionCallResultWithProvenanceDeflated[_]] = getInputsDigestWithSourceFunctionAndVersion
    val digests = inputsDeflated.map(_.outputDigest).toList
    Digest(Util.digest(digests).id)
  }

  def deflate(implicit rt: ResultTracker): FunctionCallWithProvenanceDeflated[O] =
    rt.saveCall(this)

  def inflate(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    this
}


abstract class FunctionCallResultWithProvenance[O](
  provenance: FunctionCallWithProvenance[O],
  output: Deflatable[O],
  outputBuildInfo: BuildInfo
) extends ValueWithProvenance[O] with Serializable {

  def getOutput: Deflatable[O] = output

  def getOutputValueOption: Option[O] = output.valueOption

  def getOutputValue(implicit rt: ResultTracker): O = output.resolveValue(rt).valueOption.get

  def getProvenanceValue: FunctionCallWithProvenance[O]

  def getOutputClassTag: ClassTag[O] = provenance.getOutputClassTag

  def getOutputBuildInfo: BuildInfo = outputBuildInfo

  def getOutputBuildInfoBrief: BuildInfoBrief = outputBuildInfo.abbreviate

  override def toString: String =
    provenance match {
      case _: UnknownProvenance[O] =>
        // When there is no provenance info, just stringify the data itself.
        getOutputValueOption match {
          case Some(value) => value.toString
          case None =>
            throw new RuntimeException("No outputValue for value with unknown provenance???")
        }
      case f: FunctionCallWithProvenance[O] =>
        getOutputValueOption match {
          case Some(outputValue) =>
            f"($outputValue <- ${f.toString})"
          case None =>
            f"(? <- ${f.toString})"
        }
    }

  val isResolved: Boolean = true

  def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] =
    this

  def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    this.getProvenanceValue.unresolve

  def deflate(implicit rt: ResultTracker): FunctionCallResultWithProvenanceDeflated[O] =
    rt.saveResult(this)

  def inflate(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] =
    this

}

/*
 * IdentityCall and IdentityResult are used to bootstrap parts of the system (UnknownProvenance and NoVersion).
 * They must be in this source file because to be part of the VirtualValue sealed trait.
 * They use a null to avoid circulatrity/serialization problems with the NoVersion Version itself being an IdentityValue.
 *
 */

//scalastyle:off
class IdentityCall[O : ClassTag](value: O) extends Function0CallWithProvenance[O](null)((_) => value) with Serializable {
  val functionName: String = toString

  // Note: this takes a version of "null" and explicitly sets getVersion to NoVersion.
  // Using NoVersion in the signature directly causes problems with Serialization.
  override def getVersion: ValueWithProvenance[Version] = NoVersion

  private lazy implicit val rt: ResultTracker = ResultTrackerNone()(NoBuildInfo)


  private lazy val cachedResult = new IdentityResult(this, Deflatable(value).resolveDigest)

  def newResult(o: Deflatable[O])(implicit bi: BuildInfo): IdentityResult[O] =
    cachedResult

  def duplicate(vv: ValueWithProvenance[Version]): Function0CallWithProvenance[O] =
    new IdentityCall(value)(implicitly[ClassTag[O]])

  private lazy val cachedDigest = Util.digest(value)

  override def getInputGroupValuesDigest(implicit rt: ResultTracker): Digest =
    cachedDigest

  private lazy val cachedCollapsedDigests: Vector[FunctionCallResultWithProvenanceDeflated[O]] =
    Vector(cachedResultDeflated)

  private lazy val cachedResultDeflated = {
    FunctionCallResultWithProvenanceDeflated[O](
      deflatedCall = FunctionCallWithKnownProvenanceDeflated[O](
        functionName = functionName,
        functionVersion = getVersionValue,
        inflatedCallDigest = Util.digest(this),
        outputClassName = getOutputClassTag.runtimeClass.getName
      ),
      inputGroupDigest = getInputGroupDigest,
      outputDigest = cachedResult.getOutput.resolveDigest.digestOption.get,
      buildInfo = cachedResult.getOutputBuildInfoBrief
    )
  }

  override def getInputsDigestWithSourceFunctionAndVersion(implicit rt: ResultTracker): Vector[FunctionCallResultWithProvenanceDeflated[_]] =
    cachedCollapsedDigests

  override def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] = cachedResult

  override def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O] = this

}


class IdentityResult[O : ClassTag](
  call: IdentityCall[O],
  output: Deflatable[O]
) extends Function0CallResultWithProvenance[O](call, output)(NoBuildInfo) with Serializable {
  override def deflate(implicit rt: ResultTracker): FunctionCallResultWithProvenanceDeflated[O] =
    FunctionCallResultWithProvenanceDeflated(this)
}


class IdentityValueForBootstrap[O : ClassTag](value: O) extends ValueWithProvenance[O] with Serializable {
  // This bootstraps the system by wrapping a value as a virtual value.
  // There are two uses: UnknownProvenance[O] and NoVersion.

  val isResolved: Boolean = true

  val getOutputClassTag: ClassTag[O] = implicitly[ClassTag[O]]

  private lazy val resultVar: IdentityResult[O] = new IdentityResult(callVar, Deflatable(value))
  def resolve(implicit s: ResultTracker): IdentityResult[O] = resultVar

  private lazy val callVar: IdentityCall[O] = new IdentityCall(value)
  def unresolve(implicit s: ResultTracker): IdentityCall[O] = callVar

  override def deflate(implicit rt: ResultTracker): ValueWithProvenanceDeflated[O] =
    resultVar.deflate(rt)

  def inflate(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] = this.resolve
}


/*
 * UnknownProvenance[T] represents raw values that lack provenance tracking.
 *
 */

case class UnknownProvenance[O : ClassTag](value: O) extends IdentityCall[O](value) {

  override def toString: String = f"raw($value)"

  override def newResult(value: Deflatable[O])(implicit bi: BuildInfo): UnknownProvenanceValue[O] =
    new UnknownProvenanceValue[O](this, value)

}


case class UnknownProvenanceValue[O : ClassTag](prov: UnknownProvenance[O], output: Deflatable[O]) extends IdentityResult[O](prov, output) {
  // Note: This class is present to complete the API, but nothing in the system instantiates it.
  // THe newResult method is never called for an UnknownProvenance[T].
  override def toString: String = f"rawv($output)"
}


/*
 * The second type of IdentityValue is used to represent a null version number.
 *
 */


object NoVersion extends IdentityValueForBootstrap[Version](Version("-")) with Serializable


/*
 * Deflated equivalents of the above classes are still functional as ValueWithProvenance[O],
 * but require conversion to instantiate fully.
 *
 * This allows history to extends across software made by disconnected libs,
 * and also lets us save an object graph with small incremental pieces.
 *
 */


sealed trait ValueWithProvenanceDeflated[O] extends ValueWithProvenance[O] with Serializable


sealed trait FunctionCallWithProvenanceDeflated[O] extends ValueWithProvenanceDeflated[O] with Serializable {

  def isResolved: Boolean = false

  def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] =
    inflate.resolve(rt)

  def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    inflate.unresolve(rt)

  def deflate(implicit rt: ResultTracker): FunctionCallWithProvenanceDeflated[O] = this

  def inflate(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    inflateOption match {
      case Some(value) => value
      case None =>
        throw new RuntimeException(f"Failed to inflate serialized data in $rt for $this")
    }

  def inflateOption(implicit rt: ResultTracker): Option[FunctionCallWithProvenance[O]]

}

case class FunctionCallWithKnownProvenanceDeflated[O](
  functionName: String,
  functionVersion: Version,
  outputClassName: String,
  inflatedCallDigest: Digest
)(implicit ct: ClassTag[O]) extends FunctionCallWithProvenanceDeflated[O] with Serializable {

  def getOutputClassTag: ClassTag[O] = implicitly[ClassTag[O]]

  def inflateOption(implicit rt: ResultTracker): Option[FunctionCallWithProvenance[O]] = {
    inflateNoRecurse.map {
      inflated => inflated.inflateInputs(rt)
    }
  }

  def inflateNoRecurse(implicit rt: ResultTracker): Option[FunctionCallWithProvenance[O]] = {
    rt.loadCallOption[O](functionName, functionVersion, inflatedCallDigest)
  }

}


case class FunctionCallWithUnknownProvenanceDeflated[O : ClassTag](
  outputClassName: String,
  valueDigest: Digest
) extends FunctionCallWithProvenanceDeflated[O] {

  def getOutputClassTag: ClassTag[O] = implicitly[ClassTag[O]]

  def inflateOption(implicit rt: ResultTracker): Option[FunctionCallWithProvenance[O]] =
    rt.loadValueOption[O](valueDigest).map(v => UnknownProvenance(v))
}


object FunctionCallWithProvenanceDeflated {

  def apply[O](call: FunctionCallWithProvenance[O])(implicit rt: ResultTracker): FunctionCallWithProvenanceDeflated[O] = {
    implicit val outputClassTag: ClassTag[O] = call.getOutputClassTag
    val outputClassName: String = outputClassTag.runtimeClass.getName
    call match {
      case valueWithUnknownProvenance : UnknownProvenance[O] =>
        FunctionCallWithUnknownProvenanceDeflated[O](
          outputClassName = outputClassName,
          Util.digest(valueWithUnknownProvenance.value)
        )
      case _ =>
        FunctionCallWithKnownProvenanceDeflated[O](
          functionName = call.functionName,
          functionVersion = call.getVersionValue,
          inflatedCallDigest = Util.digest(call.deflateInputs(rt)),
          outputClassName = outputClassName
        )
    }
  }
}


case class FunctionCallResultWithProvenanceDeflated[O](
  deflatedCall: FunctionCallWithProvenanceDeflated[O],
  inputGroupDigest: Digest,
  outputDigest: Digest,
  buildInfo: BuildInfo
)(implicit ct: ClassTag[O]) extends ValueWithProvenanceDeflated[O] with Serializable {

  def getOutputClassTag: ClassTag[O] = ct

  def isResolved: Boolean =
    true

  def resolve(implicit rt: ResultTracker): FunctionCallResultWithProvenance[O] =
    inflate.resolve(rt)

  def unresolve(implicit rt: ResultTracker): FunctionCallWithProvenance[O] =
    inflate.unresolve(rt)

  def deflate(implicit rt: ResultTracker): FunctionCallResultWithProvenanceDeflated[O] =
    this

  def inflate(implicit rt: ResultTracker): ValueWithProvenance[O] = {
    val output = rt.loadValue[O](outputDigest)
    deflatedCall.inflate match {
      case unk: UnknownProvenance[O] =>
        unk
      case call =>
        call.newResult(Deflatable(output))(buildInfo)
    }
  }
}


object FunctionCallResultWithProvenanceDeflated {

  def apply[O](result: FunctionCallResultWithProvenance[O])(implicit rt: ResultTracker): FunctionCallResultWithProvenanceDeflated[O] = {
    val provenance = result.getProvenanceValue
    implicit val outputClassTag: ClassTag[O] = provenance.getOutputClassTag
    FunctionCallResultWithProvenanceDeflated[O](
      deflatedCall = FunctionCallWithProvenanceDeflated(provenance),
      inputGroupDigest = provenance.getInputGroupDigest,
      outputDigest = result.getOutput.resolveDigest.digestOption.get,
      buildInfo = result.getOutputBuildInfoBrief
    )
  }

  def apply[O : ClassTag](
    functionName: String,
    functionVersion: Version,
    functionCallDigest: Digest,
    inputGroupDigest: Digest,
    outputDigest: Digest,
    outputClassName: String,
    buildInfo: BuildInfo
  ): FunctionCallResultWithProvenanceDeflated[O] = {

    FunctionCallResultWithProvenanceDeflated(
      deflatedCall = FunctionCallWithKnownProvenanceDeflated[O](
        functionName = functionName,
        functionVersion = functionVersion,
        outputClassName = outputClassName,
        inflatedCallDigest = functionCallDigest
      ),
      inputGroupDigest = inputGroupDigest,
      outputDigest = outputDigest,
      buildInfo = buildInfo
    )
  }
}
