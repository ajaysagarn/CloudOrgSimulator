package CloudSimWrappers

import HelperUtils.{CloudSimUtils}
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

/**
 * Creates a @Link{NetworkDatacenter} instance based on the configuration passed
 * @param simulation - The cloudsim simulation instance
 * @param config - The configuration to be used to set up the datacenter
 */
class ConfiguredDataCenter(simulation: Simulation, config: Config) {

  val dataCenter = createDatacenter()

  /**
   * Get the datacenter instance
   * @return NetworkDatacenter
   */
  def getConfiguredDataCenter(): NetworkDatacenter = dataCenter

  /**
   * Creates the datacenter instance
   * @return
   */
  def createDatacenter(): NetworkDatacenter = {
    // create a list of hosts for the datacenter
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

  /**
   * Creates a list of hosts for the datacenter based on the config
   * @return List[Host]
   */
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

  /**
   * Creates a list of Processing entities (Pes) for the host based on the config
   * @return List[Pesimple]
   */
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
  
}
