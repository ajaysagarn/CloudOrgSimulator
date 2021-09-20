package CloudSimWrappers

import HelperUtils.CloudSimUtils
import Simulations.BasicCloudSimPlusExample.config
import Simulations.RandomVmAllocationSim.config
import com.typesafe.config.Config
import org.cloudbus.cloudsim.core.Simulation
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.PeSimple

import collection.JavaConverters.*

class ConfiguredDataCenter(simulation: Simulation, config: Config) {
  
  def getConfiguredDataCenter(): DatacenterSimple = {
    val hosts: List[Host] = getHostList()
    new DatacenterSimple(simulation,hosts.asJava)
  }

  def getHostList(): List[Host] = {
    val hostCount = if (config.getString("host.CreationType") == "static") config.getInt("host.Count") else 0
    List.fill(hostCount)(new HostSimple(config.getLong("host.RAMInMBs"),
      config.getLong("host.StorageInMBs"),
      config.getLong("host.BandwidthInMBps"),
      getPes().asJava).setVmScheduler(CloudSimUtils.getVmScheduler(config.getString("host.VMschedulerType"))))
  }
  
  def getPes(): List[PeSimple] = {
    List.fill(config.getInt("host.Pes"))(new PeSimple(config.getDouble("host.mipsCapacity")))
  }
  
}