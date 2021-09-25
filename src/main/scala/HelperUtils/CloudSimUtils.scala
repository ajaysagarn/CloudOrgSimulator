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
  /**
   * Get the specified VMScheduer
   * @param t - the name of the Vm scheduler
   * @return
   */
  def getVmScheduler(t: String): VmSchedulerAbstract = t match {
    case "SpaceShared" => new VmSchedulerSpaceShared()
    case "TimeShared" => new VmSchedulerTimeShared()
    case _ => throw new RuntimeException("Unsupported vm scheduler")
  }

  /**
   * Get the specified Cloudlet Scheduler
   * @param t - the name of the cloudlet scheduler
   * @return
   */
  def getCloudletScheduler(t: String): CloudletSchedulerAbstract = t match {
    case "SpaceShared" => new CloudletSchedulerSpaceShared()
    case "TimeShared" => new CloudletSchedulerTimeShared()
    case _ => throw new RuntimeException("Unsupported vm scheduler")
  }

  /**
   * Get the config object from the filename and path
   * @param filename - The name of the config file
   * @param path - the path of the object within the file
   * @return
   */
  def getconfigValue(filename:String, path: String):Config  = ObtainConfigReference(filename,path) match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  /**
   * Get the specified DCVmAllocation policy
   * @param t - the name of the allocation policy
   * @return
   */
  def getDcVmAllocationPolicy(t: String): VmAllocationPolicy = t match {
    case "Simple" => new VmAllocationPolicySimple()
    case "Random" => new VmAllocationPolicyRandom(new UniformDistr())
    case "BestFit" => new VmAllocationPolicyBestFit()
    case "FirstFit" => new VmAllocationPolicyFirstFit()
    case "RoundRobin" => new VmAllocationPolicyRoundRobin()
    case _ => throw new RuntimeException("Unsupported Vm Allocation Policy")
  }

  /**
   * Get the specified resource provider
   * @param t - The name of the resource provider
   * @return
   */
  def getResourceProvisioner(t: String): ResourceProvisioner = t match {
    case  "PeSimple" => new PeProvisionerSimple()
    case "ResourceSimple" => new ResourceProvisionerSimple()
    case _ => throw new RuntimeException("Unsupported Resource Provisioner")
  }

  /**
   * Get the specified Utilization model
   * @param t - the name of the utilization model
   * @return
   */
  def getUtilizationModel(t: String): UtilizationModel = t match {
    case "Dynamic" => new UtilizationModelDynamic()
    case "Full" => new UtilizationModelFull()
    case "Stochastic" => new UtilizationModelStochastic()
    case _ => throw new RuntimeException("Unsuported utilization model")
  }


  /**
   * Configure the network by manually adding links between each datacenter and the broker
   * @param broker - The broker in the network
   * @param datacenters - List of datacenters in the network topology
   */
  def configureNetwork(broker: DatacenterBroker, datacenters: List[NetworkDatacenter]): Unit ={
    val topology:NetworkTopology  = new BriteNetworkTopology();
    datacenters.foreach(dc => {
      topology.addLink(broker,dc,1000,0.5)
    })
  }

  /**
   * Class to maintain overall costs of a simulation
   * @param totalCost - The total costs from running all the vms
   * @param totalNonIdleVms - The total vms that are not idle
   * @param processingTotalcose - The total cpu processing costs incurred
   * @param memoryTotalCost - The total memory costs incurred
   * @param storagetotalCost - The total storage costs incurred
   * @param bwTotalCost - The total bandwidth costs incurred
   * @param totalRunningTime -The total running time all the vms
   */
  case class CostTotals(totalCost:Double, totalNonIdleVms: Int, processingTotalcose: Double, memoryTotalCost: Double, storagetotalCost: Double, bwTotalCost: Double,totalRunningTime: Double)

  /**
   * Get the overall cost metrics from a list of vms.
   * @param vms - The list of vms who's costs must be calculated
   * @param printIndividual - if the individual costs must be printed/logged to the console
   * @return
   */
  def VmCostMetrics(vms: List[NetworkVm], printIndividual:Boolean): CostTotals = {
    val TotalMetrics: CostTotals = GetCostTotalCostMetrics(vms, vms.size, 0 ,CostTotals(0,0,0,0,0,0,0),printIndividual)
    TotalMetrics
  }

  /**
   * Recursive function used to calculate the overall cost parameters of the vms
   * @param vms - the list of vms
   * @param size - the number of vms
   * @param start - the starting index
   * @param totals - the current value of the totals. This is updated on each recursion
   * @param printIndividual - if the individual costs must be printed/logged to the console
   * @return
   */
  def GetCostTotalCostMetrics(vms:List[NetworkVm], size: Int, start:Int, totals:CostTotals,printIndividual:Boolean): CostTotals = {
    if(size == 0)
      totals
    else{
      val costs = new VmCost(vms(start)) //gets the cost values

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
      //increment the start index and reduce the size for the next recursion  
      GetCostTotalCostMetrics(vms,size - 1, start + 1, newTotals,printIndividual)
    }
  }

  /**
   * Logs/prints the totals to the console.
   * @param totals - The case class consisting of the cost parameters
   */
  def printCostTotals(totals: CostTotals): Unit = {
    val costs: String = f"Total running Time: ${totals.totalRunningTime}%.2f Total CPU: ${totals.processingTotalcose}%.2f$$  Total Ram:${totals.memoryTotalCost}%.2f$$ Total Storage:${totals.storagetotalCost}%.2f$$ Total BW:${totals.bwTotalCost}%.2f$$ TotalCost:${totals.totalCost}%.2f$$"
    println(costs)
  }

  /**
   * Calculate cummulative cloudlet running costs, based on the datacenter in which they run.
   * @param dataCenter - The datacenter who's cost calculations will be applied to the cloudlet
   * @param cloudletList - The list of cloudlets, who's costs needs to be aggregated.
   * @return
   */
  def cost1(dataCenter: NetworkDatacenter, cloudletList: List[CloudletSimple]): Double = {
    var cost: Double = 0.0
    val pricePerSecond = cloudletList.map(x => x.getCostPerSec(dataCenter))
    val finishTime = cloudletList.map(x => x.getFinishTime)
    cost = List.tabulate(finishTime.size)(x => pricePerSecond(x) * finishTime(x)).sum
    cost
  }



}
