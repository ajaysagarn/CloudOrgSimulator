package CloudSimWrappers

import HelperUtils.CloudSimUtils
import Simulations.BasicCloudSimPlusExample.config
import Simulations.RandomVmAllocationSim.config
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.Simulation
import org.cloudbus.cloudsim.datacenters.{Datacenter, DatacenterSimple}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.PeSimple

import collection.JavaConverters.*

class ConfiguredDataCenter(simulation: Simulation, config: Config) {

  val dataCenter = createDatacenter()

  def getConfiguredDataCenter(): Datacenter = dataCenter

  def createDatacenter(): Datacenter = {
    val hosts: List[Host] = getHostList()
    val dc = new NetworkDatacenter(simulation,hosts.asJava,CloudSimUtils.getDcVmAllocationPolicy(config.getString("VMAllocationPolicy")))
    dc.setSchedulingInterval(config.getDouble("schedulingInterval"))

    dc.getCharacteristics()
      .setCostPerStorage(config.getDouble("costPerStorage"))
      .setCostPerSecond(config.getDouble("costPerCPUSecond"))
      .setCostPerMem(config.getDouble("costPerMemory"))
      .setCostPerBw(config.getDouble("costPerBW"))
/*      .setOs(config.getString("schedulingInterval"))
      .setVmm(config.getString("schedulingInterval"))*/
    dc

  }



  def getHostList(): List[Host] = {
    val hostCount = if (config.getString("host.CreationType") == "static") config.getInt("host.Count") else 0
    List.fill(hostCount)(new HostSimple(config.getLong("host.RAMInMBs"),
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

}
