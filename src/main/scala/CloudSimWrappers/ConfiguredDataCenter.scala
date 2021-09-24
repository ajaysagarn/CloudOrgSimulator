package CloudSimWrappers

import HelperUtils.CloudSimUtils
import Simulations.BasicCloudSimPlusExample.config
import Simulations.RandomVmAllocationSim.config
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.network.NetworkHost
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.network.switches.EdgeSwitch
import org.cloudbus.cloudsim.network.topologies.{BriteNetworkTopology, NetworkTopology}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.PeSimple

import collection.JavaConverters.*

class ConfiguredDataCenter(simulation: Simulation, config: Config) {

  val dataCenter = createDatacenter()
  
  def getConfiguredDataCenter(): NetworkDatacenter = dataCenter

  def createDatacenter(): NetworkDatacenter = {
    val hosts: List[Host] = getHostList()
    val dc = new NetworkDatacenter(simulation,hosts.asJava,CloudSimUtils.getDcVmAllocationPolicy(config.getString("VMAllocationPolicy")))
    dc.setSchedulingInterval(config.getDouble("schedulingInterval"))

    dc.getCharacteristics()
      .setCostPerStorage(config.getDouble("costPerStorage"))
      .setCostPerSecond(config.getDouble("costPerCPUSecond"))
      .setCostPerMem(config.getDouble("costPerMemory"))
      .setCostPerBw(config.getDouble("costPerBW"))

    dc
  }

  def getHostList(): List[Host] = {
    val hostCount = if (config.getString("host.CreationType") == "static") config.getInt("host.Count") else 0
    List.fill(hostCount)(new NetworkHost(config.getLong("host.RAMInMBs"),
      config.getLong("host.StorageInMBs"),
      config.getLong("host.BandwidthInMBps"),
      getPes().asJava)
      .setVmScheduler(CloudSimUtils.getVmScheduler(config.getString("host.VMschedulerType")))
      .setRamProvisioner(ResourceProvisionerSimple())
      .setBwProvisioner(ResourceProvisionerSimple()))
  }

  def getPes(): List[PeSimple] = {
    List.fill(config.getInt("host.Pes"))(new PeSimple(config.getDouble("host.PemipsCapacity"),PeProvisionerSimple()))
  }

  /**
   * Creates internal Datacenter network.
   *
   * @param datacenter Datacenter where the network will be created
   */
  def createNetwork(datacenter: NetworkDatacenter): Unit = {
    val edgeSwitch = new EdgeSwitch(simulation.asInstanceOf[CloudSim], datacenter)

    datacenter.getHostList[NetworkHost].forEach(host =>{
      edgeSwitch.connectHost(host)
    })

  }

  /**
   * Gets the index of a switch where a Host will be connected,
   * considering the number of ports the switches have.
   * Ensures that each set of N Hosts is connected to the same switch
   * (where N is defined as the number of switch's ports).
   * Since the host ID is long but the switch array index is int,
   * the module operation is used safely convert a long to int
   * For instance, if the id is 2147483648 (higher than the max int value 2147483647),
   * it will be returned 0. For 2147483649 it will be 1 and so on.
   *
   * @param host        the Host to get the index of the switch to connect to
   * @param switchPorts the number of ports (N) the switches where the Host will be connected have
   * @return the index of the switch to connect the host
   */
  def getSwitchIndex(host: NetworkHost, switchPorts: Int): Int = (host.getId % Integer.MAX_VALUE.round / switchPorts).toInt

}
