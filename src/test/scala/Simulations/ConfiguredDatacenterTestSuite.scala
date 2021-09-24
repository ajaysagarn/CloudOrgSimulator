package Simulations

import HelperUtils.CloudSimUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.network.NetworkHost

class ConfiguredDatacenterTestSuite extends AnyFlatSpec with Matchers{

  val saasConfig = CloudSimUtils.getconfigValue("Saas","saasDataCenter")

  val testSim = new CloudSim()

  behavior of "Configured Datacenter module"

  "ConfiguredDatacenter" should "Create datacenter with correct costs" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()

    val dcCharac = datacenter.getCharacteristics
    assert(dcCharac.getCostPerBw == saasConfig.getDouble("costPerBW"))
    assert(dcCharac.getCostPerSecond == saasConfig.getDouble("costPerCPUSecond"))
    assert(dcCharac.getCostPerMem == saasConfig.getDouble("costPerMemory"))
    assert(dcCharac.getCostPerStorage == saasConfig.getDouble("costPerStorage"))
  }

  "ConfiguredDatacenter" should "Have correct policies" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()

    val dcCharac = datacenter.getCharacteristics
    assert(datacenter.getSchedulingInterval() == saasConfig.getDouble("schedulingInterval"))
    datacenter.getCharacteristics.getOs == saasConfig.getString("OS")
  }

  "ConfiguredDatacenter" should "have all hosts with correct attributes" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()
    val dcHosts = datacenter.getHostList[NetworkHost]

    assert(dcHosts.size() ==  saasConfig.getInt("host.Count"))

    dcHosts.forEach(host =>{
      assert(host.getMips() == saasConfig.getDouble("host.PemipsCapacity"))
      assert(host.getRam().getCapacity() == saasConfig.getLong("host.RAMInMBs"))
      assert(host.getNumberOfPes() == saasConfig.getDouble("host.Pes"))
    })
  }






}
