package Simulations

import Simulations.BasicCloudSimPlusExample.config
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*

object NetworkExample {


  @main
  def networkExample() =

    val simulation = new CloudSim()

    val peList = List.apply(new PeSimple(1000, new PeProvisionerSimple()))
    val hostList = List.apply(new HostSimple(2048,10000,1000000,peList.asJava))

    val datacenter0 = new DatacenterSimple(simulation,hostList.asJava)

    val broker = new DatacenterBrokerSimple(simulation)

    val networkTopology = BriteNetworkTopology.getInstance("topology.brite")

    simulation.setNetworkTopology(networkTopology)

    networkTopology.mapNode(datacenter0, 0)
    networkTopology.mapNode(broker, 3)


    val vmList = List(
      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), 1)
        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))
    )

    broker.submitVmList(vmList.asJava)
    val cloudletList = List.apply(new CloudletSimple(1000,1))
    broker.submitCloudletList(cloudletList.asJava)

    simulation.start()

    new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();

  def configureNetwork() =
    val networkTopology = BriteNetworkTopology.getInstance("topology.brite")



}
