package HelperUtils

import com.typesafe.config.Config
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRandom, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisioner, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerAbstract, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerAbstract, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModel, UtilizationModelDynamic, UtilizationModelFull, UtilizationModelPlanetLab, UtilizationModelStochastic}
import org.cloudbus.cloudsim.vms.network.NetworkVm
import org.cloudbus.cloudsim.vms.{VmCost, VmSimple}

import java.util

object CloudSimUtils {
  def getVmScheduler(t: String): VmSchedulerAbstract = t match {
    case "SpaceShared" => new VmSchedulerSpaceShared()
    case "TimeShared" => new VmSchedulerTimeShared()
    case _ => throw new RuntimeException("Unsupported vm scheduler")
  }

  def getCloudletScheduler(t: String): CloudletSchedulerAbstract = t match {
    case "SpaceShared" => new CloudletSchedulerSpaceShared()
    case "TimeShared" => new CloudletSchedulerTimeShared()
    case _ => throw new RuntimeException("Unsupported vm scheduler")
  }

  def getconfigValue(filename:String, path: String):Config  = ObtainConfigReference(filename,path) match {
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

  def getResourceProvisioner(t: String): ResourceProvisioner = t match {
    case  "PeSimple" => new PeProvisionerSimple()
    case "ResourceSimple" => new ResourceProvisionerSimple()
    case _ => throw new RuntimeException("Unsupported Resource Provisioner")
  }

  def getUtilizationModel(t: String): UtilizationModel = t match {
    case "Dynamic" => new UtilizationModelDynamic()
    case "Full" => new UtilizationModelFull()
    case "Stochastic" => new UtilizationModelStochastic()
    case _ => throw new RuntimeException("Unsuported utilization model")
  }


  def configureNetwork(broker: DatacenterBroker, datacenters: List[NetworkDatacenter]): Unit ={
    val topology:NetworkTopology  = new BriteNetworkTopology();
    datacenters.foreach(dc => {
      topology.addLink(broker,dc,1000,0.5)
    })
  }

  case class CostTotals(totalCost:Double, totalNonIdleVms: Int, processingTotalcose: Double, memoryTotalCost: Double, storagetotalCost: Double, bwTotalCost: Double,totalRunningTime: Double)


  def VmCostMetrics(vms: List[NetworkVm], printIndividual:Boolean): CostTotals = {
    val TotalMetrics: CostTotals = GetCostTotalCostMetrics(vms, vms.size, 0 ,CostTotals(0,0,0,0,0,0,0),printIndividual)
    TotalMetrics
  }

  def GetCostTotalCostMetrics(vms:List[NetworkVm], size: Int, start:Int, totals:CostTotals,printIndividual:Boolean): CostTotals = {
    if(size == 0)
      totals
    else{
      val costs = new VmCost(vms(start))

      val newTotals = totals.copy(totals.totalCost + costs.getTotalCost,
        totals.totalNonIdleVms + ((if(vms(start).getTotalExecutionTime  > 0) 1 else 0) ),
        totals.processingTotalcose + costs.getProcessingCost,
        totals.memoryTotalCost + costs.getMemoryCost,
        totals.storagetotalCost + costs.getStorageCost,
        totals.bwTotalCost + costs.getBwCost,
        totals.totalRunningTime + ((if(vms(start).getTotalExecutionTime  > 0) vms(start).getTotalExecutionTime else 0) )
      )

      if(printIndividual)
        println(costs)
      GetCostTotalCostMetrics(vms,size - 1, start + 1, newTotals,printIndividual)
    }
  }

  def printCostTotals(totals: CostTotals): Unit = {
    val costs: String = f"Total running Time: ${totals.totalRunningTime}%.2f Total CPU: ${totals.processingTotalcose}%.2f$$  Total Ram:${totals.memoryTotalCost}%.2f$$ Total Storage:${totals.storagetotalCost}%.2f$$ Total BW:${totals.bwTotalCost}%.2f$$ TotalCost:${totals.totalCost}%.2f$$"
    println(costs)
  }

  def cost1(dataCenter: NetworkDatacenter, cloudletList: List[CloudletSimple]): Double = {
    var cost: Double = 0.0
    val pricePerSecond = cloudletList.map(x => x.getCostPerSec(dataCenter))
    val finishTime = cloudletList.map(x => x.getFinishTime)
    cost = List.tabulate(finishTime.size)(x => pricePerSecond(x) * finishTime(x)).sum
    cost
  }



}
