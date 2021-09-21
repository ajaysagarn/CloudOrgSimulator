package HelperUtils

import com.typesafe.config.Config
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRandom, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerAbstract, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic, UtilizationModelFull, UtilizationModelPlanetLab, UtilizationModelStochastic}

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

  def getDcVmAllocationPolicy(t: String): VmAllocationPolicy = t match {
    case "Simple" => new VmAllocationPolicySimple()
    case "Random" => new VmAllocationPolicyRandom(new UniformDistr())
    case "BestFit" => new VmAllocationPolicyBestFit()
    case "FirstFit" => new VmAllocationPolicyFirstFit()
    case "RoundRobin" => new VmAllocationPolicyRoundRobin()
    case _ => throw new RuntimeException("Unsupported Vm Allocation Policy")
  }

  def getUtilizationModel(t: String): UtilizationModel = t match {
    case "Dynamic" => new UtilizationModelDynamic()
    case "Full" => new UtilizationModelFull()
    case "Stochastic" => new UtilizationModelStochastic()
    case _ => throw new RuntimeException("Unsuported utilization model")
  }

}
