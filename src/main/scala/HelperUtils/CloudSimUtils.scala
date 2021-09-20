package HelperUtils

import com.typesafe.config.Config
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerAbstract, VmSchedulerSpaceShared, VmSchedulerTimeShared}

object CloudSimUtils {
  def getVmScheduler(t: String): VmSchedulerAbstract = t match {
    case "SpaceShared" => new VmSchedulerSpaceShared()
    case "TimeShared" => new VmSchedulerTimeShared()
    case _ => throw new RuntimeException("Unsupported vm scheduler")
  }

  def getconfigValue(path: String):Config  = ObtainConfigReference(path) match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

}
