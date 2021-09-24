package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import Simulations.BasicCloudSimPlusExample.config
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.distributions.UniformDistr
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import scala.jdk.OptionConverters.*
import collection.JavaConverters.*

import scala.util.Random

class RandomVmAllocationSim

object RandomVmAllocationSim:
  val config = ObtainConfigReference("","randomCloudVmAllocation") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  val logger = CreateLogger(classOf[RandomVmAllocationSim])

  def Start() =

    val random = new UniformDistr()

    val sim = new CloudSim()
    val hostPes = List(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))

    val hosts = List.fill(config.getInt("randomCloudVmAllocation.host.hosts"))(new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
      config.getLong("cloudSimulator.host.StorageInMBs"),
      config.getLong("cloudSimulator.host.BandwidthInMBps"),
      hostPes.asJava))

    val vmAllocationPolicy = new VmAllocationPolicySimple()
    vmAllocationPolicy.setFindHostForVmFunction(findRandomSuitableHostForVm)

    val datacenter = new DatacenterSimple(sim,hosts.asJava,vmAllocationPolicy)

    val broker0 = new DatacenterBrokerSimple(sim)


    val vmList = List.fill(config.getInt("randomCloudVmAllocation.vm.vms"))(new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")))

    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.utilizationRatio"));
    val cloudlets = List.fill(config.getInt("randomCloudVmAllocation.cloudlet.cloudlets"))(new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs")))

    broker0.submitVmList(vmList.asJava)
    broker0.submitCloudletList(cloudlets.asJava)

    sim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();



  def findRandomSuitableHostForVm(vmAllocationPolicy: VmAllocationPolicy, vm: Vm) =
    val hostList = vmAllocationPolicy.getHostList().asScala.toList.map((x:Host) => x)
    hostList.find(host => host.isSuitableForVm(vm)).toJava
    







